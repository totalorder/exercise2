package se.kth.id2212.ex2.marketplace;

import se.kth.id2212.ex2.db.Tx;

public class UserStore {
  public boolean createUser(final Tx tx, final String username, final String hashedPassword) {
    if (tx.select("SELECT * FROM \"user\" WHERE username = ?", username).size() > 0) {
      return false;
    }

    tx.update("INSERT INTO \"user\" VALUES (?, ?)", username, hashedPassword);
    return true;
  }

  public boolean exists(final Tx tx, final String username, final String hashedPassword) {
    return tx.select("SELECT * FROM \"user\" WHERE username = ? AND password = ?", username, hashedPassword).size() == 1;
  }
}
