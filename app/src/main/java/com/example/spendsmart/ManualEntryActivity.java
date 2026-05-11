package com.example.spendsmart;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

/**
 * Activity for manually adding a new expense.
 * This screen allows the user to enter expense details such as
 * amount, category, date, and description, and then save the
 * expense to Firebase Realtime Database under the logged-in user.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This activity enables users to manually create and save
 *        an expense. It provides input fields for amount, category,
 *        date, and description. The date field opens a DatePicker
 *        dialog to simplify selection. Before saving, the activity
 *        verifies that the user is logged in and that the required
 *        fields are filled correctly. The expense is then stored
 *        in the Firebase Realtime Database under the user's unique
 *        ID. Upon successful saving, the user is redirected to
 *        the History screen.
 */
public class ManualEntryActivity extends AppCompatActivity {

    private EditText etAmount, etDesc, etDate;
    private Spinner spinnerCategory;
    private DatabaseReference mDatabase;

    /**
     * Initializes the manual expense entry screen.
     * This method sets the layout, connects UI components,
     * initializes the Firebase database reference, sets up
     * the category spinner, handles optional pre-filled data,
     * enables the date picker dialog, and defines the save action.
     */
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

            if (preAmount != null && !preAmount.isEmpty())
                etAmount.setText(preAmount);

            if (preDate != null && !preDate.isEmpty())
                etDate.setText(preDate);
        }

        etDate.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btn_save_manual).setOnClickListener(v -> saveExpense());
    }

    /**
     * Sets up the expense category dropdown menu.
     * This method defines the available categories and
     * connects them to the Spinner using an ArrayAdapter.
     */
    private void setupCategorySpinner() {
        String[] categories = {"Groceries", "Transport", "Entertainment", "Health", "Shopping", "Other"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    /**
     * Displays a DatePicker dialog to allow the user
     * to select a date easily. The selected date is
     * then displayed inside the date input field.
     */
    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) ->
                        etDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1),
                year, month, day);

        datePickerDialog.show();
    }

    /**
     * Saves the expense to Firebase under the current user.
     * This method verifies that a user is logged in,
     * validates required fields, converts the amount to a number,
     * creates an Expense object, generates a unique ID,
     * and stores the data inside the database.
     * If the operation succeeds, the user is redirected
     * to the History screen.
     */
    private void saveExpense() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Error: User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

            DatabaseReference userExpensesRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .child("expenses");

            String expenseId = userExpensesRef.push().getKey();

            if (expenseId != null) {

                newExpense.setId(expenseId);

                userExpensesRef.child(expenseId).setValue(newExpense)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, HistoryActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this,
                                        "Failed to save: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());
            }

        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
        }
    }
}