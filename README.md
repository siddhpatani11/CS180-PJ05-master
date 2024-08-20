# CS180-PJ05

## Repository for our CS 180 Project 5 group Shoe Marketplace!

## _Developed by:_ Siddh Patani, Anish Gorentala, John Zeng, Kian Kishimoto, and Aryan Shahu

## HOW TO RUN

---
1. Compile all Java files needed for the project (make sure your present working directory is the ``work`` directory)
   ```
   javac StoreServer.java
   javac StoreClient.java
   ```
2. Start up the server
   ```
   java StoreServer
   ```
3. Open a new GUI client to interact with the marketplace
   ```
   java StoreClient
   ```
4. Read the note below.
5. After that, you should be able to run the code and interact using an amazing GUI!

**IMPORTANT NOTE ON FIRST RUN**

- When first running the program, there are likely no files to pre-populate data from.
- To tackle this, use the program's user interface to make customers, sellers, stores, and products yourself.

**Note on importing:**

- Importing products for a Seller treats it as if the Seller is bringing in a new shipment of products.
- This means that old products in a Seller's inventory will not be replaced.
- Products will either be added if new, or have their quantity increased (summed with quantity from file) appropriately.
- The format of each line must follow: ``storeName|productName|productDescription|productQuantity|productPrice``

## SUBMISSION DETAILS

---

- [X] Kian Kishimoto submitted Project 5 Report on BrightSpace
- [X] Kian Kishimoto submitted Project 5 Presentation on BrightSpace
- [X] John Zeng submitted the Vocareum workspace

## CLASS DESCRIPTIONS

---

### Product

---
**Class that contains information for a product, storing values of name, store, description, quantity, and price.**

- The Product class represents the most basic unit of storage for this program, as a Customer or Store (of which a
  Seller can have multiple Stores) can have multiple Products.
- Besides constructors (from multiple arguments, or a single string to be parsed) and getters/setters, the Product class
  has its own toString and equals methods to make interaction with them easier.
- A product must belong to a Store, the combination of owner Store and product name make a Product object item unique.

### Store

---
**Class that contains information for, and functions to interact with a Store.**

- A Seller can have multiple Stores, and Store can have multiple Products.
- Contains information on the Store's name and its list of products.
- Besides constructors, the class contain basic methods for interaction to check if a product can be sold in the
  requested amount, as well as an equals method that handles passing in a String to just compare with the Store's name.

### User

---
**Abstract class that contains information for a user, storing values of email and password.**

- This abstract class has both customer and seller extending it.
- Contains the default information for any user which is the email and password any user will use to log in.
- Besides the constructor, getters, and setters, this class contains an equals method that checks both the email
  and password of the object.

### Customer

---
**Class that contains information for a customer, extending user and storing values of balance and shopping cart.**

- Customers are unique because their accounts have a balance as well as a shopping cart.
- There is an addItem and removeItem method that either adds or removes an item from a customer's shopping cart. It
  also edits the stored data in "carts.txt". addItem also edits the quantity of the product that is added depending
  on how many were added to the cart.
- The viewItems method allows the customers to view the products in their cart and the purchaseItems method will
  allow them to purchase the items that they've put in their cart.
-  You can also view statistics of certain stores and purchase history with the viewStats and purchaseHist methods. 
- There is also an equals method that checks if an object is equal to a customer and a toString method that returns
  the customer's data in the format that it's stored in the "customers.txt" file.

### Seller

---
**Class that contains information for a seller, extending user and storing values of stores.**

- Sellers are unique because each seller is able to own various stores, so all the stores a seller owns are stored in
  a single array list.
- There is an equals method that checks if an object is equal to the given seller and a toString method that returns a
  formatted string that can be stored in the "sellers.txt" file.
- The export method exports all the products from all the stores the seller owns into a file that they are able to
  download.
- There is also a viewInCarts method that allows a seller to view all the products that are in a shopping cart.

### StoreClient

---
**Class that contains the client that handles the input/output with the user and communicates with the server.**

- When the class is run, it creates a new socket that connects with the server socket. It will then run the runStore method
  that begins the GUI for the client, which is the login/create account page.
- There are separate methods for the login GUI, seller GUI, and customer GUI since they're all so different. 
- The login method handles the login UI and there are various methods to check if the account exists, and create a new
  account delete an account.
- There are also various methods to check for badly formatted or exit strings. There are also send methods in order to send
  send packets to the server.
- The client interacts with the server throughout the user interaction and constantly sends and receives packets.

### StoreServer

---
**Class that contains the server that the client connects to and communicates with, as well as the static data.**
- The save and load methods read and write to the various files that are acting as databases and storing information.
- The startThread function starts a new thread whenever a client connects and the main method constantly checks for
  new connections to the server socket.
- There is a separate class inside of this class called ClientHandler that extends Thread. That class is the main thread
  method for the clients. It has a send function that allows the server to send data to the client as well as a run function
  that handles the packets and sends the corresponding data. This class also contains various methods that check if the 
  account already exists, verify the login, make a new account, search through the products, add stores or products, remove
  products, import/export data from a file, view the carts, and view the sales.   
- This is the main class of the project, so it utilizes all the other classes inside it (e.g. the load and save methods reference
 various classes as well as the store and product classes, the methods that deal with login and accounts reference the customer and
 seller classes).
 
### Packet
 
---
**Class that contains the information for a packet, which is the form of data that is being sent to and from the server.**
- The packet has a constructor that includes a "tag" and a "data" attribute. Typically the tag is to allow the server
  to know what kind of packet the client is sending (eg. if the user wants to create an account it will send a packet
  with the "MAKEACCOUNT" tag to the server).
- In addition to getters and setters, the packet also has a toString method that returns the tag as well as the data
  of the packet.
- We used this class as a way of unifying the way the data is sent to and from the server. It makes it much easier for
  the server to understand the data, since we just have to use a switch statement with cases based on the tag values
  to figure out what data needs to be sent back to the client to display.
  
### ProductFrame

---
**Class that contains the information for a custom frame that has multiple text boxes specifically used for products.**
- The class just defines a specific frame that needs to be used any time a product needs to be either added or edited.
  We had to customize the frame since there are so many text boxes and it wouldn't be able to be created with just one
  JFrame statement. We made it a separate class so that we can just reference it whenever needed instead of having to
  write it over and over again.
- The class also contains getters and setters, as well as a setProductData method that takes the input from the text
  boxes and either updates the product or adds a new product using the information given.

## FUN EXTRAS!!!

---
**Movie Quote of the day :)**

```
You were the chosen one! It was said that you would destroy the Sith, not join them. 
You were to bring balance to the Force, not leave it in darkness!

**I hate you!**

You were my brother, Anakin. I loved you.
```

**Movie Parody of the day :(**

```
You were the chosen ones! It was said that you would get rid of painful/stressful projects, not create them. 
You were to bring easy work and good grades to the CompSci majors, not leave them in suffering!

Just code better >:)

You were supposed to help us, TAs! We trusted you.
```
