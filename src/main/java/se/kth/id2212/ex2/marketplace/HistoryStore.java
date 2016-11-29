package se.kth.id2212.ex2.marketplace;

import se.kth.id2212.ex2.db.Tx;

import java.util.List;
import java.util.Map;

public class HistoryStore {
  public void incrementSold(final Tx tx, final String username) {
    final History history = getOrCreateHistory(tx, username);
    tx.update("UPDATE history SET items_sold = ? WHERE username = ?", history.itemsSold + 1, username);
  }

  public void incrementBought(final Tx tx, final String username) {
    final History history = getOrCreateHistory(tx, username);
    tx.update("UPDATE history SET items_bought = ? WHERE username = ?", history.itemsBought + 1, username);
  }

  public History getOrCreateHistory(final Tx tx, final String username) {
    final List<Map<String, Object>> history = tx.select("SELECT * FROM history WHERE username = ?", username);
    if (history.size() == 0) {
      tx.update("INSERT INTO history VALUES (?, ?, ?)", username, 0, 0);
      return new History(0, 0);
    }
    return new History((int)history.get(0).get("items_sold"), (int)history.get(0).get("items_bought"));
  }
}
