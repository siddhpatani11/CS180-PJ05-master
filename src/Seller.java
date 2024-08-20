import java.io.*;
import java.util.*;

/**
 * Class that contains information for and functions to interact with a Seller.
 *
 * @author Anish Gorentala
 * @author Kian Kishimoto
 * @author Siddh Patani
 * @version November 18, 2022
 */
public class Seller extends User implements Serializable {
    /**
     * List of all Stores belonging to this Seller.
     */
    private ArrayList<Store> stores;

    /**
     * Initialize a new {@code Seller} object with the specified email and password,
     * as well as an empty ArrayList of Stores.
     *
     * @param email    Email of the seller
     * @param password Password of the seller
     */
    public Seller(String email, String password) {
        super(email, password);
        this.stores = new ArrayList<>();
    }

    /**
     * Returns the ArrayList of Stores belonging to this seller.
     *
     * @return {@link #stores} field
     */
    public ArrayList<Store> getStores() {
        return stores;
    }

    /**
     * Adds the given Store to this Sellers list of Stores.
     *
     * @param store The new Store to be added
     */
    public void addStore(Store store) {
        this.stores.add(store);
    }

    /**
     * Returns a single line, pipe-delimited String representation of this Seller.
     * Follows the format of {@code email|password|store1;store2;store3}
     *
     * @return {@code String} representation of this Seller
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Store s : stores) {
            builder.append(s.getName());
            if (stores.indexOf(s) != stores.size() - 1) {
                builder.append(";");
            }
        }
        return getEmail() + "|" + getPassword() + "|" + builder;
    }

    /**
     * Print info on every Product owned by this Seller, across all of its Stores, to the specified file.
     *
     * @param outfile Path of file to write info to
     */
    @Override
    public void export(String outfile) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(outfile))) {
            stores.forEach(store -> store.getInventory().forEach(pw::println));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print out information on all products currently in a shopping cart
     */
    public void viewInCarts() {
        try (BufferedReader br = new BufferedReader(new FileReader("carts.txt"))) {
            String line;
            int counter = 0;
            while ((line = br.readLine()) != null) {
                if (stores.contains(new Store(line.substring(0, line.indexOf("|"))))) {
                    System.out.println(line);
                    counter++;
                }
            }
            System.out.println("There are currently " + counter + " items in customer shopping carts.");
        } catch (FileNotFoundException ignored) {
            System.out.println("A carts.txt file does not exist." +
                               " This could mean that there are no items in customer shopping carts.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void viewStats(ArrayList<String> stats, int dashboardSortOption) {
        ArrayList<String> relLines = new ArrayList<>();
        for (String stat : stats) {
            Product p = new Product(stat.substring(stat.indexOf("|") + 1));
            if (stores.contains(p.getStore())) {
                relLines.add(stat);
            }
        }

        if (dashboardSortOption == 1) {
            //Arraylist of unique products
            ArrayList<Product> products = new ArrayList<>();
            //for loop through relevant lines
            for (String relLine : relLines) {
                //check if product is already in array
                boolean exists = false;
                Product p = new Product(relLine.substring(relLine.indexOf("|") + 1));
                for (Product product : products) {
                    if (product.equals(p)) {
                        exists = true;
                        break;
                    }
                }
                //if not in array add to array
                if (!exists) {
                    products.add(p);
                    //If in array add the quantity to the product already in array
                } else {
                    int index = 0;
                    for (int z = 0; z < products.size(); z++) {
                        if (products.get(z).equals(p)) {
                            index = z;
                        }
                    }
                    products.get(index).setQuantity(products.get(index).getQuantity() + p.getQuantity());
                }
            }
            //sort with custom method sorting based on getQuantity()
            products.sort(Comparator.comparing(Product::getQuantity).reversed());
            //Print out product name and quantity
            System.out.println("Products sorted by decreasing quantity purchased:");
            for (Product product : products) {
                System.out.println("Product: " + product.getName() + " | Quantity: " + product.getQuantity());
            }

        } else if (dashboardSortOption == 2) {
            //Unsorted customer and quantity array
            ArrayList<String> customer = new ArrayList<>();
            ArrayList<Integer> quantity = new ArrayList<>();
            //Find unique customers and add to arraylist
            for (String relLine : relLines) {
                boolean exists = false;
                Product p = new Product(relLine.substring(relLine.indexOf("|") + 1));
                for (String s : customer) {
                    if (s.equals(relLine.substring(0, relLine.indexOf("|")))) {
                        exists = true;
                        break;
                    }
                }
                //Customer has same index as its quantity, so we can access both with same index
                if (!exists) {
                    customer.add(relLine.substring(0, relLine.indexOf("|")));
                    quantity.add(p.getQuantity());
                } else {
                    for (int z = 0; z < customer.size(); z++) {
                        if (customer.get(z).equals(relLine.substring(0, relLine.indexOf("|")))) {
                            quantity.set(z, quantity.get(z) + p.getQuantity());
                        }
                    }
                }
            }
            //Sorted Arraylists
            ArrayList<String> sortedCustomer = new ArrayList<>();
            //copy quantity to quantitySorted
            ArrayList<Integer> quantitySorted = new ArrayList<>(quantity);
            //Sort quantity by Descending
            quantitySorted.sort(Collections.reverseOrder());
            for (Integer integer : quantitySorted) {
                for (int j = 0; j < customer.size(); j++) {
                    if (integer.equals(quantity.get(j))) {
                        sortedCustomer.add(customer.get(j));
                        break;
                    }
                }
            }
            for (int i = 0; i < sortedCustomer.size(); i++) {
                System.out.println("Customer: " + sortedCustomer.get(i) + " | Quantity: " + quantitySorted.get(i));
            }
        }
    }
}
