package se.kth.id2212.ex2.db;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcUtil {
  public static Connection getConnection() throws SQLException {
    String url = "jdbc:postgresql://localhost/marketplace";
    Properties props = new Properties();
    props.setProperty("user", "marketplace");
    props.setProperty("password", "test");
    return DriverManager.getConnection(url, props);
  }
}
