package com.example.spendsmart;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class ManualEntryActivity extends AppCompatActivity {

    private EditText etAmount, etDesc, etDate;
    private Spinner spinnerCategory;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        etAmount = findViewById(R.id.et_manual_amount);
        etDesc = findViewById(R.id.et_manual_desc);
        etDate = findViewById(R.id.et_manual_date);
        spinnerCategory = findViewById(R.id.spinner_manual_category);

        setupCategorySpinner();

        if (getIntent() != null) {
            String preAmount = getIntent().getStringExtra("pre_amount");
            String preDate = getIntent().getStringExtra("pre_date");

            if (preAmount != null && !preAmount.isEmpty()) etAmount.setText(preAmount);
            if (preDate != null && !preDate.isEmpty()) etDate.setText(preDate);
        }

        etDate.setOnClickListener(v -> showDatePicker());

        findViewById(R.id.btn_save_manual).setOnClickListener(v -> saveExpense());
    }

    private void setupCategorySpinner() {
        String[] categories = {"Groceries", "Transport", "Entertainment", "Health", "Shopping", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    etDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveExpense() {
        String amountStr = etAmount.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String date = etDate.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        if (amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please enter amount and date", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            Expense newExpense = new Expense(category, amount, date);

            String expenseId = mDatabase.child("expenses").push().getKey();

            if (expenseId != null) {
                newExpense.setId(expenseId);

                mDatabase.child("expenses").child(expenseId).setValue(newExpense)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ManualEntryActivity.this, "Saved Successfully!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(ManualEntryActivity.this, HistoryActivity.class);
                            startActivity(intent);

                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ManualEntryActivity.this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
        }

    }
}