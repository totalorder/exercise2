package se.kth.id2212.ex2.marketplace;

import se.kth.id2212.ex2.bankrmi.RejectedException;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A marketplace that allows a client to find, sell, buy and wish to buy items
 * When an item is sold, or a wished item becomes available, the corresponding client will be notified
 *
 * LIMITATION: There can only be one seller and wisher for a certain item+price.
 */
public class MarketplaceImpl extends UnicastRemoteObject implements Marketplace {
    private String marketplaceName;

    private Map<Item, Client> itemsForSale = new HashMap<>();
    private Map<Item, Client> wishes = new HashMap<>();

    public MarketplaceImpl(String marketplaceName) throws RemoteException {
        super();
        this.marketplaceName = marketplaceName;
    }

    @Override
    public synchronized List<Item> listItemsForSale() throws RemoteException {
        return itemsForSale.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    @Override
    public synchronized void wish(Client client, Item item) throws RemoteException {
        wishes.put(item, client);
    }

    @Override
    public synchronized boolean buy(Client buyer, Item item) throws RemoteException {
        // Find a seller for the given item, return false if not available
        final Client seller = itemsForSale.get(item);
        if (seller == null) {
            return false;
        }

        // Withdraw
        try {
            buyer.getBankAccount().withdraw((float)item.price);
        } catch (RejectedException e) {
            return false;
        }


        // Deposit
        try {
            seller.getBankAccount().deposit(item.price);
        } catch (RejectedException e) {
            // Roll back transaction on deposit failure
            try {
                buyer.getBankAccount().deposit((float) item.price);
            } catch (RejectedException ee) {
                throw new RuntimeException("Failed to roll back transaction! Call customer support.");
            }
            return false;
        }

        // Remove item from for-sale
        itemsForSale.remove(item);

        // Notify the client that the item is sold
        seller.onItemSoldCallback(item);
        return true;
    }

    @Override
    public synchronized void sell(Client seller, Item item) throws RemoteException {
        // Put the item up for sale
        itemsForSale.put(item, seller);

        final List<Item> wishedItems = wishes.entrySet().stream()
            // Find wishes for the item with same or lower price
            .filter(entry -> entry.getKey().name.equals(item.name) && entry.getKey().price >= item.price)
            .map(Map.Entry::getKey).collect(Collectors.toList());

        // Notify all wishers
        for (final Item wishedItem : wishedItems) {
            final Client wishingBuyer = wishes.get(wishedItem);
            // Notify whe wishing buyer of the available price
            wishingBuyer.onWishedItemAvailableCallback(item);

            // Remove the wish
            wishes.remove(wishedItem);
        }
    }
}
