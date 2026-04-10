package com.example.spendsmart;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SetBudgetActivity extends AppCompatActivity {
    private LinearLayout containerBudgets;
    private ImageButton btnAddCategory;
    private ImageButton btnRemoveCategory;
    private ImageButton btnGoToProfile;
    private Button btnSave;
    private Button btnContinue; // *** הוספנו משתנה לכפתור ה-Continue ***

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        // אתחול הרכיבים מה-XML
        containerBudgets = findViewById(R.id.container_budgets);
        btnAddCategory = findViewById(R.id.btn_add_category);
        btnRemoveCategory = findViewById(R.id.btn_remove_category);
        btnSave = findViewById(R.id.btn_save_budget);
        btnGoToProfile = findViewById(R.id.btn_go_to_profile);
        btnContinue = findViewById(R.id.btn_continue_to_type); // *** קישור לכפתור החדש מה-XML ***

        // ליסנר למעבר למסך פרופיל
        btnGoToProfile.setOnClickListener(v -> {
            Intent intent = new Intent(SetBudgetActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // *** ליסנר למעבר למסך בחירת סוג הוצאה ***
        btnContinue.setOnClickListener(v -> {
            // מעבר למסך ExpenseTypeActivity
            Intent intent = new Intent(SetBudgetActivity.this, ExpenseTypeActivity.class);
            startActivity(intent);
            // אופציונלי: finish(); אם את רוצה שלא יוכלו לחזור למסך התקציב
        });

        // הוספת שורה חדשה
        btnAddCategory.setOnClickListener(v -> addNewBudgetRow());

        // מחיקת שורה אחרונה
        btnRemoveCategory.setOnClickListener(v -> removeLastBudgetRow());

        btnSave.setOnClickListener(v -> saveToFirebase());
    }

    private void addNewBudgetRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, 20);
        row.setLayoutParams(rowParams);
        row.setWeightSum(10);

        EditText etCategory = new EditText(this);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, 150, 6);
        p1.setMargins(0, 0, 20, 0);
        etCategory.setLayoutParams(p1);
        etCategory.setHint("Category");
        etCategory.setBackgroundResource(R.drawable.edit_text_border);
        etCategory.setPadding(30, 0, 30, 0);

        EditText etAmount = new EditText(this);
        etAmount.setLayoutParams(new LinearLayout.LayoutParams(0, 150, 4));
        etAmount.setHint("₪ Amount");
        etAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etAmount.setBackgroundResource(R.drawable.edit_text_border);
        etAmount.setPadding(30, 0, 30, 0);

        row.addView(etCategory);
        row.addView(etAmount);
        containerBudgets.addView(row);
    }

    private void removeLastBudgetRow() {
        int count = containerBudgets.getChildCount();
        if (count > 0) {
            containerBudgets.removeViewAt(count - 1);
        } else {
            Toast.makeText(this, "No rows to remove", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToFirebase() {
        Toast.makeText(this, "Saving dynamic budgets...", Toast.LENGTH_SHORT).show();
    }
} 