package com.example.spendsmart;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Utility class for central Firebase references.
 * This class provides a single place to access Firebase Authentication,
 * Realtime Database, and Firebase Storage instances used throughout the app.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This class is responsible for managing and providing global
 *        access points to Firebase services used in the application.
 *        It initializes Firebase Authentication for user login and
 *        registration, Firebase Realtime Database for storing and
 *        retrieving app data such as expenses and messages, and
 *        Firebase Storage for handling image uploads. By centralizing
 *        these references, the application avoids repeated initialization
 *        and ensures consistent access to Firebase resources across all
 *        activities and components.
 */
public class FBRef {

    /**
     * Firebase Authentication instance used for user login, signup,
     * and authentication state management.
     */
    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();

    /**
     * Main Firebase Realtime Database instance used across the app.
     */
    public static FirebaseDatabase db = FirebaseDatabase.getInstance();

    /**
     * Reference to the "Messages" node in Firebase Realtime Database.
     * This can be used for storing and retrieving chat or system messages.
     */
    public static DatabaseReference refMessages = db.getReference("Messages");

    /**
     * Firebase Storage instance used for uploading and retrieving files.
     */
    public static FirebaseStorage storage = FirebaseStorage.getInstance();

    /**
     * Root reference of Firebase Storage, used as a base path for files.
     */
    public static StorageReference refStorageRoot = storage.getReference();

    /**
     * Specific storage reference pointing to the "images" folder,
     * used for storing uploaded images such as receipts or profile pictures.
     */
    public static StorageReference refFull =
            refStorageRoot.child("images");
}