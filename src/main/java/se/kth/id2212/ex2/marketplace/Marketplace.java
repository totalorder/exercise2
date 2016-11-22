package se.kth.id2212.ex2.marketplace;

import se.kth.id2212.ex2.bankrmi.Account;
import se.kth.id2212.ex2.bankrmi.RejectedException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Marketplace extends Remote {
    public List<Item> listItemsForSale() throws RemoteException;
    public void wish(Client client, Item item) throws RemoteException;
    public boolean buy(Client client, Item item) throws RemoteException;
    public void sell(Client client, Item item) throws RemoteException;
}
