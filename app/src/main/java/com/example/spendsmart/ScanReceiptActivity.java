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
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    private final String TAG = "ReceiptActivity";

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

    private void saveToFirebase() {
        String amountStr = etAmount.getText().toString().trim();
        String dateStr = etDate.getText().toString().trim();
        String selectedCategory = spinnerCategory.getSelectedItem().toString();
        String finalCategory;

        if (selectedCategory.equalsIgnoreCase("Other")) {
            EditText etOther = findViewById(R.id.et_other_category);
            finalCategory = etOther.getText().toString().trim();
            if (finalCategory.isEmpty()) finalCategory = "Other";
        } else {
            finalCategory = selectedCategory;
        }

        if (amountStr.isEmpty() || dateStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            Map<String, Object> expenseValues = new HashMap<>();
            String key = mDatabase.child("expenses").push().getKey();

            if (key != null) {
                expenseValues.put("id", key);
                expenseValues.put("category", finalCategory);
                expenseValues.put("amount", amount);
                expenseValues.put("date", dateStr);
                expenseValues.put("timestamp", System.currentTimeMillis());

                mDatabase.child("expenses").child(key).setValue(expenseValues)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show();

                            // --- עדכון: מעבר למסך ההיסטוריה אחרי שמירה מוצלחת ---
                            Intent intent = new Intent(ScanReceiptActivity.this, HistoryActivity.class);
                            startActivity(intent);

                            finish(); // סוגר את המסך הנוכחי
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(ScanReceiptActivity.this, HistoryActivity.class);
        startActivity(intent);
        finish();
    }

    private void analyzeReceiptWithAI(Bitmap bitmap) {
        iV.setImageBitmap(bitmap);
        iV.setVisibility(View.VISIBLE);
        layoutPlaceholder.setVisibility(View.GONE);
        scanProgress.setVisibility(View.VISIBLE);

        String prompt = "Carefully analyze this image. If it is NOT a receipt or doesn't contain a clear price and date, " +
                "return ONLY the word: NOT_A_RECEIPT. " +
                "Otherwise, extract the total amount, date, and category. " +
                "Return format: Amount: [number], Date: [DD/MM/YYYY], Category: [category name]";

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

    private void parseAndFillFields(String result) {
        if (result.trim().equalsIgnoreCase("NOT_A_RECEIPT")) {
            showErrorDialog("Invalid Image", "This photo doesn't look like a clear receipt.");
            return;
        }

        try {
            boolean foundData = false;

            // 1. חילוץ סכום
            if (result.contains("Amount:")) {
                String amount = result.split("Amount:")[1].split(",")[0].trim().replaceAll("[^\\d.]", "");
                if (!amount.isEmpty()) {
                    etAmount.setText(amount);
                    foundData = true;
                }
            }

            // 2. חילוץ תאריך
            if (result.contains("Date:")) {
                String date = result.split("Date:")[1].split(",")[0].trim();
                etDate.setText(date);
                foundData = true;
            }

            // 3. חילוץ וקביעת קטגוריה - התיקון כאן:
            if (result.contains("Category:")) {
                String aiCategory = result.split("Category:")[1].trim();
                boolean foundInSpinner = false;

                // עוברים על כל הפריטים ב-Spinner כדי למצוא התאמה
                for (int i = 0; i < spinnerCategory.getCount(); i++) {
                    if (spinnerCategory.getItemAtPosition(i).toString().equalsIgnoreCase(aiCategory)) {
                        spinnerCategory.setSelection(i);
                        foundInSpinner = true;
                        break;
                    }
                }

                // אם הקטגוריה מה-AI לא קיימת ב-Spinner, נבחר ב-"Other"
                if (!foundInSpinner) {
                    // נחפש את המיקום של "Other" (בדרך כלל האחרון)
                    for (int i = 0; i < spinnerCategory.getCount(); i++) {
                        if (spinnerCategory.getItemAtPosition(i).toString().equalsIgnoreCase("Other")) {
                            spinnerCategory.setSelection(i);

                            // מציגים וממלאים את שדה ה-"Other" הידני
                            EditText etOther = findViewById(R.id.et_other_category);
                            if (etOther != null) {
                                etOther.setVisibility(View.VISIBLE);
                                etOther.setText(aiCategory);
                            }
                            break;
                        }
                    }
                }
            }

            if (!foundData) {
                showErrorDialog("Analysis Failed", "Could not extract details.");
            }

        } catch (Exception e) {
            showErrorDialog("Error", "Something went wrong while parsing: " + e.getMessage());
        }
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton("Try Again", (dialog, which) -> getPhoto())
                .setNegativeButton("Manual Entry", (dialog, which) -> {
                    // מעבר למסך ההקלדה הידנית (ManualEntryActivity)
                    Intent intent = new Intent(ScanReceiptActivity.this, ManualEntryActivity.class);

                    // שליחת הנתונים שה-AI מצא (אם מצא)
                    intent.putExtra("pre_amount", etAmount.getText().toString());
                    intent.putExtra("pre_date", etDate.getText().toString());

                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void checkPermissionAndGetPhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            getPhoto();
        }
    }

    public void getPhoto() {
        String filename = "temp_receipt_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Uri imageUri;
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File imgFile = File.createTempFile(filename, ".jpg", storageDir);
            currentPath = imgFile.getAbsolutePath();
            imageUri = FileProvider.getUriForFile(this, "com.example.spendsmart.fileprovider", imgFile);
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        } catch (IOException e) { return; }
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePhotoIntent});
        startActivityForResult(chooserIntent, REQUEST_FULL_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data_back) {
        super.onActivityResult(requestCode, resultCode, data_back);
        if (requestCode == REQUEST_FULL_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Bitmap finalBitmap = (data_back != null && data_back.getData() != null) ?
                        MediaStore.Images.Media.getBitmap(this.getContentResolver(), data_back.getData()) :
                        BitmapFactory.decodeFile(currentPath);
                if (finalBitmap != null) analyzeReceiptWithAI(finalBitmap);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}