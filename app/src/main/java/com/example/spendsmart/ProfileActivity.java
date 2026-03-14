package com.example.spendsmart;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private TextView tvName, tvEmail;
    private FloatingActionButton btnChangePhoto;
    private Button btnEditProfile, btnLogout;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    uploadToFirebase(result.getData().getData(), null);
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                    uploadToFirebase(null, bitmap);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // אתחול Firebase
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("profile_pics");
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // אתחול UI
        imgProfile = findViewById(R.id.img_profile);
        tvName = findViewById(R.id.tv_display_name);
        tvEmail = findViewById(R.id.tv_display_email);
        btnChangePhoto = findViewById(R.id.btn_change_photo);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);

        // מאזינים
        btnChangePhoto.setOnClickListener(v -> showImagePickerDialog());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });

        loadUserData();
        loadProfileImage();
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) return;

        tvEmail.setText(mAuth.getCurrentUser().getEmail());
        String userId = mAuth.getCurrentUser().getUid();

        mDatabase.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("fullName").getValue(String.class);
                    if (name != null) tvName.setText(name);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void showEditProfileDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 10);

        final EditText etName = new EditText(this);
        etName.setHint("Name");
        etName.setText(tvName.getText().toString());
        layout.addView(etName);

        final EditText etEmail = new EditText(this);
        etEmail.setHint("Email");
        etEmail.setText(tvEmail.getText().toString());
        layout.addView(etEmail);

        new AlertDialog.Builder(this)
                .setTitle("Update Profile")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newEmail = etEmail.getText().toString().trim();
                    if (!newName.isEmpty() && !newEmail.isEmpty()) {
                        updateProfileInFirebase(newName, newEmail);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateProfileInFirebase(String newName, String newEmail) {
        ProgressDialog pd = ProgressDialog.show(this, "Updating", "Please wait...", true);
        String userId = mAuth.getCurrentUser().getUid();

        // עדכון אימייל ב-Auth
        mAuth.getCurrentUser().updateEmail(newEmail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // עדכון שם ואימייל ב-Database
                mDatabase.child(userId).child("fullName").setValue(newName);
                mDatabase.child(userId).child("email").setValue(newEmail);

                tvName.setText(newName);
                tvEmail.setText(newEmail);
                pd.dismiss();
                Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
            } else {
                pd.dismiss();
                Toast.makeText(this, "Auth Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                    } else {
                        galleryLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
                    }
                }).show();
    }

    private void uploadToFirebase(Uri uri, Bitmap bitmap) {
        ProgressDialog pd = ProgressDialog.show(this, "Uploading", "Please wait...", true);
        StorageReference fileRef = storageRef.child(mAuth.getCurrentUser().getUid() + ".png");

        UploadTask uploadTask = (uri != null) ? fileRef.putFile(uri) :
                fileRef.putBytes(getBitmapBytes(bitmap));

        uploadTask.addOnSuccessListener(task -> {
            pd.dismiss();
            fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                Glide.with(this).load(downloadUri).circleCrop().into(imgProfile);
            });
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private byte[] getBitmapBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private void loadProfileImage() {
        if (mAuth.getCurrentUser() == null) return;
        storageRef.child(mAuth.getCurrentUser().getUid() + ".png").getDownloadUrl()
                .addOnSuccessListener(uri -> Glide.with(this).load(uri).circleCrop().into(imgProfile));
    }
}