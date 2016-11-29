package se.kth.id2212.ex2.marketplace;

import se.kth.id2212.ex2.bankrmi.Account;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote {
  public void onItemSoldCallback(Item item) throws RemoteException;
  public void onWishedItemAvailableCallback(Item item) throws RemoteException;
  public Account getBankAccount() throws RemoteException;
  public String getUsername() throws RemoteException;
  public String getBankName() throws RemoteException;
}
