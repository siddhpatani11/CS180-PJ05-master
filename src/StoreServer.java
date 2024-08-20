import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * The server that will create threads for every new
 * client connection. Takes in input from the client and
 * processes the data before sending it back.
 */
public class StoreServer {
    private static ArrayList<Customer> customers;
    private static ArrayList<Seller> sellers;
    private static ArrayList<Product> products;
    private static ArrayList<String> searchable;
    private static ArrayList<String> stats;

    /**
     * Constructor for class StoreServer
     */
    public StoreServer() {
        customers = new ArrayList<>();
        sellers = new ArrayList<>();
        products = new ArrayList<>();
        searchable = new ArrayList<>();
        stats = new ArrayList<>();
        load();
    }

    /**
     * Load initial data for customers and sellers from stored data in files, using IO.
     * Stores parsed data into customers and sellers ArrayLists.
     */
    public void load() {
        String line;
        String[] splitLine;

        // Load customers from file
        try (BufferedReader br = new BufferedReader(new FileReader("customers.txt"))) {
            while ((line = br.readLine()) != null) {
                splitLine = line.split("\\|");
                customers.add(new Customer(splitLine[0], splitLine[1], Double.parseDouble(splitLine[2])));
            }
        } catch (FileNotFoundException ignored) {
            System.out.println("Loaded nothing from customers.txt. The file does not exist.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load sellers from file
        try (BufferedReader br = new BufferedReader(new FileReader("sellers.txt"))) {
            while ((line = br.readLine()) != null) {
                splitLine = line.split("\\|");
                sellers.add(new Seller(splitLine[0], splitLine[1]));

                // Load stores for this seller
                if (splitLine.length > 2) {
                    splitLine = splitLine[2].split(";");
                    for (String s : splitLine) {
                        sellers.get(sellers.size() - 1).addStore(new Store(s));
                    }
                }
            }
        } catch (FileNotFoundException ignored) {
            System.out.println("Loaded nothing from sellers.txt. The file does not exist.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load the carts for each customer
        String lineEmail;
        String lineProduct;
        try (BufferedReader br = new BufferedReader(new FileReader("carts.txt"))) {
            while ((line = br.readLine()) != null) {
                lineEmail = line.substring(0, line.indexOf("|"));
                lineProduct = line.substring(line.indexOf("|") + 1);

                for (Customer customer : customers) {
                    if (customer.getEmail().equals(lineEmail)) {
                        Store targetStore = new Store(lineProduct.substring(0, lineProduct.indexOf("|")));
                        Product curProduct = new Product(lineProduct);
                        int matchIndex;
                        int invIndex;
                        for (Seller seller : sellers) {
                            matchIndex = seller.getStores().indexOf(targetStore);
                            if (matchIndex >= 0) {
                                Store match = seller.getStores().get(matchIndex);
                                invIndex = match.getInventory().indexOf(curProduct);
                                if (invIndex >= 0) {
                                    curProduct.setStore(match);
                                    customer.getCart().add(curProduct);
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException ignored) {
            System.out.println("Loaded nothing from carts.txt. The file does not exist.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load products from file
        try (BufferedReader br = new BufferedReader(new FileReader("products.txt"))) {
            while ((line = br.readLine()) != null) {
                splitLine = line.split("\\|");

                //adds every single product to a main "products" arraylist for viewing
                searchable.add(line);

                Store targetStore = new Store(splitLine[0]);

                int matchIndex;
                for (Seller seller : sellers) {
                    matchIndex = seller.getStores().indexOf(targetStore);
                    if (matchIndex >= 0) {
                        Store match = seller.getStores().get(matchIndex);
                        Product curProduct = new Product(line);
                        curProduct.setStore(match);
                        match.addProduct(curProduct);
                        products.add(curProduct);
                    }
                }
            }
        } catch (FileNotFoundException ignored) {
            System.out.println("Loaded nothing from products.txt. The file does not exist.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load statistics from file
        try (BufferedReader bfr = new BufferedReader(new FileReader("history.txt"))) {
            while ((line = bfr.readLine()) != null) {
                stats.add(line);
            }
        } catch (FileNotFoundException ignored) {
            System.out.println("Loaded nothing from history.txt. The file does not exist.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void save() {
        // Save customers and their carts
        try (
                PrintWriter pw = new PrintWriter(new FileWriter("customers.txt"));
                PrintWriter cartWriter = new PrintWriter(new FileWriter("carts.txt"))
        ) {
            for (Customer customer : customers) {
                pw.println(customer);
                for (Product cartItem : customer.getCart()) {
                    cartWriter.println(customer.getEmail() + "|" + cartItem);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save sellers
        try (PrintWriter pw = new PrintWriter(new FileWriter("sellers.txt"))) {
            for (Seller seller : sellers) {
                pw.println(seller);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save products
        try (PrintWriter pw = new PrintWriter(new FileWriter("products.txt"))) {
            for (Product product : products) {
                pw.println(product);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save stats/history
        try (PrintWriter pw = new PrintWriter(new FileWriter("history.txt"))) {
            for (String stat : stats) {
                pw.println(stat);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startThread(Socket socket) {
        try {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            Thread connection = new ClientHandler(socket, input, output);
            connection.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The main method that will run the server.
     *
     * @param args Arguments from the command line.
     */
    public static void main(String[] args) {
        StoreServer server = new StoreServer();
        try (ServerSocket serverSocket = new ServerSocket(1337)) {
            System.out.println("Server now running on Port 1337");

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected: " + socket);
                    server.startThread(socket);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ClientHandler extends Thread {
        private final Socket socket;
        private final ObjectInputStream input;
        private final ObjectOutputStream output;
        private User user;

        public ClientHandler(Socket socket, ObjectInputStream input, ObjectOutputStream output) {
            this.socket = socket;
            this.input = input;
            this.output = output;
        }

        public void send(Object obj) {
            try {
                output.writeObject(obj);
                output.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Runs when the thread joins.
         */
        @Override
        public void run() {
            Packet received;
            String tag;
            Object data;
            while (!socket.isClosed()) {
                try {
                    received = (Packet) input.readObject();
                    tag = received.getTag();
                    data = received.getData();
                    System.out.println(received);

                    switch (tag) {
                        case "LOGIN" -> send(verifyLogin((String[]) data));
                        case "MAKEACCOUNT" -> send(makeAccount((String[]) data));
                        case "USERTYPE" -> send(user instanceof Customer ? "CUSTOMER" : "SELLER");
                        case "VIEWMARKET" -> send(search(new String[]{"", (String) data}));
                        case "ADDPRODUCT" -> send(addProduct((String[]) data));
                        case "REMOVEPRODUCT" -> send(removeProduct((String[]) data));
                        case "VIEWINCARTS" -> send(viewInCarts());
                        case "VIEWCART" -> send(viewCart());
                        case "REMOVEFROMCART" -> {
                            Customer customer = (Customer) user;
                            send(customer.getCart().remove((Product) data) ? "SUCCESSFUL" : "UNSUCCESSFUL");
                        }
                        case "ADDTOCART" -> send(addToCart((Product) data));
                        case "ADDBALANCE" -> {
                            Customer customer = (Customer) user;
                            customer.setBalance(customer.getBalance() + (double) data);
                        }
                        case "SEARCH" -> send(search((String[]) data));
                        case "ADDSTORE" -> send(addStore((String) data));
                        case "REMOVESTORE" -> send(removeStore((String) data));
                        case "GETSTORES" -> {
                            Seller seller = (Seller) user;
                            Store[] s = seller.getStores().toArray(new Store[0]);
                            send(s);
                        }
                        case "PURCHASEHIST" -> send(purchaseHistory());
                        case "PURCHASECART" -> send(purchaseCart());
                        case "VIEWSALES" -> send(viewSales((Integer) data));
                        case "GETPRODUCTS" -> send(getProducts());
                        case "IMPORT" -> send(importFromFile((String) data));
                        case "EXPORT" -> send(export((String) data));
                        case "EXIT" -> {
                            send("CLOSED");
                            input.close();
                            output.close();
                            socket.close();
                        }
                        default -> send("Invalid Input!");
                    }
                    save();
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            System.out.println("Closed thread for: " + socket);
        }

        public User existingAccount(String email) {
            for (Customer customer : customers)
                if (customer.getEmail().equals(email)) {
                    return customer;
                }
            for (Seller seller : sellers)
                if (seller.getEmail().equals(email)) {
                    return seller;
                }
            return null;
        }

        public String verifyLogin(String[] loginData) {
            String email = loginData[0];
            String password = loginData[1];

            User existingAccount = existingAccount(email);

            // Account does not exist
            if (existingAccount == null) {
                return "DNE";
            }

            // Account exists, and password is valid
            if (existingAccount.getPassword().equals(password)) {
                this.user = existingAccount;
                return "VALID";
            }

            // Account exists, but password is invalid
            return "INVALID";
        }

        public String makeAccount(String[] data) {
            String email = data[0];
            String password = data[1];
            String userType = data[2];

            User existingAccount = existingAccount(email);
            if (existingAccount == null) {
                if (userType.equals("CUSTOMER")) {
                    this.user = new Customer(email, password, 0);
                    customers.add((Customer) this.user);
                } else {
                    this.user = new Seller(email, password);
                    sellers.add((Seller) this.user);
                }
                return "FREE";
            } else {
                return "TAKEN";
            }
        }

        public ArrayList<Product> search(String[] data) {
            String query = data[0];
            String sortType = data[1];

            // Find related Products to query
            ArrayList<Product> output = new ArrayList<>();
            for (Product p : products) {
                if (p.toString().contains(query)) {
                    output.add(p);
                }
            }

            // Sort filtered results as requested
            if (sortType.equals("QUANTITY")) {
                // Sort by quantity
                output.sort(Comparator.comparingInt(Product::getQuantity));
            } else {
                // Sort by price
                output.sort(Comparator.comparingDouble(Product::getPrice));
            }

            return output;
        }

        public String addStore(String storeName) {
            Seller seller = (Seller) user;
            if (seller.getStores().contains(new Store(storeName))) {
                return "TAKEN";
            } else {
                seller.addStore(new Store(storeName));
                return "FREE";
            }
        }

        public String removeStore(String storeName) {
            Seller seller = (Seller) user;
            int storeInd = seller.getStores().indexOf(new Store(storeName));
            if (storeInd >= 0) {
                Store matchStore = seller.getStores().get(storeInd);
                for (Product p : matchStore.getInventory()) {
                    products.remove(p);
                }
                seller.getStores().remove(storeInd);
                return "SUCCESSFUL";
            } else {
                return "UNSUCCESSFUL";
            }
        }

        public String addProduct(String[] product) {
            String storeName = product[0];
            String productName = product[1];
            String description = product[2];
            int quantity = Integer.parseInt(product[3]);
            double price = Double.parseDouble(product[4]);

            Seller seller = (Seller) user;
            Store store = new Store(storeName);

            int existingStoreInd = seller.getStores().indexOf(store);
            if (existingStoreInd < 0) {
                return "INVALID";
            }

            store = seller.getStores().get(existingStoreInd);
            Product p = new Product(store, productName, description, quantity, price);
            if (store.getInventory().contains(p)) {
                return "TAKEN";
            } else {
                store.getInventory().add(p);
                products.add(p);
                searchable.add(p.toString());
                return "SUCCESSFUL";
            }
        }

        public String removeProduct(String[] data) {
            String storeName = data[0];
            String productName = data[1];

            Seller seller = (Seller) user;
            Store store = new Store(storeName);

            int existingStoreInd = seller.getStores().indexOf(store);
            if (existingStoreInd < 0) {
                return "INVALID";
            }

            store = seller.getStores().get(existingStoreInd);

            if (store.getInventory().remove(new Product(store, productName, "", 0, 0))) {
                return "SUCCESSFUL";
            } else {
                return "UNSUCCESSFUL";
            }
        }

        public String importFromFile(String infile) {
            // Add products to inventory from file
            Seller seller = (Seller) user;
            String line;
            String[] splitLine;
            try (BufferedReader br = new BufferedReader(new FileReader(infile))) {
                while ((line = br.readLine()) != null) {
                    splitLine = line.split("\\|");

                    //adds every single product to a main "products" arraylist for viewing
                    searchable.add(line);

                    Store targetStore = new Store(splitLine[0]);
                    Product curProduct = new Product(line);
                    int matchIndex = seller.getStores().indexOf(targetStore);
                    if (matchIndex >= 0) {
                        Store match = seller.getStores().get(matchIndex);
                        if (match.getInventory().contains(curProduct)) {
                            curProduct = match.getInventory().get(match.getInventory().indexOf(curProduct));
                            curProduct.setQuantity(curProduct.getQuantity() + Integer.parseInt(splitLine[3]));
                        } else {
                            curProduct.setStore(match);
                            match.addProduct(curProduct);
                            products.add(curProduct);
                        }
                    } else {
                        seller.addStore(curProduct.getStore());
                        curProduct.getStore().addProduct(curProduct);
                        products.add(curProduct);
                    }
                }
            } catch (FileNotFoundException fileNotFoundException) {
                return "INVALID";
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "VALID";
        }

        public String export(String outfile) {
            Seller seller = (Seller) user;
            try (PrintWriter pw = new PrintWriter(new FileWriter(outfile))) {
                seller.getStores().forEach(store -> store.getInventory().forEach(pw::println));
            } catch (IOException e) {
                return "INVALID";
            }
            return "VALID";
        }

        public String viewInCarts() {
            int counter = 0;
            Seller seller = (Seller) user;
            for (Customer customer : customers) {
                for (Product prod : customer.getCart()) {
                    if (seller.getStores().contains(prod.getStore())) {
                        counter += prod.getQuantity();
                    }
                }
            }
            return String.valueOf(counter);
        }

        public ArrayList<String> viewSales(int sortOption) {
            ArrayList<String> sales = new ArrayList<>();
            Seller seller = (Seller) user;

            if (sortOption == 0) {
                if (stats.isEmpty()) {
                    return sales;
                }

                HashMap<String, Integer> storeCounts = new HashMap<>();
                String[] splitString;
                String store;
                int quantity;
                for (String s : stats) {
                    splitString = s.split("\\|");
                    store = splitString[1];
                    quantity = Integer.parseInt(splitString[4]);

                    if (seller.getStores().contains(new Store(store))) {
                        storeCounts.merge(store, quantity, Integer::sum);
                    }
                }

                storeCounts.entrySet().stream().sorted(
                        (a, b) -> -a.getValue().compareTo(b.getValue())).forEach(
                        x -> sales.add(x.getKey() + ": " + x.getValue()
                        )
                );
            } else if (sortOption == 1) {
                HashMap<String, Integer> customerCounts = new HashMap<>();
                String[] splitString;
                String email;
                String store;
                int quantity;
                for (String s : stats) {
                    splitString = s.split("\\|");
                    email = splitString[0];
                    store = splitString[1];
                    quantity = Integer.parseInt(splitString[4]);

                    if (seller.getStores().contains(new Store(store))) {
                        customerCounts.merge(email, quantity, Integer::sum);
                    }
                }

                customerCounts.entrySet().stream().sorted(
                        (a, b) -> -a.getValue().compareTo(b.getValue())).forEach(
                        x -> sales.add(x.getKey() + ": " + x.getValue()
                        )
                );
            }
            return sales;
        }

        public ArrayList<Product> viewCart() {
            Customer customer = (Customer) user;
            return customer.getCart();
        }

        public String[] getProducts() {
            Seller seller = (Seller) user;
            ArrayList<String> products = new ArrayList<>();
            for(int i = 0; i < seller.getStores().size(); i++) {
                for(int j = 0; j < seller.getStores().get(i).getInventory().size(); j++) {
                    products.add(seller.getStores().get(i).getInventory().get(j).getName());
                }
            }
            String[] productNames = products.toArray(new String[products.size()]);
            return productNames;
        }

        public String addToCart(Product product) {
            Customer customer = (Customer) user;

            // set variable for index of product in the store's inventory
            int inventoryIndex = -1;
            for(int i = 0; i < product.getStore().getInventory().size(); i++) {
                if(product.getStore().getInventory().get(i).equals(product)) {
                    inventoryIndex = i;
                    break;
                }
            }

            // if the product does not exist in the inventory, return UNSUCCESSFUL
            if(inventoryIndex < 0) {
                return "UNSUCCESSFUL";
            }

            // if the quantity of the product in the inventory is less than the quantity of the product requested by the customer, return UNSUCCESSFUL
            if(product.getStore().getInventory().get(inventoryIndex).getQuantity() < product.getQuantity()) {
                return "UNSUCCESSFUL";
            }

            // if the quantity of the product requested by the customer is available in the store's inventory, return successful
            else if(product.getStore().getInventory().get(inventoryIndex).getQuantity() >= product.getQuantity()) {
                customer.getCart().add(product);
                return "SUCCESSFUL";
            }

            // by default return UNSUCCESSFUL
            return "UNSUCCESSFUL";
        }

        public String purchaseCart() {
            Customer customer = (Customer) user;

            // variable for total cost of order
            double orderTotal = 0.0;

            // variable for items out of the customers cart that were available in the marketplace at the given quantity
            int itemsAvailable = 0;

            // outer for-loop multiplies the quantity and price of the current product and adds it to the order total
            for(int i = 0; i < customer.getCart().size(); i++) {
                orderTotal += (customer.getCart().get(i).getQuantity() * customer.getCart().get(i).getPrice());
                // inner for-loop looks through the inventory of the store that the current product is from
                for(int j = 0; j < customer.getCart().get(i).getStore().getInventory().size(); j++) {
                    // if the product is found in the inventory
                    if(customer.getCart().get(i).equals(customer.getCart().get(i).getStore().getInventory().get(j))) {
                        // and if the quantity of the product in the inventory is >= to the quantity of the product requested by the customer
                        if(customer.getCart().get(i).getStore().getInventory().get(j).getQuantity() >= customer.getCart().get(i).getQuantity()) {
                            // then add that product as an available item
                            itemsAvailable++;
                        }
                        break;
                    }
                }
            }

            // if the customer's balance is not enough to pay the order total or there were items unavailable in the market return UNSUCCESSFUL
            if(customer.getBalance() < orderTotal || itemsAvailable < customer.getCart().size()) {
                return "UNSUCCESSFUL";
            } else {
                // resetting inventory quantities of the products bought
                for(int i = 0; i < customer.getCart().size(); i++) {
                    // inner for-loop looks through the inventory of the store that the current product is from
                    // once it finds the store, it subtracts the quantity of the product that the user bought from the quantity of the product in the inventory
                    for(int j = 0; j < customer.getCart().get(i).getStore().getInventory().size(); j++) {
                        if(customer.getCart().get(i).equals(customer.getCart().get(i).getStore().getInventory().get(j))) {
                            customer.getCart().get(i).getStore().getInventory().get(j).setQuantity(customer.getCart().get(i).getStore().getInventory().get(j).getQuantity() - customer.getCart().get(i).getQuantity());
                            break;
                        }
                    }
                }

                // add the products the customer bought to their purchase history
                for(int i = 0; i < customer.getCart().size(); i++) {
                    customer.getPurchaseHistory().add(customer.getCart().get(i));
                }
                // empty customer cart
                customer.getCart().clear();
                return "SUCCESSFUL";
            }
        }

        public ArrayList<Product> purchaseHistory() {
            Customer customer = (Customer) user;
            return customer.getPurchaseHistory();
        }
    }
}