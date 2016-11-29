package se.kth.id2212.ex2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.kth.id2212.ex2.db.JdbcUtil;
import se.kth.id2212.ex2.db.Tx;
import se.kth.id2212.ex2.db.TransactionManager;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TransactionManagerTest {


  private TransactionManager transactionManager;
  private Tx tx = null;

  @Before
  public void setUp() throws Exception {
    Connection connection = JdbcUtil.getConnection();
    transactionManager = new TransactionManager(connection);
  }

  @After
  public void tearDown() throws Exception {
    if (tx != null) {
      tx.rollback();
    }
    transactionManager.close();
  }

  @Test
  public void insertsAndSelects() throws Exception {
    tx = transactionManager.open();
    tx.execute("CREATE TABLE test (num INT, str TEXT);");
    tx.update("INSERT INTO test VALUES (?, ?);", 123, "hello");
    tx.update("INSERT INTO test VALUES (?, ?);", 456, "asdf");
    final List<Map<String, Object>> result = tx.select("SELECT * FROM test");
    assertEquals(2, result.size());
    assertEquals(123, result.get(0).get("num"));
    assertEquals("hello", result.get(0).get("str"));
  }
}