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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private EditText eTEmail, eTPass, eTCon;
    private Button btn_register;
    private TextView tVMsg;
    private TextView tvBackToLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        eTEmail = findViewById(R.id.et_email);
        eTPass = findViewById(R.id.et_password);
        eTCon = findViewById(R.id.et_confirm_password);
        btn_register = findViewById(R.id.btn_register);
        tVMsg = findViewById(R.id.tv_msg);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createUser(v);
            }
        });

        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת Intent למעבר למסך ההתחברות
                Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
                startActivity(intent);

                // אופציונלי: finish() יסגור את מסך ההרשמה כדי שלא יחזרו אליו בלחיצה על "חזור"
                finish();
            }
        });
    }

    public void createUser(View view) {
        String email = eTEmail.getText().toString().trim();
        String pass = eTPass.getText().toString().trim();
        String conPass = eTCon.getText().toString().trim();

        // בדיקות תקינות קלט בסיסיות
        if (email.isEmpty() || pass.isEmpty() || conPass.isEmpty()) {
            tVMsg.setText("Please fill all fields");
            return;
        }

        if (!pass.equals(conPass)) {
            tVMsg.setText("Passwords do not match");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Creating user...");
        pd.show();

        refAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pd.dismiss();
                        if (task.isSuccessful()) {
                            Log.i("SignUpActivity", "createUser:success");

                            Toast.makeText(SignUpActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                            // --- כאן השינוי המרכזי ---
                            // מעבר למסך הגדרת תקציבים (SetBudgetActivity)
                            Intent intent = new Intent(SignUpActivity.this, SetBudgetActivity.class);
                            startActivity(intent);

                            // סגירת מסך ההרשמה כדי שלא יהיה אפשר לחזור אליו בכפתור "חזור"
                            finish();
                            // -------------------------

                        } else {
                            Exception exp = task.getException();
                            if (exp instanceof FirebaseAuthWeakPasswordException) {
                                tVMsg.setText("Password too weak (min 6 chars).");
                            } else if (exp instanceof FirebaseAuthUserCollisionException) {
                                tVMsg.setText("User already exists.");
                            } else if (exp instanceof FirebaseNetworkException) {
                                tVMsg.setText("Network error.");
                            } else {
                                tVMsg.setText("Error: " + exp.getMessage());
                            }
                        }
                    }
                });
    }
}