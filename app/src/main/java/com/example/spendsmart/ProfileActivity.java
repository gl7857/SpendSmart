package com.example.spendsmart;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

/**
 * ProfileActivity manages the user's profile screen in the SpendSmart application.
 * This screen allows viewing and editing user details, updating profile picture,
 * and logging out. It integrates Firebase Authentication, Realtime Database,
 * and Firebase Storage.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This activity displays and manages the user's profile information.
 *        It retrieves the user's name and email from Firebase, allows editing
 *        of the name, supports changing the profile picture via camera or gallery,
 *        uploads images to Firebase Storage, and displays them using Glide.
 *        It also provides logout functionality and navigation between app screens.
 */
public class ProfileActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private TextView tvName, tvEmail;
    private FloatingActionButton btnChangePhoto;
    private Button btnEditProfile, btnLogout;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            uploadToFirebase(uri, null);
                        }
                    }
            );

    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicturePreview(),
                    bitmap -> {
                        if (bitmap != null) {
                            uploadToFirebase(null, bitmap);
                        }
                    }
            );

    /**
     * Initializes UI components, Firebase services,
     * bottom navigation, and loads user data and profile image.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("profile_pics");
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        imgProfile = findViewById(R.id.img_profile);
        tvName = findViewById(R.id.tv_display_name);
        tvEmail = findViewById(R.id.tv_display_email);
        btnChangePhoto = findViewById(R.id.btn_change_photo);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_expenses) {
                startActivity(new Intent(this, ExpenseTypeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_graphs) {
                startActivity(new Intent(this, AnalyticsActivity.class));
                finish();
                return true;
            }
            return false;
        });

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

    /**
     * Loads user name and email from Firebase.
     * Email comes from Authentication, name from Realtime Database.
     */
    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) return;

        tvEmail.setText(mAuth.getCurrentUser().getEmail());
        String userId = mAuth.getCurrentUser().getUid();

        mDatabase.child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("fullName").getValue(String.class);
                            if (name != null && !name.isEmpty()) {
                                tvName.setText(name);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ProfileActivity.this,
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Opens dialog to edit user's name and saves it to Firebase.
     */
    private void showEditProfileDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 10);

        final EditText etName = new EditText(this);
        etName.setHint("Name");
        etName.setText(tvName.getText().toString());
        layout.addView(etName);

        new AlertDialog.Builder(this)
                .setTitle("Update Profile")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {

                    String newName = etName.getText().toString().trim();

                    if (!newName.isEmpty()) {
                        mDatabase.child(mAuth.getCurrentUser().getUid())
                                .child("fullName")
                                .setValue(newName);

                        tvName.setText(newName);

                        Toast.makeText(this,
                                "Name Updated!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Shows dialog to choose image source (camera or gallery).
     */
    private void showImagePickerDialog() {

        String[] options = {"Camera", "Gallery"};

        new AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {
                        cameraLauncher.launch(null);
                    } else {
                        galleryLauncher.launch("image/*");
                    }
                }).show();
    }

    /**
     * Uploads image to Firebase Storage and displays it using Glide.
     */
    private void uploadToFirebase(Uri uri, Bitmap bitmap) {

        ProgressDialog pd = ProgressDialog.show(
                this,
                "Uploading",
                "Please wait...",
                true);

        StorageReference fileRef =
                storageRef.child(mAuth.getCurrentUser().getUid() + ".png");

        UploadTask uploadTask =
                (uri != null) ? fileRef.putFile(uri)
                        : fileRef.putBytes(getBitmapBytes(bitmap));

        uploadTask.addOnSuccessListener(task -> {

            pd.dismiss();

            fileRef.getDownloadUrl().addOnSuccessListener(downloadUri ->
                    Glide.with(this)
                            .load(downloadUri)
                            .circleCrop()
                            .into(imgProfile));

        }).addOnFailureListener(e -> {

            pd.dismiss();

            Toast.makeText(this,
                    "Failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Converts Bitmap to byte array for Firebase upload.
     */
    private byte[] getBitmapBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * Loads profile image from Firebase Storage and displays it.
     */
    private void loadProfileImage() {
        if (mAuth.getCurrentUser() == null) return;

        storageRef.child(mAuth.getCurrentUser().getUid() + ".png")
                .getDownloadUrl()
                .addOnSuccessListener(uri ->
                        Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .into(imgProfile));
    }
}