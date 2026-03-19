package filemanager;

/**
 * Represents a food item in the menu.
 * Contains name and price.
 */
public class FoodItem {

    private String name;
    private double price;

    public FoodItem(String name, double price) {
        this.name = name;
        this.price = price;
    }

    // Getters
    public String getName() { return name; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return name + " - ₱" + price;
    }
}