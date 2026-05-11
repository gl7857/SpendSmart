package com.example.spendsmart;

/**
 * UserSession is a utility class that manages the currently logged-in user
 * throughout the application.
 * It acts as an in-memory session holder to store, retrieve, and clear
 * the active user without repeatedly accessing Firebase.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This class is responsible for maintaining the current user session
 *        in the application. It stores the active User object in a static
 *        variable so it can be accessed globally from any activity. This
 *        helps improve performance and simplifies user state management.
 *        The class also provides a method to clear the session when the user
 *        logs out, ensuring that sensitive data is removed from memory.
 */
public class UserSession {

    /**
     * Holds the currently logged-in user in memory.
     * This allows global access to user data across all activities.
     */
    private static User currentUser;

    /**
     * Sets the current logged-in user.
     * This method is called after successful login or registration.
     *
     * @param user the User object to store in the session
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Returns the currently logged-in user.
     * If no user is logged in, returns null.
     *
     * @return current User object or null if session is empty
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Clears the current session.
     * This method is used when the user logs out to remove all stored data.
     */
    public static void clearSession() {
        currentUser = null;
    }
}