import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class that contains information for, and functions to interact with a Store.
 * Contains information on the Store's name and its products.
 *
 * @author Anish Gorentala
 * @version November 18, 2022
 */
public class Store implements Serializable {
    /**
     * Name of the Store
     */
    private String name;

    /**
     * List of the Products belonging to this Store
     */
    private ArrayList<Product> inventory;

    /**
     * Initialize a new {@code Store} object with the specified name
     *
     * @param name Name of the Store
     */
    public Store(String name) {
        this.name = name;
        this.inventory = new ArrayList<>();
    }

    /**
     * @return {@link #name} field
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The value of the {@link #name} field to be set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return {@link #inventory} field
     */
    public ArrayList<Product> getInventory() {
        return inventory;
    }

    /**
     * Adds the given Product to this store's inventory.
     * If the Product already exists in the inventory, increases quantity.
     *
     * @param product Product to be added to this Store's inventory
     */
    public void addProduct(Product product) {
        if (!inventory.contains(product)) {
            inventory.add(product);
        }
    }

    /**
     * @param inventory The value of the {@link #inventory} field to be set
     */
    public void setInventory(ArrayList<Product> inventory) {
        this.inventory = inventory;
    }

    /**
     * Determine whether the passed in Object is equal to this Store.
     * Returns true if the passed in Object is a Store with the same name as the current Store.
     * Returns true if the passed in Object is a String with the same value as this current Store's name
     * Returns false otherwise.
     *
     * @param obj Object to compare with the current Product
     * @return Whether this Product and the passed in Object are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof String) {
            String temp = (String) obj;
            return this.name.equals(temp);
        } else if (obj instanceof Store) {
            Store temp = (Store) obj;
            return this.name.equals(temp.getName());
        } else {
            return false;
        }
    }

    /**
     * Returns information on whether the given Product can be sold by the current Store.
     * Uses the given {@code sellAmount} as the quantity to attempt to buy.
     * Uses the give {@code buyFlag} to specify whether to actually process the sell transaction.
     * If {@code buyFlag} is {@code true}, takes from stock. If {@code false}, does not take from stock.
     *
     * @param product Product to be sold
     * @param buyFlag Whether to actually take the quantity from the Product's stock
     * @return -2 if the given Product is not in this Store's inventory,
     * -1 if the {@code sellAmount} is more than the amount in stock,
     * and the {@code sellAmount} if the product can be sold in the given amount.
     */
    public int sellProduct(Product product, boolean buyFlag) {
        if (inventory.contains(product)) {
            Product myProduct = inventory.get(inventory.indexOf(product));
            if (myProduct.getQuantity() < product.getQuantity()) {
                System.out.println("There is only " + myProduct.getQuantity()
                        + " of item named \"" + myProduct.getName() + "\" in " + name + "'s stock.");
                return -1;
            } else {
                if (buyFlag) {
                    myProduct.setQuantity(myProduct.getQuantity() - product.getQuantity());
                }
                return myProduct.getQuantity();
            }
        } else {
            System.out.println("Item \"" + product.getName() + "\" is not in " + name + "'s inventory.");
        }
        return -2;
    }
}
