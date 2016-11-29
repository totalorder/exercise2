package se.kth.id2212.ex2.marketplace;

import org.apache.commons.codec.digest.DigestUtils;
import se.kth.id2212.ex2.bankrmi.Account;
import se.kth.id2212.ex2.bankrmi.Bank;
import se.kth.id2212.ex2.bankrmi.RejectedException;
import se.kth.id2212.ex2.db.TransactionManager;
import se.kth.id2212.ex2.db.Tx;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A marketplace that allows a client to find, sell, buy and wish to buy items
 * When an item is sold, or a wished item becomes available, the corresponding client will be notified
 *
 * LIMITATION: There can only be one seller and wisher for a certain item+price.
 */
public class MarketplaceImpl extends UnicastRemoteObject implements Marketplace {
    private String marketplaceName;

    private final TransactionManager transactionManager;
    private final UserStore userStore;
    private final ItemStore itemStore;
    private final HistoryStore historyStore;

    private Map<Item, Client> wishes = new HashMap<>();
    private Map<Client, String> loggedInUsers = new HashMap<>();

    public MarketplaceImpl(final String marketplaceName,
                           final TransactionManager transactionManager,
                           final UserStore userStore,
                           final ItemStore itemStore,
                           final HistoryStore historyStore) throws RemoteException {
        super();
        this.marketplaceName = marketplaceName;
        this.transactionManager = transactionManager;
        this.userStore = userStore;
        this.itemStore = itemStore;
        this.historyStore = historyStore;
    }

    private void ensureLoggedIn(Client client) {
        if (loggedInUsers.get(client) == null) {
            throw new RuntimeException("Client not logged in!");
        }
    }

    @Override
    public synchronized List<Item> listItemsForSale(Client client) throws RemoteException {
        ensureLoggedIn(client);
        final Tx tx = transactionManager.open();
        try {
            final List<Item> items = itemStore.listItems(tx);
            tx.commit();
            return items;
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    private Client getSeller(final String username) {
        return loggedInUsers.entrySet()
            .stream()
            .filter(entry -> entry.getValue().equals(username))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElseGet(() -> null);
    }

    @Override
    public synchronized void wish(Client client, Item item) throws RemoteException {
        ensureLoggedIn(client);
        wishes.put(item, client);
    }

    @Override
    public synchronized boolean buy(Client buyer, Item item) throws RemoteException {
        ensureLoggedIn(buyer);

        final Tx tx = transactionManager.open();
        try {
            // Find seller
            final Seller seller = itemStore.purchaseItem(tx, item);
            if (seller == null) {
                tx.commit();
                return false;
            }

            // Get seller bank account
            final Account sellerAccount = seller.getBankAccount();
            if (sellerAccount == null) {
                tx.rollback();
                return false;
            }

            historyStore.incrementBought(tx, buyer.getUsername());
            historyStore.incrementSold(tx, seller.username);

            // Withdraw
            try {
                buyer.getBankAccount().withdraw((float)item.price);
            } catch (RejectedException e) {
                tx.rollback();
                return false;
            }

            // Deposit
            try {
                sellerAccount.deposit(item.price);
            } catch (RejectedException e) {
                // Roll back transaction on deposit failure
                try {
                    buyer.getBankAccount().deposit((float) item.price);
                } catch (RejectedException ee) {
                    throw new RuntimeException("Failed to roll back transaction! Call customer support.");
                }
                tx.rollback();
                return false;
            }
            tx.commit();

            // Notify the client that the item is sold if logged in
            final Client sellerClient = getSeller(seller.username);
            if (sellerClient != null) {
                sellerClient.onItemSoldCallback(item);
            }
            return true;
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    @Override
    public synchronized void sell(Client seller, Item item) throws RemoteException {
        ensureLoggedIn(seller);

        final Tx tx = transactionManager.open();
        try {
            // Put the item up for sale
            itemStore.createSale(tx, seller.getUsername(), seller.getBankName(), item);
            tx.commit();

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
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    @Override
    public boolean registerUser(String username, String password) throws RemoteException {
        if (password.length() < 8) {
            return false;
        }
        final String hashedPassword = DigestUtils.sha1Hex(password);
        final Tx tx = transactionManager.open();
        try {
            final boolean created = userStore.createUser(tx, username, hashedPassword);
            tx.commit();
            return created;
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    @Override
    public synchronized boolean login(Client client, String username, String password) throws RemoteException {
        final String hashedPassword = DigestUtils.sha1Hex(password);
        final Tx tx = transactionManager.open();
        try {
            final boolean exists = userStore.exists(tx, username, hashedPassword);
            tx.commit();
            if (exists) {
                loggedInUsers.put(client, username);
            }
            return exists;
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    @Override
    public synchronized void logout(final Client client) throws RemoteException {
        loggedInUsers.remove(client);
    }

    @Override
    public History getHistory(final Client client) throws RemoteException {
        ensureLoggedIn(client);

        final Tx tx = transactionManager.open();
        try {
            final History history = historyStore.getOrCreateHistory(tx, client.getUsername());
            tx.commit();
            return history;
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
}
