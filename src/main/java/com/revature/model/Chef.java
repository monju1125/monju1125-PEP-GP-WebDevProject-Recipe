package com.revature.model;

import java.util.Objects;

/**
The Chef class represents a chef user in the system. It stores the chef's basic information such as id, username, email, password, and whether the chef has admin privileges. This class provides getter and setter methods and overridden Object class methods.

You do not need to edit this class.

 */
public class Chef {

    // fields

    /** The unique identifier of the chef. */
    private int id;
    /** The username of the chef. */
    private String username;
    /** The password of the chef. */
    private String password;
    /** The unique identifier of the chef. */
    private String email;
    /** A flag indicating if the chef has admin privileges. */
    private boolean isAdmin;

    // constructors
    public Chef() {
    }

    public Chef(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Chef(int id, String username, String email, String password, boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public Chef(String username, String email, String password, boolean isAdmin) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    // getter and setter methods
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    /**
     * Compares this Chef object with another object for equality.
     * 
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Chef))
            return false;
        Chef chef = (Chef) obj;
        return id == chef.id &&
                username.equals(chef.username) &&
                email.equals(chef.email) &&
                password.equals(chef.password) &&
                isAdmin == chef.isAdmin;

    }

    /**
     * Generates the hash code for this Chef object.
     * 
     * @return the hash code of the chef
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, password, isAdmin);
    }

    /**
     * Returns a string representation of the Chef object.
     * This includes the object's `id`, `username`, `password`, `email`,
     * and `isAdmin` status in a structured format.
     *
     * @return a string representation of the Chef object, including
     *         the `id`, `username`, `password`, `email`, and `isAdmin` status.
     */
    @Override
    public String toString() {
        return "Chef{id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", isAdmin=" + isAdmin + '}';
    }

}
