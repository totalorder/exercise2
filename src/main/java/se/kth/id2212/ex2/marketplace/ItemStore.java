package se.kth.id2212.ex2.marketplace;

import se.kth.id2212.ex2.db.Tx;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemStore {
  public void createSale(final Tx tx, final String username, final String bankName, final Item item) {
    tx.update("INSERT INTO item VALUES (?, ?, ?, ?)", username, bankName, item.name, item.price);
  }

  public List<Item> listItems(final Tx tx) {
    return tx.select("SELECT * FROM item").stream()
        .map(row -> new Item((String)row.get("name"), (int)row.get("price")))
        .collect(Collectors.toList());
  }

  public Seller purchaseItem(final Tx tx, final Item item) {
    return tx.select(
        "DELETE FROM item WHERE name = ? AND price = ? RETURNING seller, bank_name", item.name, item.price)
        .stream()
        .map(row -> new Seller((String) row.get("seller"), (String) row.get("bank_name")))
        .findFirst()
        .orElseGet(() -> null);
  }
}
