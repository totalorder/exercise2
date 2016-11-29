package se.kth.id2212.ex2;

import org.junit.Test;
import se.kth.id2212.ex2.db.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class JdbcUtilTest {
  @Test
  public void connects() throws SQLException {
    Connection conn = JdbcUtil.getConnection();
  }

  @Test
  public void selects() throws SQLException {
    Connection conn = JdbcUtil.getConnection();
    PreparedStatement st = conn.prepareStatement("SELECT ?");
    st.setInt(1, 123);
    ResultSet rs = st.executeQuery();
            rs.next();
    int result = rs.getInt(1);

    assertEquals(123, result);

    rs.close();
    st.close();
  }
}