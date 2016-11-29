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

  public Tx open() {
    connectionLock.lock();
    try {
      connection.setAutoCommit(false);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return new Tx(connection, connectionLock);
  }

  public void close() throws SQLException {
    connection.close();
  }
}
