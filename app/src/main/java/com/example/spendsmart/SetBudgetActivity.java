package com.example.spendsmart;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for setting the user's budget.
 * This screen allows the user to define budget limits per category
 * and calculate a total monthly budget. The data is then saved
 * to Firebase under the current user and updated in the user session.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This activity enables the user to create and manage
 *        their personal budget. The user can dynamically add or
 *        remove budget categories, enter category names and amounts,
 *        and save the data to Firebase. The activity calculates the
 *        total monthly budget automatically based on the entered values,
 *        stores it in the database, and updates the current user session.
 *        It also provides navigation to other sections of the application
 *        using a bottom navigation bar.
 */
public class SetBudgetActivity extends AppCompatActivity {

    private LinearLayout containerBudgets;
    private ImageButton btnAddCategory, btnRemoveCategory;
    private Button btnSave;

    /**
     * Initializes the Set Budget screen.
     * This method sets the layout, connects UI components,
     * initializes bottom navigation, and defines actions for
     * adding, removing, and saving budget categories.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        containerBudgets = findViewById(R.id.container_budgets);
        btnAddCategory = findViewById(R.id.btn_add_category);
        btnRemoveCategory = findViewById(R.id.btn_remove_category);
        btnSave = findViewById(R.id.btn_save_budget);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_expenses)
                startActivity(new Intent(this, ExpenseTypeActivity.class));

            else if (id == R.id.nav_history)
                startActivity(new Intent(this, HistoryActivity.class));

            else if (id == R.id.nav_graphs)
                startActivity(new Intent(this, AnalyticsActivity.class));

            else if (id == R.id.nav_profile)
                startActivity(new Intent(this, ProfileActivity.class));

            finish();
            return true;
        });

        btnAddCategory.setOnClickListener(v -> addNewBudgetRow());
        btnRemoveCategory.setOnClickListener(v -> removeLastBudgetRow());
        btnSave.setOnClickListener(v -> saveToFirebase());
    }

    /**
     * Adds a new budget input row to the screen.
     * Each row contains a category field and an amount field,
     * allowing the user to dynamically define multiple budgets.
     */
    private void addNewBudgetRow() {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(-1, -2);
        rowParams.setMargins(0, 0, 0, 20);
        row.setLayoutParams(rowParams);

        EditText etCategory = new EditText(this);
        etCategory.setLayoutParams(new LinearLayout.LayoutParams(0, 150, 6));
        etCategory.setHint("Category");
        etCategory.setBackgroundResource(R.drawable.edit_text_border);
        etCategory.setPadding(30, 0, 30, 0);

        EditText etAmount = new EditText(this);
        etAmount.setLayoutParams(new LinearLayout.LayoutParams(0, 150, 4));
        etAmount.setHint("₪ Amount");
        etAmount.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etAmount.setBackgroundResource(R.drawable.edit_text_border);
        etAmount.setPadding(30, 0, 30, 0);

        row.addView(etCategory);
        row.addView(etAmount);
        containerBudgets.addView(row);
    }

    /**
     * Removes the last budget row from the screen.
     * This allows the user to delete the most recently added category.
     */
    private void removeLastBudgetRow() {
        if (containerBudgets.getChildCount() > 0) {
            containerBudgets.removeViewAt(containerBudgets.getChildCount() - 1);
        }
    }

    /**
     * Saves the budget data to Firebase.
     * This method collects all category inputs and amounts,
     * calculates the total monthly budget, validates the data,
     * and stores it under the current user's record in Firebase.
     * It also updates the local user session with the new values.
     */
    private void saveToFirebase() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef =
                FirebaseDatabase.getInstance().getReference("users").child(uid);

        Map<String, Double> budgetMap = new HashMap<>();
        double totalBudget = 0;
        boolean hasData = false;

        for (int i = 0; i < containerBudgets.getChildCount(); i++) {

            LinearLayout row = (LinearLayout) containerBudgets.getChildAt(i);
            EditText etCat = (EditText) row.getChildAt(0);
            EditText etAmt = (EditText) row.getChildAt(1);

            String cat = etCat.getText().toString().trim();
            String amtStr = etAmt.getText().toString().trim();

            if (!cat.isEmpty() && !amtStr.isEmpty()) {
                try {
                    double amt = Double.parseDouble(amtStr);
                    budgetMap.put(cat, amt);
                    totalBudget += amt;
                    hasData = true;
                } catch (NumberFormatException e) {
                    Toast.makeText(this,
                            "Invalid amount in " + cat,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        if (hasData) {

            Map<String, Object> updates = new HashMap<>();
            updates.put("categoryTotals", budgetMap);
            updates.put("monthlyLimit", totalBudget);

            double finalTotalBudget = totalBudget;

            userRef.updateChildren(updates).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {

                    User currentUser = UserSession.getCurrentUser();
                    if (currentUser != null) {
                        currentUser.categoryTotals = budgetMap;
                        currentUser.monthlyLimit = finalTotalBudget;
                        UserSession.setCurrentUser(currentUser);
                    }

                    Toast.makeText(this,
                            "Budget saved! Total limit: " + finalTotalBudget,
                            Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this,
                            "Failed to save.",
                            Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(this,
                    "Please fill in fields",
                    Toast.LENGTH_SHORT).show();
        }
    }
}