package com.example.spendsmart;

import java.io.Serializable;

/**
 * Represents a financial expense in the SpendSmart application.
 * This class stores all the information related to a single expense
 * including its unique identifier, category, amount, date, and optional notes.
 * The class implements Serializable to allow passing Expense objects
 * between activities.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This class defines the Expense model used in the application.
 *        It contains the necessary attributes to represent an expense
 *        and provides constructors, getters, and setters to enable
 *        safe access and modification of the data. The class is
 *        compatible with Firebase and supports object transfer
 *        between activities.
 */
public class Expense implements Serializable {

    /**
     * Unique identifier of the expense.
     * This field is mainly used for Firebase database storage
     * to distinguish between different expense records.
     */
    private String id;

    /**
     * Category of the expense.
     * Example values include food, fuel, entertainment, etc.
     * This helps in organizing and filtering expenses.
     */
    private String category;

    /**
     * Monetary amount of the expense.
     * Represents how much money was spent.
     */
    private double amount;

    /**
     * Date of the expense.
     * Stored as a String to represent when the expense occurred.
     */
    private String date;

    /**
     * Optional notes related to the expense.
     * This field can store additional information
     * provided by the user.
     */
    private String notes;

    /**
     * Empty constructor required by Firebase.
     * Firebase uses this constructor when retrieving
     * data from the database.
     */
    public Expense() {
    }

    /**
     * Constructor for creating a new Expense object.
     *
     * @param category The category of the expense.
     * @param amount   The amount of money spent.
     * @param date     The date of the expense.
     */
    public Expense(String category, double amount, String date) {
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    /**
     * Returns the unique ID of the expense.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the expense.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the category of the expense.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category of the expense.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Returns the amount of the expense.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the amount of the expense.
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Returns the date of the expense.
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the date of the expense.
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Returns a formatted string representation of the expense.
     * This method is mainly used for displaying the expense
     * inside a ListView or other UI components.
     */
    @Override
    public String toString() {
        return category + ": ₪" + amount + " (" + date + ")";
    }
}