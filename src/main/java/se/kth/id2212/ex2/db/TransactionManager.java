package se.kth.id2212.ex2.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TransactionManager {
  private final Connection connection;
  private final Lock connectionLock;

  public TransactionManager(final Connection connection) {
    this.connection = connection;
    this.connectionLock = new ReentrantLock();
  }

  public Transaction open() throws SQLException {
    connectionLock.lock();
    connection.setAutoCommit(false);
    return new Transaction(connection, connectionLock);
  }

  public void close() throws SQLException {
    connection.close();
  }
}
