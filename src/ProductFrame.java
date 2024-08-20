import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A custom frame that has multiple text boxes which ask for product information.
 */
public class ProductFrame extends JFrame {
    String[] data;
    String store;
    boolean validInfo = false;

    /**
     * Constructor for class ProductFrame
     */
    public ProductFrame(String store) {
        this.store = store;
        buildProductFrame();
        setSize(400,300);
        setLocationRelativeTo(null);
    }

    /**
     * Creates the frame that asks for the information.
     */
    public void buildProductFrame() {
        JButton enter = new JButton("Enter");
        JTextField nameBox = new JTextField();
        JTextField desBox = new JTextField();
        JTextField quantityBox = new JTextField();
        JTextField priceBox = new JTextField();
        JLabel description = new JLabel("Please enter your product information.");
        JLabel nameLabel = new JLabel("Product Name: ");
        JLabel prodDescriptionLabel = new JLabel("Product Description: ");
        JLabel quantityLabel = new JLabel("Product Quantity: ");
        JLabel priceLabel = new JLabel("Product Price: ");

        enter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setProductData(nameBox.getText(), desBox.getText(), quantityBox.getText(), priceBox.getText());
            }
        });

        JPanel descriptionPanel = new JPanel();
        JPanel namePanel = new JPanel();
        JPanel prodDescriptionPanel = new JPanel();
        JPanel quantityPanel = new JPanel();
        JPanel pricePanel = new JPanel();
        JPanel enterPanel = new JPanel();
        prodDescriptionPanel.add(prodDescriptionLabel);
        namePanel.add(nameLabel);
        descriptionPanel.add(description);
        quantityPanel.add(quantityLabel);
        pricePanel.add(priceLabel);
        enterPanel.add(enter);

        nameBox.setBounds(150,50,200,20);
        desBox.setBounds(150, 100, 200, 20);
        quantityBox.setBounds(150, 150, 200, 20);
        priceBox.setBounds(150, 200, 200, 20);

        descriptionPanel.setBounds(50, 10, 300, 20);
        namePanel.setBounds(0, 45, 100, 20);
        prodDescriptionPanel.setBounds(0, 95, 130, 20);
        quantityPanel.setBounds(0, 145, 110, 20);
        pricePanel.setBounds(0, 195, 90, 20);

        enterPanel.setBounds(215, 225, 80, 50);

        add(nameBox);
        add(desBox);
        add(quantityBox);
        add(priceBox);

        add(descriptionPanel);
        add(namePanel);
        add(prodDescriptionPanel);
        add(pricePanel);
        add(quantityPanel);
        add(enterPanel);
        add(new Panel());
    }

    /**
     * Sets the data to an array containing the given string parameters
     * @param name First value in the array.
     * @param des Second value in the array.
     * @param q Third value in the array.
     * @param price Fourth value in the array.
     */
    public void setProductData(String name, String des, String q, String price) {
        data = new String[]{store, name, des, q, price};
    }

    public String[] getData() {
        return data;
    }

    public void setData(String[] data) {
        this.data = data;
    }

    public void setInfoValidity(boolean validInfo) {
        this.validInfo = validInfo;
    }

    public boolean infoIsValid() {
        return validInfo;
    }
}
