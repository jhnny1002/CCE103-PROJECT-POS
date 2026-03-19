package filemanager;

/**
 * Represents a single order item.
 * Combines a FoodItem and a quantity.
 */
public class OrderItem {

    private FoodItem food;
    private int quantity;

    public OrderItem(FoodItem food, int quantity) {
        this.food = food;
        this.quantity = quantity;
    }

    // Getters
    public FoodItem getFood() { return food; }
    public int getQuantity() { return quantity; }

    // Calculates subtotal for this order item
    public double getSubtotal() {
        return food.getPrice() * quantity;
    }
}