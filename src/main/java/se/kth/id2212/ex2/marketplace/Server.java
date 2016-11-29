package se.kth.id2212.ex2.marketplace;

import se.kth.id2212.ex2.db.JdbcUtil;
import se.kth.id2212.ex2.db.TransactionManager;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.sql.Connection;

public class Server {
    private static final String USAGE = "java marketplace.Server <bank_rmi_url>";
    private static final String MARKETPLACE = "Blocket";

    public Server(String marketplaceName) {
        try {
            final Connection connection = JdbcUtil.getConnection();
            final TransactionManager transactionManager = new TransactionManager(connection);
            final UserStore userStore = new UserStore();
            final ItemStore itemStore = new ItemStore();
            final HistoryStore historyStore = new HistoryStore();
            final Marketplace marketplace = new MarketplaceImpl(
                marketplaceName, transactionManager, userStore, itemStore, historyStore);
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
