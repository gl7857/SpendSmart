package com.example.spendsmart;

import static com.example.spendsmart.FBRef.refAuth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button; // הוספתי את זה
import android.widget.EditText; // הוספתי את זה
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class ForgotPasswordActivity extends AppCompatActivity {

    // הגדרת הרכיבים
    TextView tvBackToLogin;
    EditText etResetEmail; // תיבת הטקסט של המייל
    Button btnResetPassword; // הכפתור ששולח את המייל

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // קישור הרכיבים ל-IDs מה-XML (וודאי שה-IDs תואמים ל-XML שלך)
        tvBackToLogin = findViewById(R.id.tv_back_to_login);
        etResetEmail = findViewById(R.id.et_reset_email);
        btnResetPassword = findViewById(R.id.btn_reset_password);

        // הגדרת לחיצה על כפתור האיפוס
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // שליפת המייל שהמשתמש הקליד
                String email = etResetEmail.getText().toString().trim();

                // הפעלת פונקציית האיפוס
                resetPassword(email);
            }
        });

        // חזרה למסך לוגין
        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // סוגר את המסך הנוכחי וחוזר אחורה
            }
        });
    }

    public void resetPassword(String email) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        // כאן קורה הקסם מול Firebase Console
        refAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // אם המייל קיים במערכת, Firebase ישלח הודעה
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Reset link sent! Check your inbox", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            // אם הייתה שגיאה (למשל מייל לא רשום או בעיית אינטרנט)
                            String error = task.getException().getMessage();
                            Toast.makeText(ForgotPasswordActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}