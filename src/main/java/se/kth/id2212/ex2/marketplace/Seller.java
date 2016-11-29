package se.kth.id2212.ex2.marketplace;

import se.kth.id2212.ex2.bankrmi.Account;
import se.kth.id2212.ex2.bankrmi.Bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Seller {
  public final String username;
  public final String bankName;

  public Seller(final String username, final String bankName) {
    this.username = username;
    this.bankName = bankName;
  }

  public Account getBankAccount() {
    try {
      final Bank sellerBank = (Bank) Naming.lookup(bankName);
      return sellerBank.getAccount(username);
    } catch (NotBoundException | MalformedURLException | RemoteException e) {
      throw new RuntimeException(e);
    }
  }
}
