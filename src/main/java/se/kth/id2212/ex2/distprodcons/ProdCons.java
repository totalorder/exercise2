package se.kth.id2212.ex2.distprodcons;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ProdCons {

    public static void main(String[] args) throws RemoteException,
                                                  NotBoundException,
                                                  MalformedURLException {
        RemoteBuffer buffer = (RemoteBuffer) Naming.lookup("buffer");
        Thread consumerThread = new Thread(new Consumer(buffer));
        Thread producerThread = new Thread(new Producer(buffer, 100));

        consumerThread.start();
        producerThread.start();

        try {
            consumerThread.join();
            producerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
