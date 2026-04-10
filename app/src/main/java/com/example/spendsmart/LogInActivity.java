package com.example.spendsmart;

import static com.example.spendsmart.FBRef.refAuth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class LogInActivity extends AppCompatActivity {

    private EditText eTEmail, eTPass;
    private Button btn_login;
    private TextView tVMsg, tvGoToSignup, tv_forgot_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in); // וודא שזה שם ה-XML של מסך ההתחברות

        // קישור רכיבים לפי ה-IDs ב-XML החדש
        eTEmail = findViewById(R.id.et_login_email);
        eTPass = findViewById(R.id.et_login_password);
        btn_login = findViewById(R.id.btn_login);
        tVMsg = findViewById(R.id.tv_login_msg);
        tvGoToSignup = findViewById(R.id.tv_go_to_signup);
        tv_forgot_password = findViewById(R.id.tv_forgot_password);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        tv_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);

                finish();
            }
        });

        // מעבר למסך הרשמה
        tvGoToSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
                startActivity(intent);
                // לא חייב finish() כאן כדי שהמשתמש יוכל לחזור אחורה
            }
        });
    }

    public void loginUser() {
        String email = eTEmail.getText().toString().trim();
        String pass = eTPass.getText().toString().trim();

        // בדיקה שהשדות לא ריקים
        if (email.isEmpty() || pass.isEmpty()) {
            tVMsg.setText("Please enter email and password");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Logging in...");
        pd.show();

        // שימוש בפונקציית כניסה (Sign In) ולא יצירה
        refAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pd.dismiss();
                        if (task.isSuccessful()) {
                            Log.i("LogInActivity", "signIn:success");
                            FirebaseUser user = refAuth.getCurrentUser();

                            Toast.makeText(LogInActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                            // מעבר למסך הראשי
                            Intent intent = new Intent(LogInActivity.this, ExpenseTypeActivity.class);
                            startActivity(intent);
                            finish(); // סוגר את מסך הלוגין כדי שלא יחזרו אליו בלחיצה על "Back"
                        } else {
                            Exception exp = task.getException();
                            Log.e("LogInActivity", "signIn:failure", exp);

                            if (exp instanceof FirebaseNetworkException) {
                                tVMsg.setText("Network error. Check your connection.");
                            } else {
                                // בדרך כלל טעות במייל או בסיסמה
                                tVMsg.setText("Invalid email or password.");
                            }
                        }
                    }
                });
    }
}