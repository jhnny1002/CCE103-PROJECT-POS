package filemanager;

import java.io.*;
import java.util.ArrayList;

/**
 * FileManager handles saving, reading, and clearing order data from a file.
 * Each checkout is saved with the order type (Dine In / Take Out) and totals.
 */
public class FileManager {

    private static final String FILE_NAME = "orders.txt";

    /**
     * Saves a list of orders to the file.
     * Each checkout creates a separate section with total and order type.
     *
     * @param orders    ArrayList of OrderItem objects to save
     * @param orderType "Dine In" or "Take Out"
     */
    public static void saveOrders(ArrayList<OrderItem> orders, String orderType) {
        if (orders == null || orders.isEmpty()) return;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write("----- NEW ORDER (" + orderType + ") -----\n");

            double total = 0;
            for (OrderItem order : orders) {
                writer.write(order.getFood().getName() + "," +
                        order.getFood().getPrice() + "," +
                        order.getQuantity() + "," +
                        order.getSubtotal() + "\n");
                total += order.getSubtotal();
            }

            writer.write("TOTAL: " + total + "\n");
            writer.write("---------------------\n");

        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
    }

    /**
     * Reads all orders from the file.
     * Can be used to display previous receipts.
     *
     * @return contents of orders.txt as a String
     */
    public static String readOrders() {
        StringBuilder sb = new StringBuilder();
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return "No previous orders found.\n";
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            return "Error reading file: " + e.getMessage() + "\n";
        }

        return sb.toString();
    }

    /**
     * Clears the orders file.
     * Could be used for resetting all saved records.
     */
    public static void clearOrders() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            writer.write(""); // empty the file
        } catch (IOException e) {
            System.err.println("Error clearing file: " + e.getMessage());
        }
    }
}