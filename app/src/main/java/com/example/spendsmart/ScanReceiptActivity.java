package com.example.spendsmart;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity for scanning receipts using the camera or gallery and extracting expense data using AI.
 * This screen allows the user to capture or select an image of a receipt, analyze it using AI,
 * automatically fill expense details such as amount, date, and category, and then save the
 * extracted information to Firebase Realtime Database under the current user.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This activity enables smart expense creation using receipt scanning.
 *        The user can take a photo or select an image from the gallery, which is then
 *        processed by an AI model (Gemini) to extract expense details. The extracted
 *        data is automatically filled into the UI fields. The user can then confirm
 *        and save the expense to Firebase. The activity also handles camera permissions,
 *        image processing, error handling, and fallback to manual entry when needed.
 */
public class ScanReceiptActivity extends AppCompatActivity {

    private EditText etAmount, etDate;
    private Spinner spinnerCategory;
    private ImageView iV;
    private ProgressBar scanProgress;
    private View layoutPlaceholder;

    private GeminiManager geminiManager;
    private String currentPath;
    private DatabaseReference mDatabase;

    private static final int REQUEST_CAMERA_PERMISSION = 6709;
    private static final int REQUEST_FULL_IMAGE_CAPTURE = 9051;

    /**
     * Initializes the scan receipt screen.
     * This method sets up all UI components, initializes Firebase reference,
     * prepares the AI manager, and sets click listeners for scanning receipts
     * and saving expenses.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_receipt);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        etAmount = findViewById(R.id.et_amount);
        etDate = findViewById(R.id.et_date);
        spinnerCategory = findViewById(R.id.spinner_category);
        iV = findViewById(R.id.iV);
        scanProgress = findViewById(R.id.scan_progress);
        layoutPlaceholder = findViewById(R.id.layout_scan_placeholder);

        geminiManager = GeminiManager.getInstance();

        findViewById(R.id.btn_scan_receipt).setOnClickListener(v -> checkPermissionAndGetPhoto());
        findViewById(R.id.btn_save_expense).setOnClickListener(v -> saveToFirebase());
    }

    /**
     * Saves the scanned or manually corrected expense to Firebase.
     * This method validates user input, ensures the user is logged in,
     * updates category totals, generates a unique expense ID,
     * and stores the expense data in the database under the user.
     */
    private void saveToFirebase() {
        String amountStr = etAmount.getText().toString().trim();
        String dateStr = etDate.getText().toString().trim();
        String selectedCategory = spinnerCategory.getSelectedItem().toString();

        String finalCategory;
        if (selectedCategory.equalsIgnoreCase("Other")) {
            EditText etOther = findViewById(R.id.et_other_category);
            finalCategory = (etOther != null && !etOther.getText().toString().isEmpty())
                    ? etOther.getText().toString().trim() : "Other";
        } else {
            finalCategory = selectedCategory;
        }

        if (amountStr.isEmpty() || dateStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            User currentUser = UserSession.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Error: User session expired", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Double> totals = currentUser.categoryTotals;
            double currentTotal = totals.getOrDefault(finalCategory, 0d);
            totals.put(finalCategory, currentTotal + amount);
            currentUser.categoryTotals = totals;

            String key = mDatabase.child("expenses").push().getKey();
            if (key != null) {

                Map<String, Object> expenseValues = new HashMap<>();
                expenseValues.put("id", key);
                expenseValues.put("category", finalCategory);
                expenseValues.put("amount", amount);
                expenseValues.put("date", dateStr);
                expenseValues.put("timestamp", System.currentTimeMillis());

                mDatabase.child("expenses").child(key).setValue(expenseValues);

                mDatabase.child("users")
                        .child(currentUser.userId)
                        .child("categoryTotals")
                        .setValue(totals)
                        .addOnSuccessListener(aVoid -> {
                            UserSession.setCurrentUser(currentUser);
                            Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, HistoryActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sends the receipt image to AI for analysis and extracts expense data.
     * The AI returns structured text which is then parsed and used
     * to automatically fill the expense fields in the UI.
     */
    private void analyzeReceiptWithAI(Bitmap bitmap) {
        iV.setImageBitmap(bitmap);
        iV.setVisibility(View.VISIBLE);
        layoutPlaceholder.setVisibility(View.GONE);
        scanProgress.setVisibility(View.VISIBLE);

        String prompt = "Carefully analyze this image. If it is NOT a receipt or doesn't contain a clear price and date, " +
                "return ONLY the word: NOT_A_RECEIPT. Otherwise extract amount, date and category.";

        geminiManager.sendTextWithPhotoPrompt(prompt, bitmap, new GeminiCallBack() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    scanProgress.setVisibility(View.GONE);
                    parseAndFillFields(result);
                });
            }

            @Override
            public void onFailure(Throwable error) {
                runOnUiThread(() -> {
                    scanProgress.setVisibility(View.GONE);
                    showErrorDialog("Connection Error", "AI server unreachable.");
                });
            }
        });
    }

    /**
     * Parses AI response and fills UI fields accordingly.
     * If the response is invalid or not a receipt, an error dialog is shown.
     */
    private void parseAndFillFields(String result) {
        if (result.trim().equalsIgnoreCase("NOT_A_RECEIPT")) {
            showErrorDialog("Invalid Image", "This photo doesn't look like a clear receipt.");
            return;
        }

        try {
            if (result.contains("Amount:")) {
                String amount = result.split("Amount:")[1].split(",")[0].trim().replaceAll("[^\\d.]", "");
                etAmount.setText(amount);
            }

            if (result.contains("Date:")) {
                String date = result.split("Date:")[1].split(",")[0].trim();
                etDate.setText(date);
            }

            if (result.contains("Category:")) {
                String aiCategory = result.split("Category:")[1].trim();

                boolean found = false;
                for (int i = 0; i < spinnerCategory.getCount(); i++) {
                    if (spinnerCategory.getItemAtPosition(i).toString().equalsIgnoreCase(aiCategory)) {
                        spinnerCategory.setSelection(i);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    for (int i = 0; i < spinnerCategory.getCount(); i++) {
                        if (spinnerCategory.getItemAtPosition(i).toString().equalsIgnoreCase("Other")) {
                            spinnerCategory.setSelection(i);
                            EditText etOther = findViewById(R.id.et_other_category);
                            if (etOther != null) etOther.setText(aiCategory);
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            showErrorDialog("Error", e.getMessage());
        }
    }

    /**
     * Shows an error dialog with options to retry scanning or switch to manual entry.
     */
    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Try Again", (d, w) -> checkPermissionAndGetPhoto())
                .setNegativeButton("Manual Entry", (d, w) -> {
                    Intent intent = new Intent(this, ManualEntryActivity.class);
                    intent.putExtra("pre_amount", etAmount.getText().toString());
                    intent.putExtra("pre_date", etDate.getText().toString());
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    /**
     * Checks camera permission before opening the camera.
     */
    private void checkPermissionAndGetPhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            getPhoto();
        }
    }

    /**
     * Opens camera or gallery chooser to get receipt image.
     */
    public void getPhoto() {
        String filename = "temp_receipt_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Uri imageUri;

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            File imgFile = File.createTempFile(filename, ".jpg", storageDir);
            currentPath = imgFile.getAbsolutePath();
            imageUri = FileProvider.getUriForFile(this,
                    "com.example.spendsmart.fileprovider", imgFile);
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        } catch (IOException e) {
            return;
        }

        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent chooser = Intent.createChooser(galleryIntent, "Select Source");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePhotoIntent});

        startActivityForResult(chooser, REQUEST_FULL_IMAGE_CAPTURE);
    }

    /**
     * Receives image result from camera or gallery and starts AI analysis.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FULL_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = (data != null && data.getData() != null)
                        ? MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData())
                        : BitmapFactory.decodeFile(currentPath);

                if (bitmap != null) {
                    analyzeReceiptWithAI(bitmap);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}