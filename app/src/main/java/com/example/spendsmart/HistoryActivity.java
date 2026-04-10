package com.example.spendsmart;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView lvExpenses;
    private Spinner monthSpinner;
    private DatabaseReference mDatabase;
    private List<Expense> allExpenses = new ArrayList<>();
    private ArrayList<String> displayList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        lvExpenses = findViewById(R.id.lv_expenses);
        monthSpinner = findViewById(R.id.month_spinner);

        // הנתיב ל-Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("expenses");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        lvExpenses.setAdapter(adapter);

        setupMonthSpinner();
        loadExpensesFromFirebase();
    }

    private void setupMonthSpinner() {
        String[] months = {"All Months", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, months);
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

    private void loadExpensesFromFirebase() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allExpenses.clear();

                // 1. בדיקה אם הנתיב בכלל קיים
                if (!snapshot.exists()) {
                    Toast.makeText(HistoryActivity.this, "DEBUG: Path 'expenses' NOT found!", Toast.LENGTH_LONG).show();
                    return;
                }

                // 2. כמה ילדים יש בתוך התיקייה?
                long count = snapshot.getChildrenCount();
                Toast.makeText(HistoryActivity.this, "DEBUG: Found " + count + " items", Toast.LENGTH_SHORT).show();

                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        // 3. הדפסה ללוג כדי לראות את השמות המדויקים
                        Log.d("HistoryDB", "Full Object: " + data.toString());

                        // נסיון שליפה הכי גנרי שיש
                        String cat = String.valueOf(data.child("category").getValue());
                        String dt = String.valueOf(data.child("date").getValue());
                        String amt = String.valueOf(data.child("amount").getValue());

                        // אם השליפה הצליחה (גם אם זה לא Expense Class מושלם)
                        if (!cat.equals("null") && !dt.equals("null")) {
                            allExpenses.add(new Expense(cat, Double.parseDouble(amt), dt));
                        }
                    } catch (Exception e) {
                        Log.e("HistoryDB", "Error reading item: " + e.getMessage());
                    }
                }

                // 4. מעבר לסינון
                filterExpensesByMonth(monthSpinner.getSelectedItem().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistoryActivity.this, "DB Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterExpensesByMonth(String monthName) {
        displayList.clear();

        if (monthName.equals("All Months")) {
            for (Expense exp : allExpenses) {
                displayList.add(exp.getCategory() + ": ₪" + exp.getAmount() + "\n📅 " + exp.getDate());
            }
        } else {
            int targetMonthInt = Integer.parseInt(getMonthNumber(monthName));

            for (Expense exp : allExpenses) {
                String date = exp.getDate();
                if (date != null) {
                    // מנקה רווחים ומפצל לפי לוכסן
                    String[] parts = date.trim().split("/");
                    if (parts.length >= 2) {
                        try {
                            // בודק אם החלק השני (חודש) תואם למטרה
                            int expenseMonthInt = Integer.parseInt(parts[1].trim());
                            if (expenseMonthInt == targetMonthInt) {
                                displayList.add(exp.getCategory() + ": ₪" + exp.getAmount() + "\n📅 " + exp.getDate());
                            }
                        } catch (Exception e) {
                            Log.e("HistoryFilter", "Date error: " + date);
                        }
                    }
                }
            }
        }

        if (displayList.isEmpty()) {
            displayList.add("No expenses found for " + monthName);
        }
        adapter.notifyDataSetChanged();
    }

    private String getMonthNumber(String monthName) {
        String[] months = {"", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        for (int i = 1; i < months.length; i++) {
            if (months[i].equalsIgnoreCase(monthName)) return String.valueOf(i);
        }
        return "0";
    }
}