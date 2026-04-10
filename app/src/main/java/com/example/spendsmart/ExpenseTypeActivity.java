package com.example.spendsmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ExpenseTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_type);

        // 1. כפתור סריקה - עובר למסך ה-AI (ScanReceiptActivity)
        Button btnScanReceipt = findViewById(R.id.btn_scan_receipt);
        btnScanReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExpenseTypeActivity.this, ScanReceiptActivity.class);
                startActivity(intent);
            }
        });

        // 2. כפתור הזנה ידנית - עובר למסך הידני (ManualEntryActivity)
        Button btnManualInput = findViewById(R.id.btn_manual_input);
        btnManualInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExpenseTypeActivity.this, ManualEntryActivity.class);
                startActivity(intent);
            }
        });

        // 3. כפתור ביטול (TextView) - חזרה למסך הקודם
        TextView tvBack = findViewById(R.id.tv_back_from_choice);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // סוגר את המסך הנוכחי
            }
        });
    }
}