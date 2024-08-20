import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * The client that will be handling input output with
 * the user and communication with the server. Send
 * inputs to server and receives + displays processed data from
 * server.
 */
public class StoreClient {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    /**
     * The main method that will run the client.
     *
     * @param args Arguments from the command line.
     */
    public static void main(String[] args) {
        StoreClient storeClient = new StoreClient();
        storeClient.runStore();
        try {
            storeClient.output.close();
            storeClient.input.close();
            storeClient.socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor for class StoreClient
     */
    public StoreClient() {
        try {
            socket = new Socket(InetAddress.getByName("localhost"), 1337);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that runs the IO for the store through a GUI.
     */
    public void runStore() {
        String[] loginOptions = {"Login", "Make New Account"};
        String response;
        int loginOrMakeNew = JOptionPane.showOptionDialog(null,
                "Welcome! Would you like to login to an existing account or make new account?",
                "Store Client", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                loginOptions, loginOptions[0]);
        if (isExit(loginOrMakeNew))
            return;
        if (loginOrMakeNew == 0) {
            if (login()) {
                send("EXIT");
                return;
            }
        } else if (makeNewAccount()) {
                send("EXIT");
                return;
        }
        send("USERTYPE");
        try {
            response = (String) input.readObject();
            switch (response) {
                case "CUSTOMER" -> {
                    customerUI();
                }
                case "SELLER" -> {
                    sellerUI();
                }
                default -> System.out.println("SOMETHING WENT SERIOUSLY WRONG SERVERSIDE!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        send("EXIT");
    }

    /**
     * Runs IO for a seller through a GUI
     */
    public void sellerUI() {
        String[] options = new String[]{"Add Store", "View/Edit Your Stores", "View Sales", "View Customer Carts",
        "Import From File", "Export to File"};
        while (true) {
            int selection = JOptionPane.showOptionDialog(null, "What would you like to do?",
                    "Seller Client", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    options, options[0]);
            if (isExit(selection)) {
                send("EXIT");
                return;
            }
            switch (selection) {
                case 0 -> {
                    boolean valid = false;
                    while (!valid) {
                        String name = JOptionPane.showInputDialog(null, "What is the name of the store you would" +
                                " like to add?", "Seller Client", JOptionPane.QUESTION_MESSAGE);
                        if (isExit(name)) {
                            break;
                        }
                        send("ADDSTORE", name);
                        try {
                            String response = (String) input.readObject();
                            switch (response) {
                                case "FREE" -> {
                                    JOptionPane.showMessageDialog(null, "You have successfully added your store!"
                                    , "Seller Client", JOptionPane.INFORMATION_MESSAGE);
                                    valid = true;
                                }
                                case "TAKEN" ->
                                    JOptionPane.showMessageDialog(null, "A store with this name already exists :("
                                            , "Seller Client", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                case 1 -> {
                    send("GETSTORES");
                    try {
                        Store[] response = (Store[]) input.readObject();
                        if (response.length == 0) {
                            JOptionPane.showMessageDialog(null, "There are no stores in " +
                                            "this account.", "Store Client",
                                    JOptionPane.INFORMATION_MESSAGE);
                            break;
                        }
                        String[] choices = new String[response.length];
                        String[] secondChoices = new String[]{"Add a Product", "Remove a Product", "Remove This Store"};
                        for (int i = 0 ; i < response.length ; i++) {
                            choices[i] = response[i].getName();
                        }
                        String store = (String) JOptionPane.showInputDialog(null, "Choose a store to edit: ",
                                "Seller Client", JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
                        if (isExit(store)) {
                            break;
                        }
                        int choice = JOptionPane.showOptionDialog(null, "What would you like to do?",
                                "Seller Client", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                                secondChoices, secondChoices[0]);
                        if (isExit(choice)) {
                            break;
                        }
                        switch (choice) {
                            case 0 -> {
                                boolean valid = false;
                                while (!valid) {
                                    String[] data = null;
                                    ProductFrame pf = new ProductFrame(store);
                                    pf.setVisible(true);
                                    while (pf.isVisible() && data == null) {
                                        data = pf.getData();
                                    }
                                    if (pf.isVisible())
                                        pf.setVisible(false);
                                    else
                                        break;
                                    if (checkData(data)) {
                                        continue;
                                    }
                                    send("ADDPRODUCT", data);
                                    try {
                                        String str = (String) input.readObject();
                                        switch (str) {
                                            case "SUCCESSFUL" -> {
                                                valid = true;
                                                JOptionPane.showMessageDialog(null, "You have successfully" +
                                                                " added your product to the store!", "Store Client",
                                                        JOptionPane.INFORMATION_MESSAGE);
                                            }
                                            case "TAKEN" -> {
                                                JOptionPane.showMessageDialog(null, "This product already " +
                                                                "exists in the store :(", "Store Client",
                                                        JOptionPane.INFORMATION_MESSAGE);
                                            }
                                            case "INVALID" -> {
                                                System.out.println("SHIT HIT THE FUCKING FAN");
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            case 1 -> {
                                send("GETPRODUCTS");
                                try {
                                    String[] products = (String[]) input.readObject();
                                    if (products == null || products.length == 0) {
                                        JOptionPane.showMessageDialog(null, "There are no products in " +
                                                        "this store.", "Store Client",
                                                JOptionPane.INFORMATION_MESSAGE);
                                        break;
                                    }
                                    String prod = (String) JOptionPane.showInputDialog(null, "Choose a product" +
                                                    " to delete: ", "Seller Client", JOptionPane.PLAIN_MESSAGE, null, products,
                                            products[0]);
                                    send("REMOVEPRODUCT", new String[]{store, prod});
                                    try {
                                        String str = (String) input.readObject();
                                        switch (str) {
                                            case "SUCCESSFUL" -> {
                                                JOptionPane.showMessageDialog(null, "You have successfully" +
                                                                " removed \"" + prod + "\" :)", "Store Client",
                                                        JOptionPane.INFORMATION_MESSAGE);
                                            }
                                            case "UNSUCCESSFUL" -> {
                                                JOptionPane.showMessageDialog(null, "There was an issue" +
                                                                " with removing \"" + prod + "\" :(", "Store Client",
                                                        JOptionPane.INFORMATION_MESSAGE);
                                                System.out.println("SHIT HIT THE FUCKING FAN");
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            case 2 -> {
                                send("REMOVESTORE", store);
                                try {
                                    String str = (String) input.readObject();
                                    switch (str) {
                                        case "SUCCESSFUL" -> {
                                            JOptionPane.showMessageDialog(null, "You have successfully" +
                                                            " removed \"" + store + "\" :)", "Store Client",
                                                    JOptionPane.INFORMATION_MESSAGE);
                                        }
                                        case "UNSUCCESSFUL" -> {
                                            JOptionPane.showMessageDialog(null, "There was an issue" +
                                                            " with removing \"" + store + "\" :(", "Store Client",
                                                    JOptionPane.INFORMATION_MESSAGE);
                                            System.out.println("SHIT HIT THE FUCKING FAN");
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                case 2 -> {
                    String[] selections = new String[]{"Most Sales", "By Customers"};
                    int choice = JOptionPane.showOptionDialog(null, "Would you like your sales sorted" +
                            "by stores with most sales or by which customers bought the most products?", "Seller Client",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                            selections, selections[0]);
                    if (isExit(choice)) {
                        break;
                    }
                    send("VIEWSALES", choice);
                    try {
                        ArrayList<String> response = (ArrayList<String>) input.readObject();
                        StringBuilder out = new StringBuilder();
                        for (String str : response) {
                            out.append(str).append("\n");
                        }
                        JOptionPane.showMessageDialog(null, out, "Seller Client", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                case 3 -> {
                    send("VIEWINCARTS");
                    try {
                        String response = (String) input.readObject();
                        JOptionPane.showMessageDialog(null, "There are currently " + response + " of your items in " +
                                "customer shopping carts.", "Seller Client", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                case 4 -> {
                    boolean valid = false;
                    while (!valid) {
                        String fileName = JOptionPane.showInputDialog(null, "What is the name of the file you would" +
                                " like to import from?", "Seller Client", JOptionPane.QUESTION_MESSAGE);
                        if (isExit(fileName)) {
                            break;
                        }
                        send("IMPORT", fileName);
                        try {
                            String response = (String) input.readObject();
                            switch (response) {
                                case "VALID" -> {
                                    valid = true;
                                    JOptionPane.showMessageDialog(null, "You have successfully imported from "
                                                    + fileName + "!", "Seller Client", JOptionPane.INFORMATION_MESSAGE);
                                }
                                case "INVALID" ->
                                    JOptionPane.showMessageDialog(null, "You have entered an invalid file name :("
                                            , "Seller Client", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                case 5 -> {
                    boolean valid = false;
                    while (!valid) {
                        String fileName = JOptionPane.showInputDialog(null, "What is the name of the file you would" +
                                " like to export to?", "Seller Client", JOptionPane.QUESTION_MESSAGE);
                        if (isExit(fileName)) {
                            break;
                        }
                        send("EXPORT", fileName);
                        try {
                            String response = (String) input.readObject();
                            switch (response) {
                                case "VALID" -> {
                                    valid = true;
                                    JOptionPane.showMessageDialog(null, "You have successfully exported to "
                                            + fileName + "!", "Seller Client", JOptionPane.INFORMATION_MESSAGE);
                                }
                                case "INVALID" ->
                                    JOptionPane.showMessageDialog(null, "You have entered an invalid file name :("
                                            , "Seller Client", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }                }
            }
        }
    }

    /**
     * Takes in product data and checks to see if it is in the right format.
     * @param data The data being checked
     * @return true if the data is bad; otherwise,
     *         false
     */
    public boolean checkData(String[] data) {
        if (emptyOrContainsBadChar(data[1]) || emptyOrContainsBadChar(data[2])) {
            JOptionPane.showMessageDialog(null, "One or more of the fields is " +
                            "invalid. The name and description cannot contain \"|\" and must be " +
                            "at least one character long. The price and quantity must be non-negative " +
                            "numbers. The price must be an integer.", "Store Client",
                    JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        try {
            double q = Double.parseDouble(data[4]);
            int p =  Integer.parseInt(data[3]);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "One or more of the fields is " +
                            "invalid. The name and description cannot contain \"|\" and must be " +
                            "at least one character long. The price and quantity must be non-negative " +
                            "numbers. The price must be an integer.", "Store Client",
                    JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        return false;
    }

    /**
     * Runs IO for a customer through a GUI
     */
    public void customerUI() {
        String[] options = new String[]{"View Overall Marketplace", "Search Marketplace", "View Purchase History",
                "Add to Balance", "View Cart", "Purchase Cart"};
        while (true) {
            int selection = JOptionPane.showOptionDialog(null, "What would you like to do?",
                    "Customer Menu", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    options, options[0]);
            if (isExit(selection)) {
                send("EXIT");
                return;
            }
            switch (selection) {
                case 0 -> {
                    try {
                        boolean valid = false;
                        while (!valid) {
                            String[] sortOptions = {"Quantity", "Price"};
                            int sort = JOptionPane.showOptionDialog(null, "How would you like" +
                                            " the marketplace to be sorted?", "Customer Menu",
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                    null, sortOptions, sortOptions[0]);
                            if (isExit(sort)) {
                                break;
                            }
                            switch (sort) {
                                case 0 -> {
                                    send("VIEWMARKET", "QUANTITY");
                                    Product[] response = (Product[]) input.readObject();
                                    if (response.length == 0) {
                                        JOptionPane.showMessageDialog(null, "There are no" +
                                                        " products in the marketplace currently.", "Customer Menu",
                                                JOptionPane.INFORMATION_MESSAGE);
                                        break;
                                    }
                                    String[] choices = new String[response.length];
                                    for (int i = 0 ; i < response.length ; i++) {
                                        choices[i] = response[i].getName();
                                    }
                                    int prodnumber = (Integer) JOptionPane.showInputDialog(null,
                                            "Choose a product to add to cart: ",
                                            "Customer Menu", JOptionPane.PLAIN_MESSAGE, null, choices,
                                            choices[0]);
                                    if (isExit(prodnumber)) {
                                        break;
                                    } else {
                                        send("ADDTOCART", response[prodnumber]);
                                        String response2 = (String) input.readObject();
                                        if (response2.equals("SUCCESSFUL")) {
                                            JOptionPane.showMessageDialog(null, "Product " +
                                                            "successfully added to cart!", "Customer Menu",
                                                    JOptionPane.INFORMATION_MESSAGE);
                                        }
                                        else {
                                            JOptionPane.showMessageDialog(null, "Product " +
                                                            "couldn't be added to cart, it just went out of stock.",
                                                    "Customer Menu", JOptionPane.INFORMATION_MESSAGE);
                                        }
                                    }
                                }
                                case 1 -> {
                                    send("VIEWMARKET", "PRICE");
                                    Product[] response = (Product[]) input.readObject();
                                    if (response.length == 0) {
                                        JOptionPane.showMessageDialog(null, "There are no" +
                                                        " products in the marketplace currently.", "Customer Menu",
                                                JOptionPane.INFORMATION_MESSAGE);
                                        break;
                                    }
                                    String[] choices = new String[response.length];
                                    for (int i = 0 ; i < response.length ; i++) {
                                        choices[i] = response[i].getName();
                                    }
                                    int prodnumber = (Integer) JOptionPane.showInputDialog(null,
                                            "Choose a product to add to cart: ",
                                            "Customer Menu", JOptionPane.PLAIN_MESSAGE, null, choices,
                                            choices[0]);
                                    if (isExit(prodnumber)) {
                                        break;
                                    } else {
                                        send("ADDTOCART", response[prodnumber]);
                                        String response2 = (String) input.readObject();
                                        if (response2.equals("SUCCESSFUL")) {
                                            JOptionPane.showMessageDialog(null, "Product " +
                                                            "successfully added to cart!", "Customer Menu",
                                                    JOptionPane.INFORMATION_MESSAGE);
                                        }
                                        else {
                                            JOptionPane.showMessageDialog(null, "Product " +
                                                            "couldn't be added to cart, it just went out of stock.",
                                                    "Customer Menu", JOptionPane.INFORMATION_MESSAGE);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                case 1 -> {
                    try {
                        boolean valid = false;
                        while (!valid) {
                            String query = JOptionPane.showInputDialog(null, "What would you" +
                                    " like to search the marketplace by?", "Customer Menu",
                                    JOptionPane.QUESTION_MESSAGE);
                            if (isExit(query)) {
                                break;
                            }
                            String[] sortOptions = {"Quantity", "Price"};
                            int sort = JOptionPane.showOptionDialog(null, "How would you like" +
                                            " the searched items to be sorted?", "Customer Menu",
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                    null, sortOptions, sortOptions[0]);
                            if (isExit(sort)) {
                                break;
                            }
                            switch (sort) {
                                case 0 -> {
                                    String[] searchArray = {query, "QUANTITY"};
                                    send("SEARCH", searchArray);
                                    Product[] response = (Product[]) input.readObject();
                                    if (response.length == 0) {
                                        JOptionPane.showMessageDialog(null, "There are no" +
                                                        " products in the marketplace by that search.",
                                                "Customer Menu", JOptionPane.INFORMATION_MESSAGE);
                                        break;
                                    }
                                    String[] choices = new String[response.length];
                                    for (int i = 0 ; i < response.length ; i++) {
                                        choices[i] = response[i].getName();
                                    }
                                    int prodnumber = (Integer) JOptionPane.showInputDialog(null,
                                            "Choose a product to add to cart: ",
                                            "Customer Menu", JOptionPane.PLAIN_MESSAGE, null, choices,
                                            choices[0]);
                                    if (isExit(prodnumber)) {
                                        break;
                                    } else {
                                        send("ADDTOCART", response[prodnumber]);
                                        String response2 = (String) input.readObject();
                                        if (response2.equals("SUCCESSFUL")) {
                                            JOptionPane.showMessageDialog(null, "Product " +
                                                            "successfully added to cart!", "Customer Menu",
                                                    JOptionPane.INFORMATION_MESSAGE);
                                        }
                                        else {
                                            JOptionPane.showMessageDialog(null, "Product " +
                                                            "couldn't be added to cart, it just went out of stock.",
                                                    "Customer Menu", JOptionPane.INFORMATION_MESSAGE);
                                        }
                                    }
                                }
                                case 1 -> {
                                    send("VIEWMARKET", "PRICE");
                                    Product[] response = (Product[]) input.readObject();
                                    if (response.length == 0) {
                                        JOptionPane.showMessageDialog(null, "There are no" +
                                                        " products in the marketplace currently.", "Customer Menu",
                                                JOptionPane.INFORMATION_MESSAGE);
                                        break;
                                    }
                                    String[] choices = new String[response.length];
                                    for (int i = 0 ; i < response.length ; i++) {
                                        choices[i] = response[i].getName();
                                    }
                                    int prodnumber = (Integer) JOptionPane.showInputDialog(null,
                                            "Choose a product to add to cart: ",
                                            "Customer Menu", JOptionPane.PLAIN_MESSAGE, null, choices,
                                            choices[0]);
                                    if (isExit(prodnumber)) {
                                        break;
                                    } else {
                                        send("ADDTOCART", response[prodnumber]);
                                        String response2 = (String) input.readObject();
                                        if (response2.equals("SUCCESSFUL")) {
                                            JOptionPane.showMessageDialog(null, "Product " +
                                                            "successfully added to cart!", "Customer Menu",
                                                    JOptionPane.INFORMATION_MESSAGE);
                                        }
                                        else {
                                            JOptionPane.showMessageDialog(null, "Product " +
                                                            "couldn't be added to cart, it just went out of stock.",
                                                    "Customer Menu", JOptionPane.INFORMATION_MESSAGE);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                case 2 -> {
                    try {
                        send("PURCHASEHIST");
                        Product[] response = (Product[]) input.readObject();
                        if (response.length == 0) {
                            JOptionPane.showMessageDialog(null, "You have no purchase history.",
                                    "Customer Menu", JOptionPane.INFORMATION_MESSAGE);
                            break;
                        }
                        String[] choices = new String[response.length];
                        for (int i = 0 ; i < response.length ; i++) {
                            choices[i] = response[i].getName();
                        }
                        JOptionPane.showInputDialog(null,
                                "Choose a product to add to cart: ",
                                "Customer Menu", JOptionPane.PLAIN_MESSAGE, null, choices,
                                choices[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                case 3 -> {
                    while (true) {
                        String balance = JOptionPane.showInputDialog(null, "Please enter the " +
                                " of money you would like to add like to import from?", +
                                JOptionPane.QUESTION_MESSAGE);
                        if (isExit(balance)) {
                            break;
                        }
                        try {
                            double amount = Double.parseDouble(balance);
                            if (amount > 0) {
                                send("ADDBALANCE", amount);
                                JOptionPane.showMessageDialog(null, "You have successfully added $"
                                        + amount + "0 to your balance!", "Seller Client", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Please enter an " +
                                    " amount of money.", "Seller Client", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
                case 4 -> {
                    boolean valid = false;
                    while (!valid) {
                        try {
                            send("VIEWCART");
                            Product[] response = (Product[]) input.readObject();
                            if (response.length == 0) {
                                JOptionPane.showMessageDialog(null, "There are no" +
                                                " products in your cart currently.", "Customer Menu",
                                        JOptionPane.INFORMATION_MESSAGE);
                                break;
                            }
                            String[] choices = new String[response.length];
                            for (int i = 0 ; i < response.length ; i++) {
                                choices[i] = response[i].getName();
                            }
                            int prodnumber = (Integer) JOptionPane.showInputDialog(null,
                                    "Choose a product to remove from cart: ",
                                    "Customer Menu", JOptionPane.PLAIN_MESSAGE, null, choices,
                                    choices[0]);
                            if (isExit(prodnumber)) {
                                break;
                            } else {
                                send("REMOVEFROMCART", response[prodnumber]);
                                String response2 = (String) input.readObject();
                                if (response2.equals("SUCCESSFUL")) {
                                    JOptionPane.showMessageDialog(null, "Product " +
                                                    "successfully removed from cart!", "Customer Menu",
                                            JOptionPane.INFORMATION_MESSAGE);
                                }
                                else {
                                    JOptionPane.showMessageDialog(null, "Product " +
                                                    "couldn't be removed from cart for unknown reasons.",
                                            "Customer Menu", JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                case 5 -> {
                    try {
                        send("PURCHASECART");
                        String response = (String) input.readObject();
                        if (response.equals("SUCCESSFUL")) {
                            JOptionPane.showMessageDialog(null, "Cart Purchased",
                                    "Customer Menu", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Purchase Failed",
                                    "Customer Menu", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    /**
     * Use input output to provide UI for logging in to an existing account. Will
     * check to see if login info is valid.
     *
     * @return true if the user decides to exit out of the program; otherwise,
     * false
     */
    public boolean login() {
        boolean valid = false;
        String email;
        String pwd;
        while (!valid) {
            email = JOptionPane.showInputDialog(null, "Enter your email address.",
                    "Store Client", JOptionPane.QUESTION_MESSAGE);
            if (isExit(email)) {
                send("EXIT");
                return true;
            }
            while (emptyOrContainsBadChar(email)) {
                JOptionPane.showMessageDialog(null, "Invalid email. It can not contain \"|\" " +
                        "and must be at least one character long.", "Store Client",
                        JOptionPane.INFORMATION_MESSAGE);
                email = JOptionPane.showInputDialog(null, "Enter your email address.",
                        "Store Client", JOptionPane.QUESTION_MESSAGE);
                if (isExit(email)) {
                    send("EXIT");
                    return true;
                }
            }

            pwd = JOptionPane.showInputDialog(null, "Enter your password.",
                    "Store Client", JOptionPane.QUESTION_MESSAGE);
            if (isExit(pwd)) {
                send("EXIT");
                return true;
            }
            while (pwd == null || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Nothing was entered.",
                        "Store Client", JOptionPane.INFORMATION_MESSAGE);
                pwd = JOptionPane.showInputDialog(null, "Enter your password.",
                        "Store Client", JOptionPane.QUESTION_MESSAGE);
                if (isExit(pwd)) {
                    send("EXIT");
                    return true;
                }
            }
            send("LOGIN", new String[]{email, pwd});
            try {
                String response = (String)input.readObject();
                switch (response) {
                    case "VALID" -> valid = true;
                    case "INVALID" -> JOptionPane.showMessageDialog(null, "Invalid Password.",
                            "Store Client", JOptionPane.INFORMATION_MESSAGE);
                    case "DNE" -> JOptionPane.showMessageDialog(null, "The account doesn't exist.",
                            "Store Client", JOptionPane.INFORMATION_MESSAGE);
                    default -> System.out.println("SOMETHING WENT SERIOUSLY WRONG SERVERSIDE!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Use input output to provide UI for making a new account. Will check to see
     * if info is valid.
     *
     * @return true if the user decides to exit out of the program; otherwise,
     * false
     */
    public boolean makeNewAccount() {
        boolean valid = false;
        String email;
        String pwd;
        int isCustomer;
        while (!valid) {
            email = JOptionPane.showInputDialog(null, "Enter your email address.",
                    "Store Client", JOptionPane.QUESTION_MESSAGE);
            if (isExit(email)) {
                send("EXIT");
                return true;
            }
            while (emptyOrContainsBadChar(email)) {
                JOptionPane.showMessageDialog(null, "Invalid email. It can not contain \"|\" " +
                                "and must be at least one character long.", "Store Client",
                        JOptionPane.INFORMATION_MESSAGE);
                email = JOptionPane.showInputDialog(null, "Enter your email address.",
                        "Store Client", JOptionPane.QUESTION_MESSAGE);
                if (isExit(email)) {
                    send("EXIT");
                    return true;
                }
            }

            pwd = JOptionPane.showInputDialog(null, "Enter your password.",
                    "Store Client", JOptionPane.QUESTION_MESSAGE);
            if (isExit(pwd)) {
                send("EXIT");
                return true;
            }
            while (emptyOrContainsBadChar(pwd)) {
                JOptionPane.showMessageDialog(null, "Invalid password. It can not contain \"|\"" +
                                "and must be at least one character long.", "Store Client",
                        JOptionPane.INFORMATION_MESSAGE);
                pwd = JOptionPane.showInputDialog(null, "Enter your password.",
                        "Store Client", JOptionPane.QUESTION_MESSAGE);
                if (isExit(pwd)) {
                    send("EXIT");
                    return true;
                }
            }

            isCustomer = JOptionPane.showOptionDialog(null, "What kind of account are you making?", "Store Client",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    new String[]{"Customer", "Seller"}, null);
            if (isExit(isCustomer)) {
                send("EXIT");
                return true;
            }
            String[] data;
            if (isCustomer == 0)
                data = new String[]{email, pwd, "CUSTOMER"};
            else
                data = new String[]{email, pwd, "SELLER"};
            send("MAKEACCOUNT", data);
            try {
                String response = (String)input.readObject();
                switch (response) {
                    case "FREE" -> valid = true;
                    case "TAKEN" -> JOptionPane.showMessageDialog(null, "There already exists an account with the" +
                                    " given email. Please try again.", "Store Client", JOptionPane.INFORMATION_MESSAGE);
                    default -> System.out.println("SOMETHING WENT SERIOUSLY WRONG SERVERSIDE!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Checks to see if the string is empty or contains "|"
     *
     * @param str The string in question.
     * @return true if the string is either empty or contains "|"; otherwise,
     * false
     */
    public boolean emptyOrContainsBadChar(String str) {
        return str == null || str.isEmpty() || str.contains("|");
    }

    /**
     * Checks to see if the given string is null.
     *
     * @param str The string in question.
     * @return true if the string is null; otherwise,
     * false
     */
    public boolean isExit(String str) {
        return str == null;
    }

    /**
     * Checks to see if the given integer is -1.
     *
     * @param num The integer in question.
     * @return true if the integer is -1; otherwise,
     * false
     */
    public boolean isExit(int num) {
        return num == -1;
    }

    /**
     * Sends a packet with the given tag.
     * @param tag The tag being sent.
     */
    public void send(String tag) {
        try {
            output.writeObject(new Packet(tag, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a packet with the given tag and data.
     * @param tag The tag being sent.
     * @param data The data being sent.
     */
    public void send(String tag, Serializable data) {
        try {
            output.writeObject(new Packet(tag, data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
