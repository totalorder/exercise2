package se.kth.id2212.ex2.marketplace;

import se.kth.id2212.ex2.bankrmi.Bank;
import se.kth.id2212.ex2.bankrmi.BankImpl;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Server {
    private static final String USAGE = "java marketplace.Server <bank_rmi_url>";
    private static final String MARKETPLACE = "Blocket";

    public Server(String marketplaceName) {
        try {
            Marketplace marketplace = new MarketplaceImpl(marketplaceName);
            // Register the newly created object at rmiregistry.
            try {
                LocateRegistry.getRegistry(1099).list();
            } catch (RemoteException e) {
                LocateRegistry.createRegistry(1099);
            }
            Naming.rebind(marketplaceName, marketplace);
            System.out.println(marketplaceName + " is ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length > 1 || (args.length > 0 && args[0].equalsIgnoreCase("-h"))) {
            System.out.println(USAGE);
            System.exit(1);
        }

        String marketplaceName;
        if (args.length > 0) {
            marketplaceName = args[0];
        } else {
            marketplaceName = MARKETPLACE;
        }

        new Server(marketplaceName);
    }
}
