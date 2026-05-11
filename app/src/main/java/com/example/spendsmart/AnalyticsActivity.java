package com.example.spendsmart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.*;

/**
 * Activity for displaying expense analytics using a pie chart.
 * This screen visualizes the user's expenses by category,
 * fetching data from Firebase and presenting it in a graphical
 * format using MPAndroidChart.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This activity is responsible for displaying financial analytics
 *        based on the user's stored expenses. It retrieves all expense
 *        data from Firebase Realtime Database, groups expenses by category,
 *        and calculates total spending per category. The data is then
 *        visualized using a PieChart. The activity also includes a bottom
 *        navigation bar that allows the user to move between the main
 *        sections of the application such as expenses, history, profile,
 *        and analytics.
 */
public class AnalyticsActivity extends AppCompatActivity {

    /**
     * Initializes the analytics screen.
     * This method sets the layout, configures bottom navigation,
     * and initializes the pie chart that displays expense data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        setupBottomNavigation();

        PieChart pieChart = findViewById(R.id.pieChart);
        setupChart(pieChart);
    }

    /**
     * Configures the bottom navigation bar.
     * This method defines navigation between the main sections
     * of the application and ensures the current screen is highlighted.
     */
    private void setupBottomNavigation() {

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_graphs);

        bottomNav.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.nav_expenses) {
                startActivity(new Intent(this, ExpenseTypeActivity.class));
                finish();
                return true;
            }

            else if (itemId == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                finish();
                return true;
            }

            else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }

            else if (itemId == R.id.nav_graphs) {
                return true;
            }

            return false;
        });
    }

    /**
     * Loads expense data from Firebase and prepares it for visualization.
     * This method retrieves all user expenses, groups them by category,
     * and calculates total spending per category before rendering the chart.
     */
    private void setupChart(PieChart pieChart) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("expenses")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Map<String, Double> totals = new HashMap<>();

                        for (DataSnapshot child : snapshot.getChildren()) {

                            String category = child.child("category").getValue(String.class);
                            Double amount = child.child("amount").getValue(Double.class);

                            if (amount == null) {
                                Long longVal = child.child("amount").getValue(Long.class);
                                if (longVal != null) {
                                    amount = longVal.doubleValue();
                                }
                            }

                            if (category != null && amount != null) {
                                totals.put(category,
                                        totals.getOrDefault(category, 0.0) + amount);
                            }
                        }

                        if (!totals.isEmpty()) {
                            renderChart(pieChart, totals);
                        } else {
                            pieChart.setNoDataText("No data to display");
                            pieChart.invalidate();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("DEBUG_CHART", error.getMessage());
                    }
                });
    }

    /**
     * Renders the pie chart using categorized expense data.
     * This method converts the calculated totals into chart entries
     * and applies styling such as colors, animation, and percentage view.
     */
    private void renderChart(PieChart pieChart, Map<String, Double> totals) {

        List<PieEntry> entries = new ArrayList<>();

        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Spending");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
}