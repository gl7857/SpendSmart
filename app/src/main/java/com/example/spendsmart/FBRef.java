package com.example.spendsmart;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FBRef {

    public static FirebaseAuth refAuth = FirebaseAuth
            .getInstance();

    public static FirebaseDatabase db = FirebaseDatabase.getInstance();
    public static DatabaseReference refMessages = db.getReference("Messages");

    public static FirebaseStorage storage = FirebaseStorage.getInstance();
    public static StorageReference refStorageRoot = storage.getReference();

    public static StorageReference refFull =
            refStorageRoot.child("images");
}