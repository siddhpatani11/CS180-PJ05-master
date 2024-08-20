import java.io.Serializable;

/**
 * Class that contains information for a Product, storing values of name, store, description, quantity, and price.
 *
 * @author Anish Gorentala
 * @author Kian Kishimoto
 * @version November 13, 2022
 */
public class Product implements Serializable {
    /**
     * Name of the Product
     */
    private String name;

    /**
     * Store that the Product belongs to
     */
    private Store store;

    /**
     * String of the Product's description
     */
    private String description;

    /**
     * Quantity of the Product
     */
    private int quantity;

    /**
     * Price of the Product
     */
    private double price;

    /**
     * Initialize a new {@code Product} object with the specified properties
     *
     * @param store       Store that the Product belongs to
     * @param name        Name of the Product
     * @param description Description of the Product
     * @param quantity    Quantity of the Product
     * @param price       Price of the Product
     */
    public Product(Store store, String name, String description, int quantity, double price) {
        this.store = store;
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
    }

    /**
     * Initialize a Product with parameters parsed from the given String
     *
     * @param productString The String representation of the Product to parse and initialize
     */
    public Product(String productString) {
        String[] splitString = productString.split("\\|");
        this.store = new Store(splitString[0]);
        this.name = splitString[1];
        this.description = splitString[2];
        this.quantity = Integer.parseInt(splitString[3]);
        this.price = Double.parseDouble(splitString[4]);
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
     * @return {@link #price} field
     */
    public double getPrice() {
        return price;
    }

    /**
     * @return {@link #quantity} field
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @return {@link #description} field
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return {@link #store} field
     */
    public Store getStore() {
        return store;
    }

    /**
     * @param quantity The value of the {@link #quantity} field to be set
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * @param description The value of the {@link #description} field to be set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param price The value of the {@link #price} field to be set
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * @param store The value of the {@link #store} field to be set
     */
    public void setStore(Store store) {
        this.store = store;
    }

    /**
     * Returns a single line, pipe-delimited String representation of this Product.
     * Follows the format of {@code store|name|description|quantity|price}
     *
     * @return {@code String} representation of this Product
     */
    @Override
    public String toString() {
        return store.getName() + "|" + name + "|" + description + "|" + quantity + "|" + price;
    }

    /**
     * Determine whether the passed in Object is equal to this Product.
     * Returns true if the passed in Object is a Product with the same store and name as the current Product.
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

        if (!(obj instanceof Product)) {
            return false;
        }

        Product temp = (Product) obj;
        return (this.store.equals(temp.getStore())) && this.name.equals(temp.getName());
    }
}
