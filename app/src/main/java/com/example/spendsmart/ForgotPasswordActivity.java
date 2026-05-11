package com.example.spendsmart;

import static com.example.spendsmart.FBRef.refAuth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Activity for password reset.
 * This activity allows the user to request a password reset
 * by entering their email address. It sends a reset link
 * using Firebase Authentication and provides navigation
 * back to the login screen.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This activity enables users to reset their password
 *        in case they forget it. The user enters an email address,
 *        and the application validates the input before sending
 *        a password reset email through Firebase Authentication.
 *        If the request is successful, a reset link is sent
 *        to the user's inbox. The activity also handles errors
 *        such as empty input, invalid email, or network problems,
 *        and allows the user to return to the login screen.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    TextView tvBackToLogin;
    EditText etResetEmail;
    Button btnResetPassword;

    /**
     * Initializes the Forgot Password screen.
     * This method sets the layout, connects UI components
     * to their XML IDs, defines the click action for sending
     * the reset email, and enables navigation back to the
     * login screen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        tvBackToLogin = findViewById(R.id.tv_back_to_login);
        etResetEmail = findViewById(R.id.et_reset_email);
        btnResetPassword = findViewById(R.id.btn_reset_password);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etResetEmail.getText().toString().trim();
                resetPassword(email);
            }
        });

        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Sends a password reset email to the user.
     *
     * This method first checks whether the email field is empty.
     * If it is not empty, it uses Firebase Authentication
     * to send a password reset link to the provided email address.
     * If the operation is successful, a confirmation message
     * is displayed and the screen is closed. If an error occurs,
     * an error message is shown to the user.
     *
     * @param email The email address entered by the user.
     */
    public void resetPassword(String email) {

        if (email.isEmpty()) {
            Toast.makeText(this,
                    "Please enter your email",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        refAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Reset link sent! Check your inbox",
                                    Toast.LENGTH_LONG).show();

                            finish();

                        } else {

                            String error = task.getException().getMessage();

                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Error: " + error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}