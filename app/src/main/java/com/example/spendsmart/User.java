package com.example.spendsmart;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a user in the SpendSmart application.
 * This class stores all user-related financial data including
 * personal details, monthly budget limits, and spending breakdowns
 * by category.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This class defines the user model used throughout the application.
 *        It contains the user's unique identifier, full name, monthly spending
 *        limit, and a map of category-based expenses. The class is designed
 *        to be stored and retrieved from Firebase Realtime Database, where
 *        it represents the main user data structure. It also supports default
 *        initialization for Firebase deserialization and a parameterized
 *        constructor for creating new users during registration.
 */
public class User {

    /**
     * Unique identifier of the user in Firebase Authentication and Database.
     */
    public String userId;

    /**
     * Full name of the user as entered during registration.
     */
    public String fullName;

    /**
     * Monthly spending limit defined by the user.
     * This value is used to track and compare total expenses.
     */
    public double monthlyLimit;

    /**
     * Map that stores total expenses per category.
     * Key = category name (e.g., Food, Transport)
     * Value = total amount spent in that category.
     * Using Double ensures higher precision when storing financial data.
     */
    public Map<String, Double> categoryTotals;

    /**
     * Empty constructor required by Firebase.
     * Firebase uses this constructor to automatically deserialize data.
     */
    public User() {
    }

    /**
     * Constructor used when creating a new user in the system.
     * Initializes user details and sets up an empty category map.
     *
     * @param userId        unique user ID from Firebase Authentication
     * @param fullName      full name of the user
     * @param monthlyLimit  initial monthly budget limit
     */
    public User(String userId, String fullName, double monthlyLimit) {
        this.userId = userId;
        this.fullName = fullName;
        this.monthlyLimit = monthlyLimit;
        this.categoryTotals = new HashMap<>();
    }
}