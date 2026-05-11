package com.example.spendsmart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying user's expense history.
 * This screen shows all saved expenses retrieved from Firebase,
 * allows filtering by month, and presents the data in a list view.
 * It also includes bottom navigation for moving between app sections.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This activity is responsible for displaying the user's
 *        full expense history. It retrieves expense data from
 *        Firebase Realtime Database under the current user,
 *        filters it by selected month using a spinner, and
 *        displays the results in a ListView. The activity also
 *        filters out outdated data by year and ensures that
 *        only relevant expenses are shown. Additionally, it
 *        provides navigation to other sections of the app such
 *        as expenses input, analytics, and user profile.
 */
public class HistoryActivity extends AppCompatActivity {

    private ListView lvExpenses;
    private Spinner monthSpinner;
    private DatabaseReference mDatabase;
    private List<Expense> allExpenses = new ArrayList<>();
    private ArrayList<String> displayList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    /**
     * Initializes the history screen.
     * This method sets the layout, checks user authentication,
     * initializes Firebase reference, sets up the ListView adapter,
     * configures the month filter spinner, and loads expenses
     * from the database.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        lvExpenses = findViewById(R.id.lv_expenses);
        monthSpinner = findViewById(R.id.month_spinner);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("expenses");

        setupBottomNavigation();

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                displayList);

        lvExpenses.setAdapter(adapter);

        setupMonthSpinner();
        loadExpensesFromFirebase();
    }

    /**
     * Configures the bottom navigation bar.
     * This method allows navigation between History, Expenses,
     * Analytics, and Profile screens while keeping the current
     * screen selected.
     */
    private void setupBottomNavigation() {

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_history);

        bottomNav.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.nav_history)
                return true;

            else if (itemId == R.id.nav_expenses)
                startActivity(new Intent(this, ExpenseTypeActivity.class));

            else if (itemId == R.id.nav_graphs)
                startActivity(new Intent(this, AnalyticsActivity.class));

            else if (itemId == R.id.nav_profile)
                startActivity(new Intent(this, ProfileActivity.class));

            finish();
            return true;
        });
    }

    /**
     * Sets up the month filter spinner.
     * This method allows the user to filter displayed expenses
     * by month or view all expenses.
     */
    private void setupMonthSpinner() {

        String[] months = {"All Months", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                months);

        monthSpinner.setAdapter(monthAdapter);

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterExpensesByMonth(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Loads all expenses from Firebase for the current user.
     * This method listens to database changes, retrieves expense data,
     * filters outdated entries by year, and updates the local list.
     */
    private void loadExpensesFromFirebase() {

        mDatabase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                allExpenses.clear();
                int currentYear = java.util.Calendar.getInstance()
                        .get(java.util.Calendar.YEAR);

                for (DataSnapshot data : snapshot.getChildren()) {

                    try {
                        String cat = String.valueOf(data.child("category").getValue());
                        String dt = String.valueOf(data.child("date").getValue());
                        String amt = String.valueOf(data.child("amount").getValue());

                        if (!cat.equals("null") && !dt.equals("null")) {

                            String[] parts = dt.split("/");

                            if (parts.length >= 3 &&
                                    Integer.parseInt(parts[2]) >= currentYear) {

                                allExpenses.add(new Expense(
                                        cat,
                                        Double.parseDouble(amt),
                                        dt));
                            }
                        }

                    } catch (Exception e) {
                        Log.e("HistoryDB", e.getMessage());
                    }
                }

                filterExpensesByMonth(monthSpinner.getSelectedItem().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Filters expenses by selected month and updates the ListView.
     * If "All Months" is selected, all expenses are shown.
     */
    private void filterExpensesByMonth(String monthName) {

        displayList.clear();

        if (monthName.equals("All Months")) {

            for (Expense exp : allExpenses) {
                displayList.add(exp.getCategory() + ": ₪" + exp.getAmount()
                        + "\n📅 " + exp.getDate());
            }

        } else {

            String targetMonth = getMonthNumber(monthName);

            for (Expense exp : allExpenses) {

                String[] parts = exp.getDate().split("/");

                if (parts.length >= 2 &&
                        parts[1].trim().equals(targetMonth)) {

                    displayList.add(exp.getCategory() + ": ₪" + exp.getAmount()
                            + "\n📅 " + exp.getDate());
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Converts month name to numeric representation.
     */
    private String getMonthNumber(String monthName) {

        String[] months = {"", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        for (int i = 1; i < months.length; i++) {
            if (months[i].equalsIgnoreCase(monthName)) {
                return String.valueOf(i);
            }
        }

        return "0";
    }
}