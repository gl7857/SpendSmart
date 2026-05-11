package com.example.spendsmart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity for selecting expense input type.
 * This screen allows the user to choose how they want to add
 * a new expense, either by scanning a receipt or by manual entry.
 * It also includes a bottom navigation menu for moving
 * between the main sections of the application.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This activity serves as the main expense selection screen.
 *        It provides navigation options using a BottomNavigationView
 *        that allows the user to move to the Profile, History,
 *        and Analytics screens. The current screen is marked
 *        as selected in the navigation menu. Additionally,
 *        the user can choose to either scan a receipt or enter
 *        an expense manually, which opens the corresponding activity.
 */
public class ExpenseTypeActivity extends AppCompatActivity {

    /**
     * Initializes the expense type screen.
     * This method sets the layout, configures the bottom navigation
     * menu and its item selection behavior, and defines the actions
     * for the "Scan Receipt" and "Manual Input" buttons.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_type);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setSelectedItemId(R.id.nav_expenses);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_expenses) return true;

            else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }

            else if (id == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                finish();
                return true;
            }

            else if (id == R.id.nav_graphs) {
                startActivity(new Intent(this, AnalyticsActivity.class));
                finish();
                return true;
            }

            return false;
        });

        Button btnScanReceipt = findViewById(R.id.btn_scan_receipt);
        btnScanReceipt.setOnClickListener(v -> {
            Intent intent = new Intent(ExpenseTypeActivity.this, ScanReceiptActivity.class);
            startActivity(intent);
        });

        Button btnManualInput = findViewById(R.id.btn_manual_input);
        btnManualInput.setOnClickListener(v -> {
            Intent intent = new Intent(ExpenseTypeActivity.this, ManualEntryActivity.class);
            startActivity(intent);
        });
    }
}