package filemanager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * POSframe is the main graphical user interface for the POS system.
 * Allows adding orders, deleting orders, checkout, and viewing previous orders.
 */
public class POSframe extends JFrame {

    // GUI components
    JTable table;
    DefaultTableModel model;
    JTextArea receipt;
    JLabel totalLabel;

    // Stores current orders
    ArrayList<OrderItem> orders = new ArrayList<>();
    double total = 0;
    private boolean receiptFinalized = false;

    // Tracks whether current order is Dine In or Take Out
    private String orderType = "Dine In";

    public POSframe() {
        setTitle("Fast Food POS System");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Prompt user for Dine In / Take Out selection
        promptOrderType();

        // Create menu tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Breakfast", createMenuPanel(new FoodItem[]{
                new FoodItem("Pancake Meal", 90),
                new FoodItem("Breakfast Burger", 95),
                new FoodItem("Egg & Rice", 80)
        }));
        tabs.add("Kids Meal", createMenuPanel(new FoodItem[]{
                new FoodItem("Kids Spaghetti", 70),
                new FoodItem("Kids Chicken", 75),
                new FoodItem("Kids Burger", 65)
        }));
        tabs.add("Super Meal", createMenuPanel(new FoodItem[]{
                new FoodItem("Chicken + Spaghetti", 150),
                new FoodItem("Burger + Fries", 140),
                new FoodItem("Chicken + Burger Steak", 160)
        }));
        tabs.add("Mix & Match", createMenuPanel(new FoodItem[]{
                new FoodItem("Burger + Fries", 110),
                new FoodItem("Spaghetti + Drink", 100),
                new FoodItem("Chicken + Rice", 130)
        }));
        tabs.add("Drinks", createMenuPanel(new FoodItem[]{
                new FoodItem("Coke", 40),
                new FoodItem("Sprite", 40),
                new FoodItem("Iced Tea", 45)
        }));

        // Table to display current orders
        model = new DefaultTableModel(
                new String[]{"Food", "Price", "Qty", "Subtotal"}, 0);
        table = new JTable(model);
        JScrollPane tableScroll = new JScrollPane(table);

        // Receipt display
        receipt = new JTextArea(12, 35);
        receipt.setEditable(false);
        JScrollPane receiptScroll = new JScrollPane(receipt);

        totalLabel = new JLabel("Total: ₱0");

        // Buttons
        JButton deleteBtn = new JButton("Delete Order");
        JButton checkoutBtn = new JButton("Checkout");
        JButton previousBtn = new JButton("Previous Orders");

        // Delete selected order
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                total -= orders.get(row).getSubtotal();
                orders.remove(row);
                model.removeRow(row);
                updateReceipt();
            }
        });

        // Checkout and payment
        checkoutBtn.addActionListener(e -> {
            if (orders.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No orders yet.");
                return;
            }

            try {
                String paymentStr = JOptionPane.showInputDialog(
                        this,
                        "Total Amount: ₱" + total + "\nEnter payment:");
                if (paymentStr == null) return;

                double payment = Double.parseDouble(paymentStr);

                if (payment < total) {
                    JOptionPane.showMessageDialog(this, "Insufficient payment.");
                    return;
                }

                double change = payment - total;

                // Append payment info to receipt
                String finalReceipt = receipt.getText();
                finalReceipt += "\n----------------------------\n";
                finalReceipt += "Payment: ₱" + payment + "\n";
                finalReceipt += "Change: ₱" + change + "\n";
                finalReceipt += "THANK YOU!\n";
                receipt.setText(finalReceipt);

                JOptionPane.showMessageDialog(this, "Payment successful!");

                // Save orders with the selected order type
                FileManager.saveOrders(orders, orderType);

                // Clear current orders
                orders.clear();
                model.setRowCount(0);
                total = 0;
                totalLabel.setText("Total: ₱0");
                receiptFinalized = true;

                // Prompt for new order type
                promptOrderType();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid payment input.");
            }
        });

        // Show previous orders
        previousBtn.addActionListener(e -> {
            String previous = FileManager.readOrders();
            JTextArea previousArea = new JTextArea(previous, 20, 40);
            previousArea.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(previousArea),
                    "Previous Orders", JOptionPane.INFORMATION_MESSAGE);
        });

        // Left button panel
        JPanel leftButtons = new JPanel();
        leftButtons.setLayout(new GridLayout(3, 1, 5, 5));
        leftButtons.add(deleteBtn);
        leftButtons.add(checkoutBtn);
        leftButtons.add(previousBtn);

        // Bottom panel for receipt
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(receiptScroll, BorderLayout.CENTER);
        bottom.add(totalLabel, BorderLayout.SOUTH);

        // Add components to main frame
        add(tabs, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
        add(leftButtons, BorderLayout.WEST);
        add(bottom, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * Creates a menu panel with a JComboBox of food items, quantity spinner, and Add button.
     */
    private JPanel createMenuPanel(FoodItem[] items) {
        JPanel panel = new JPanel();

        JComboBox<FoodItem> menuBox = new JComboBox<>(items);
        JSpinner qty = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        JButton addBtn = new JButton("Add Order");

        // Add order to current list
        addBtn.addActionListener(e -> {
            if (receiptFinalized) {
                receipt.setText("");
                receiptFinalized = false;
            }

            FoodItem food = (FoodItem) menuBox.getSelectedItem();
            int quantity = (int) qty.getValue();

            OrderItem order = new OrderItem(food, quantity);
            orders.add(order);

            model.addRow(new Object[]{food.getName(), food.getPrice(), quantity, order.getSubtotal()});

            total += order.getSubtotal();
            updateReceipt();
        });

        panel.add(new JLabel("Item"));
        panel.add(menuBox);
        panel.add(new JLabel("Qty"));
        panel.add(qty);
        panel.add(addBtn);

        return panel;
    }

    /**
     * Updates the receipt display to show current orders, totals, and order type.
     */
    private void updateReceipt() {
        if (receiptFinalized) return;

        StringBuilder text = new StringBuilder();
        text.append("========= RECEIPT =========\n");
        text.append("Order Type: ").append(orderType).append("\n\n");

        for (OrderItem o : orders) {
            text.append(o.getQuantity())
                    .append(" x ")
                    .append(o.getFood().getName())
                    .append(" - ₱")
                    .append(o.getSubtotal())
                    .append("\n");
        }

        text.append("----------------------------\n");
        text.append("TOTAL: ₱").append(total);

        receipt.setText(text.toString());
        totalLabel.setText("Total: ₱" + total);
    }

    /**
     * Prompts the user to select Dine In or Take Out.
     * Updates window title and order type for the session.
     */
    private void promptOrderType() {
        String[] options = {"Dine In", "Take Out"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Select Order Type:",
                "Order Type",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice != -1) {
            orderType = options[choice];
        } else {
            orderType = "Dine In";
        }
        setTitle("Fast Food POS System - " + orderType);
    }

    public static void main(String[] args) {
        new POSframe();
    }
}