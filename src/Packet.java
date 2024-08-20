import java.io.Serializable;

/**
 * Class that contains information for a Packet of data, storing values of a tag and data.
 * Allows for the easier sending and processing of info between the server and client.
 *
 * @author Anish Gorentala
 * @version December 6, 2022
 */
public class Packet implements Serializable {
    /**
     * The tag associated with this packet
     */
    private String tag;
    /**
     * The data associated with this packet
     */
    private Serializable data;

    /**
     * Initialize a new {@code Packet} object with the specified tag and data
     *
     * @param tag  Tag of the Packet
     * @param data Data of the Packet
     */
    public Packet(String tag, Serializable data) {
        this.tag = tag;
        this.data = data;
    }

    /**
     * @return {@link #tag} field
     */
    public String getTag() {
        return tag;
    }

    /**
     * @param tag The value of the {@link #tag} field to be set
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * @return {@link #data} field
     */
    public Object getData() {
        return data;
    }

    /**
     * @param data The value of the {@link #data} field to be set
     */
    public void setData(Serializable data) {
        this.data = data;
    }

    /**
     * Returns a single line String representation of this Packet.
     * Follows the format of {@code Packet{tag="tagValue", data="dataValue"}}
     *
     * @return {@code String} representation of this Packet
     */
    @Override
    public String toString() {
        return "Packet{" +
                "tag='" + tag + '\'' +
                ", data=" + data +
                '}';
    }
}
