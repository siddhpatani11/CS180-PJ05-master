/**
 * Abstract class that contains basic information/implementation for a User.
 *
 * @author Anish Gorentala
 * @version November 13, 2022
 */
public abstract class User {
    /**
     * String of the User's email address (treated like a username for most purposes)
     */
    private String email;

    /**
     * String of the User's password
     */
    private String password;

    /**
     * Initialize a new {@code User} object with the specified email and password.
     *
     * @param email    Email of the user
     * @param password Password of the user
     */
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * @return {@link #email} field
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email The value of the {@link #email} field to be set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return {@link #password} field
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password The value of the {@link #password} field to be set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Determine whether the passed in Object is equal to this User.
     * Returns true if the passed in Object is a User with the same email and password as the current User.
     * Returns false otherwise.
     *
     * @param obj Object to compare with the current User
     * @return Whether this User and the passed in Object are equal
     */
    @Override
    public boolean equals(Object obj) {
        // If same exact object, return true
        if (this == obj) {
            return true;
        }

        // If object is not of type User, return false
        if (!(obj instanceof User)) {
            return false;
        }

        // Return true if the current and passed in User share the same email and password
        User temp = (User) obj;
        return this.email.equals(temp.getEmail()) && this.password.equals(temp.getPassword());
    }

    /**
     * Print relevant info on the current User to the specified file
     *
     * @param outfile Path of file to write info to
     */
    public abstract void export(String outfile);
}
