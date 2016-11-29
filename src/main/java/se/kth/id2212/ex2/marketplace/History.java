package se.kth.id2212.ex2.marketplace;

import java.io.Serializable;

public class History implements Serializable {
  public final int itemsSold;
  public final int itemsBought;

  public History(final int itemsSold, final int itemsBought) {
    this.itemsSold = itemsSold;
    this.itemsBought = itemsBought;
  }

  public String toString() {
    return String.format("items sold: %d, items sold: %d", itemsSold, itemsBought);
  }
}
