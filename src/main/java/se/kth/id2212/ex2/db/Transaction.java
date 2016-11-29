package se.kth.id2212.ex2.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Created by lingon on 11/29/16.
 */
public class Transaction {
  private final Connection connection;
  private final Lock connectionLock;

  Transaction(final Connection connection, final Lock connectionLock) {
    this.connection = connection;
    this.connectionLock = connectionLock;
  }

  public void commit() throws SQLException {
    connection.commit();
    connectionLock.unlock();
  }

  public void rollback() throws SQLException {
    connection.rollback();
    connectionLock.unlock();
  }

  private PreparedStatement createQuery(final String sql, final Object... args) throws SQLException {
    final PreparedStatement preparedStatement = connection.prepareStatement(sql);
    // Apply args to statement
    int index = 0;
    for (final Object arg : args) {
      index++;
      if (arg instanceof Integer) {
        preparedStatement.setInt(index, (int) arg);
      } else if (arg instanceof String) {
        preparedStatement.setString(index, (String) arg);
      } else {
        throw new RuntimeException("Unknown type: " + arg.getClass().getName());
      }
    }
    return preparedStatement;
  }

  public void execute(final String sql, final Object... args) throws SQLException {
    final PreparedStatement preparedStatement = createQuery(sql, args);
    try {
      preparedStatement.execute();
    } finally {
      if (preparedStatement != null) {
        preparedStatement.close();
      }
    }
  }

  public int update(final String sql, final Object... args) throws SQLException {
    final PreparedStatement preparedStatement = createQuery(sql, args);
    try {
      return preparedStatement.executeUpdate();
    } finally {
      if (preparedStatement != null) {
        preparedStatement.close();
      }
    }
  }

  public List<Map<String, Object>> select(final String sql, final Object... args) throws SQLException {
    final List<Map<String, Object>> rows = new ArrayList<>();
    final PreparedStatement preparedStatement = createQuery(sql, args);
    ResultSet rs = null;
    try {
      rs = preparedStatement.executeQuery();

      // Get column names
      final ResultSetMetaData meta = rs.getMetaData();
      final List<String> columnNames = new ArrayList<>();
      for (int index = 1; index <= meta.getColumnCount(); index++) {
        columnNames.add(meta.getColumnName(index));
      }

      while (rs.next()) {
        // Put row data in map
        final Map<String, Object> row = new HashMap<>();
        for (String columnName : columnNames) {
          final Object val = rs.getObject(columnName);
          row.put(columnName, val);
        }
        rows.add(row);
      }
    } finally {
      // Clean up
      if (rs != null) {
        rs.close();
      }

      if (preparedStatement != null) {
        preparedStatement.close();
      }
    }

    return rows;
  }
}
