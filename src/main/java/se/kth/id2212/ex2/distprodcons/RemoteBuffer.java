package se.kth.id2212.ex2.distprodcons;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A FIFO buffer that can be called remotely.
 */
public interface RemoteBuffer extends Remote {
    /**
     * Stores the specified integer last in the buffer.
     * 
     * @param i The integer that will be stored in the buffer.
     * @throws RemoteException If failed storing in the buffer.
     */
    void put(Integer i) throws RemoteException;
    
    /**
     * Reads, and the removes, the first element in the buffer.
     * 
     * @return The first element in the buffer.
     * @throws RemoteException If failed reading from the buffer.
     */
    Integer get() throws RemoteException;
}
