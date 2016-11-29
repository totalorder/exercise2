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
public class Tx {
  private final Connection connection;
  private final Lock connectionLock;

  Tx(final Connection connection, final Lock connectionLock) {
    this.connection = connection;
    this.connectionLock = connectionLock;
  }

  public void commit() {
    try {
      connection.commit();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    connectionLock.unlock();
  }

  public void rollback() {
    try {
      connection.rollback();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    connectionLock.unlock();
  }

  private PreparedStatement createQuery(final String sql, final Object... args) {
    try {
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
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void execute(final String sql, final Object... args) {
    try {
      final PreparedStatement preparedStatement = createQuery(sql, args);
      try {
        preparedStatement.execute();
      } finally {
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public int update(final String sql, final Object... args) {
    final PreparedStatement preparedStatement = createQuery(sql, args);

    try {
      try {
        return preparedStatement.executeUpdate();
      } finally {
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Map<String, Object>> select(final String sql, final Object... args) {
    final List<Map<String, Object>> rows = new ArrayList<>();

    try {
      PreparedStatement preparedStatement = null;
      ResultSet rs = null;
      try {
        preparedStatement = createQuery(sql, args);

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
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
