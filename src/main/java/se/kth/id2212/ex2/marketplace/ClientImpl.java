package se.kth.id2212.ex2.marketplace;

import se.kth.id2212.ex2.bankrmi.Account;
import se.kth.id2212.ex2.bankrmi.Bank;
import se.kth.id2212.ex2.bankrmi.RejectedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.StringTokenizer;

/**
 * A marketplace Client that allows a user to find, sell, buy and wish to buy items
 * The user can also list his or her bank balance
 *
 * When an item is sold, or a wished item becomes available, the user will be notified
 */
public class ClientImpl extends UnicastRemoteObject implements Client {
    private static final String USAGE = "java marketplace.Client <user_name> <password> [<marketplace_url>] [<bank_url>]";
    private static final String DEFAULT_MARKETPLACE_NAME = "Blocket";
    private static final String DEFAULT_BANK_NAME = "Nordea";
    private Account account;
    private Marketplace marketplace;
    private String marketplaceName;
    private String username;
    private String bankName;

    @Override
    public void onItemSoldCallback(Item item) throws RemoteException {
        System.out.println("Sold item: " + item);
    }

    @Override
    public void onWishedItemAvailableCallback(Item item) throws RemoteException {
        System.out.println("Wished item " + item + " is now available");
    }

    @Override
    public Account getBankAccount() {
        return account;
    }

    @Override
    public String getUsername() throws RemoteException {
        return username;
    }

    @Override
    public String getBankName() throws RemoteException {
        return bankName;
    }

    static enum CommandName {
        sell, buy, list, wish, balance, history, quit, help;
    };

    public ClientImpl(final String username, final String password, final String marketplaceName, final String bankName) throws RemoteException {
        this(marketplaceName);
        this.username = username;
        this.bankName = bankName;
        try {
            // Connect to the bank and get or create a new account (with a $5000 bonus if new)
            final Bank bank = (Bank) Naming.lookup(bankName);
            Account existingAccount = bank.getAccount(username);
            if (existingAccount == null) {
                existingAccount = bank.newAccount(username);
                existingAccount.deposit(5000);
            }
            this.account = existingAccount;

            if (marketplace.registerUser(username, password)) {
                System.out.println("Registered user " + username);
            }
            if (!marketplace.login(this, username, password)) {
                System.out.println("Incorrect username/password!");
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println("The runtime failed: " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Connected to bank: " + bankName);
    }

    public ClientImpl(final String marketplaceName) throws RemoteException {
        super();
        this.marketplaceName = marketplaceName;
        try {
            // Connect to the marketplace
            marketplace = (Marketplace)Naming.lookup(this.marketplaceName);
        } catch (Exception e) {
            System.out.println("The runtime failed: " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Connected to marketplace: " + this.marketplaceName);
    }

    public ClientImpl(final String username, final String password) throws RemoteException {
        this(username, password, DEFAULT_MARKETPLACE_NAME, DEFAULT_BANK_NAME);
    }

    public ClientImpl(final String username, final String password, final String marketplaceName) throws RemoteException {
        this(username, password, marketplaceName, DEFAULT_BANK_NAME);
    }

    public void run() {
        BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));

        // Read from stdin until the user types "quit"
        while (true) {
            System.out.print(username + "@" + marketplaceName + ">");
            try {
                String userInput = consoleIn.readLine();
                execute(parse(userInput));
            } catch (RejectedException re) {
                System.out.println(re);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Command parse(String userInput) {
        if (userInput == null) {
            return null;
        }

        StringTokenizer tokenizer = new StringTokenizer(userInput);
        if (tokenizer.countTokens() == 0) {
            return null;
        }

        CommandName commandName = null;
        int amount = 0;
        int userInputTokenNo = 1;
        String itemName = null;

        // Parse the parameters commandName, itemName and amount - in that order
        while (tokenizer.hasMoreTokens()) {
            switch (userInputTokenNo) {
                case 1:
                    try {
                        String commandNameString = tokenizer.nextToken();
                        commandName = CommandName.valueOf(CommandName.class, commandNameString);
                    } catch (IllegalArgumentException commandDoesNotExist) {
                        System.out.println("Illegal command");
                        return null;
                    }
                    break;
                case 2:
                    itemName  = tokenizer.nextToken();
                    break;
                case 3:
                    try {
                        amount = Integer.parseInt(tokenizer.nextToken());
                    } catch (NumberFormatException e) {
                        System.out.println("Illegal amount");
                        return null;
                    }
                    break;
                default:
                    System.out.println("Illegal command");
                    return null;
            }
            userInputTokenNo++;
        }
        return new Command(commandName, itemName, amount);
    }

    void execute(Command command) throws RemoteException, RejectedException {
        if (command == null) {
            return;
        }

        Item item;

        switch (command.getCommandName()) {
            case list:
                try {
                    marketplace.listItemsForSale(this).forEach(System.out::println);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                return;
            case quit:
                System.exit(0);
            case help:
                for (CommandName commandName : CommandName.values()) {
                    System.out.println(commandName);
                }
            case sell:
                item = new Item(command.itemName, command.amount);
                marketplace.sell(this, item);
                System.out.println("Put item " + item + " up for sale.");
                break;
            case buy:
                item = new Item(command.itemName, command.amount);
                final boolean boughItem = marketplace.buy(this, item);
                if (boughItem) {
                    System.out.println("Bought item " + item + ".");
                } else {
                    System.out.println("ERROR: Failed buying item " + item + ".");
                }
                break;
            case wish:
                item = new Item(command.itemName, command.amount);
                marketplace.wish(this, item);
                System.out.println("Put up wish for buying item " + item + ".");
                break;
            case balance:
                System.out.println("Balance: $" + account.getBalance());
                break;
            case history:
                System.out.println("History: " + marketplace.getHistory(this));
                break;
            default:
                System.out.println("Illegal command");
        }
    }

    private class Command {
        private String itemName;
        private int amount;
        private CommandName commandName;

        private float getAmount() {
            return amount;
        }


        private CommandName getCommandName() {
            return commandName;
        }

        private Command(ClientImpl.CommandName commandName, String itemName, int amount) {
            this.commandName = commandName;
            this.itemName = itemName;
            this.amount = amount;
        }
    }

    public static void main(String[] args) {
        if ((args.length > 4) || args.length < 2 || (args.length > 0 && args[0].equals("-h"))) {
            System.out.println(USAGE);
            System.exit(1);
        }

        String username = args[0];
        String password = args[1];
        try {
            if (args.length == 2) {
                new ClientImpl(username, password).run();
            } else if (args.length == 3) {
                new ClientImpl(username, password, args[2]).run();
            } else if (args.length == 4) {
                new ClientImpl(username, password, args[2], args[3]).run();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
