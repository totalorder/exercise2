package se.kth.id2212.ex2.marketplace;

import java.io.Serializable;

public class Item implements Serializable {
  public final String name;
  public final int price;

  public Item(String name, int price) {
    this.name = name;
    this.price = price;
  }

  public String toString() {
    return name + " $" + price;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Item item = (Item) o;

    if (price != item.price) return false;
    return name.equals(item.name);

  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + price;
    return result;
  }
}
