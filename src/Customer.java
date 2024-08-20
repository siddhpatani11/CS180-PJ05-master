import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

/**
 * A class that stores a User with an email, password, balance, and cart of Products.
 * Contains useful methods to interact with a Customer object.
 *
 * @author Anish Gorentala
 * @author Kian Kishimoto
 * @author Aryan Shahu
 * @version November 18, 2022
 */

public class Customer extends User implements Serializable {
    /**
     * Money balance of the Customer
     */
    private double balance;

    /**
     * List of Products in this Customer's shopping cart
     */
    private ArrayList<Product> cart;

    /**
     * List of Products this Customer has purchased
     */
    private ArrayList<Product> purchaseHistory;


    /**
     * Initialize a new Customer object, calling the super class (User) constructor for email and password.
     * Sets balance field to specified amount.
     * Sets cart to a new empty ArrayList object.
     *
     * @param email    Email of the customer
     * @param password Password of the customer
     * @param balance  Balance of the customer
     */
    public Customer(String email, String password, double balance) {
        super(email, password);
        this.balance = balance;
        this.cart = new ArrayList<>();
        this.purchaseHistory = new ArrayList<>();
    }

    /**
     * @return {@link #cart} field
     */
    public ArrayList<Product> getCart() {
        return cart;
    }

    public ArrayList<Product> getPurchaseHistory() {
        return purchaseHistory;
    }

    /**
     * @return {@link #balance} field
     */
    public double getBalance() {
        return balance;
    }

    /**
     * @param balance The value of the {@link #balance} field to be set
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * @param cart The value of the {@link #cart} field to be set
     */
    public void setCart(ArrayList<Product> cart) {
        this.cart = cart;
    }

    /**
     * Returns a single line, pipe-delimited String representation of this Customer.
     * Follows the format of {@code email|password|balance}
     *
     * @return {@code String} representation of this Customer
     */
    @Override
    public String toString() {
        return getEmail() + "|" + getPassword() + "|" + balance;
    }

    /**
     * Determine whether the passed in Object is equal to this Customer.
     * Returns true if the passed in Object is a Customer with the same email and password as the current Customer.
     * Returns false otherwise.
     *
     * @param obj Object to compare with the current Customer
     * @return Whether this Customer and the passed in Object are equal
     */
    @Override
    public boolean equals(Object obj) {
        // If same exact object, return true
        if (this == obj) {
            return true;
        }

        // If object is not of type User, return false
        if (!(obj instanceof Customer)) {
            return false;
        }

        // Return true if the current and passed in Customer share the same email and password
        Customer temp = (Customer) obj;
        return getEmail().equals(temp.getEmail()) && getPassword().equals(temp.getPassword());
    }

    public boolean addToCart(Product product) {
        int cartIndex = cart.indexOf(product);
        if (cartIndex >= 0) {
            Product productInCart = cart.get(cartIndex);
            productInCart.setQuantity(productInCart.getQuantity() + product.getQuantity());
            return false;
        } else {
            cart.add(product);
            return true;
        }
    }

    public void saveCart() {
        ArrayList<String> fileContents = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("carts.txt"))) {
            String line;
            String lineEmail;
            while ((line = br.readLine()) != null) {
                lineEmail = line.substring(0, line.indexOf("|"));

                if (!getEmail().equals(lineEmail)) {
                    fileContents.add(line);
                }
            }
        } catch (FileNotFoundException ignored) {
            System.out.println("The file carts.txt does not already exist.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter("carts.txt"))) {
            for (String s : fileContents) {
                pw.println(s);
            }
            for (Product product : cart) {
                pw.println(getEmail() + "|" + product);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void viewItems() {
        System.out.println("Your cart:");
        System.out.println("-----------------------------");
        if (cart.isEmpty()) {
            System.out.println("You have no items in your cart!");
        } else {
            for (Product item : cart) {
                System.out.println(item.getName() + ": " + item.getStore().getName() + ", " + item.getDescription()
                        + ", " + item.getPrice() + ", " + item.getQuantity());
            }
        }
    }

    public void checkout() {
        int cost = 0;
        for (Product item : cart) {
            cost += item.getQuantity() * item.getPrice();
            int attemptSell = item.getStore().sellProduct(item, false);
            if (attemptSell < 0) {
                System.out.println("Checkout Failed! The above listed items had issues.");
                return;
            }
        }
        if (cost > balance) {
            System.out.println("Checkout Failed! Your balance is $" + balance + "but total cart cost is $" + cost);
            return;
        }
        for (Product item : cart) {
            item.getStore().sellProduct(item, true);
        }
        balance -= cost;
        System.out.println("Successfully checked out cart of price $" + cost + "! Your new balance is $" + balance);
    }

    @Override
    public void export(String outfile) {

    }

    public void viewStats(ArrayList<String> stats, int dashboard) {
        if (dashboard == 1) {
            if (stats.isEmpty()) {
                System.out.println("There are no statistics for this marketplace yet.");
                return;
            }

            HashMap<String, Integer> storeCounts = new HashMap<>();
            String[] splitString;
            String store;
            int quantity;
            for (String s : stats) {
                splitString = s.split("\\|");
                store = splitString[1];
                quantity = Integer.parseInt(splitString[4]);
                if (storeCounts.containsKey(store)) {
                    storeCounts.put(store, storeCounts.get(store) + quantity);
                } else {
                    storeCounts.put(store, quantity);
                }
            }

            storeCounts.entrySet().stream().sorted(
                    (a, b) -> -a.getValue().compareTo(b.getValue())).forEach(
                    x -> System.out.println(x.getKey() + ": " + x.getValue()
                    )
            );
        } else if (dashboard == 2) {
            HashMap<String, Integer> customerStoreCounts = new HashMap<>();
            String[] splitString;
            String email;
            String store;
            int quantity;
            for (String s : stats) {
                splitString = s.split("\\|");
                email = splitString[0];
                store = splitString[1];
                quantity = Integer.parseInt(splitString[4]);
                if (getEmail().equals(email)) {
                    if (customerStoreCounts.containsKey(store)) {
                        customerStoreCounts.put(store, customerStoreCounts.get(store) + quantity);
                    } else {
                        customerStoreCounts.put(store, quantity);
                    }
                }
            }

            if (customerStoreCounts.isEmpty()) {
                System.out.println("There are no statistics for this customer yet.");
            } else {
                customerStoreCounts.entrySet().stream().sorted(
                        (a, b) -> -a.getValue().compareTo(b.getValue())).forEach(
                        x -> System.out.println(x.getKey() + ": " + x.getValue()
                        )
                );
            }
        }
    }

    public void purchaseHist(ArrayList<String> stats) {
        System.out.println("Purchase History:");

        int counter = 0;
        for (String stat : stats) {
            String statEmail = stat.substring(0, stat.indexOf("|"));
            if (getEmail().equals(statEmail)) {
                System.out.println(stat.substring(stat.indexOf("|") + 1));
                counter++;
            }
        }

        if (counter == 0) {
            System.out.println("You have no purchase history.");
        }
    }
}
