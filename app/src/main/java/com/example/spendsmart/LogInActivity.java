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

/**
 * Activity for user login.
 * This activity allows the user to log into the SpendSmart application
 * using email and password authentication with Firebase.
 * It also provides navigation to the Sign Up screen and
 * Forgot Password screen.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This activity handles user authentication.
 *        It validates input fields, performs Firebase sign-in,
 *        displays progress while connecting, handles errors
 *        (such as network problems or invalid credentials),
 *        and navigates the user to the main expenses screen
 *        upon successful login.
 */
public class LogInActivity extends AppCompatActivity {

    private EditText eTEmail, eTPass;
    private Button btn_login;
    private TextView tVMsg, tvGoToSignup, tv_forgot_password;

    /**
     * Initializes the activity.
     *
     * This method:
     * - Sets the layout of the login screen.
     * - Connects UI components to their XML IDs.
     * - Sets click listeners for:
     *      - Login button
     *      - Sign up navigation
     *      - Forgot password navigation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

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

        tvGoToSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Handles the user login process.
     *
     * This method:
     * - Retrieves the email and password from input fields.
     * - Validates that the fields are not empty.
     * - Displays a progress dialog while connecting to Firebase.
     * - Attempts to sign in using Firebase Authentication.
     * - Handles success and failure cases.
     * - Navigates to ExpenseTypeActivity upon successful login.
     */
    public void loginUser() {

        String email = eTEmail.getText().toString().trim();
        String pass = eTPass.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            tVMsg.setText("Please enter email and password");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Logging in...");
        pd.show();

        refAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        pd.dismiss();

                        if (task.isSuccessful()) {

                            Log.i("LogInActivity", "signIn:success");
                            FirebaseUser user = refAuth.getCurrentUser();

                            Toast.makeText(LogInActivity.this,
                                    "Login Successful!",
                                    Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LogInActivity.this,
                                    ExpenseTypeActivity.class);
                            startActivity(intent);
                            finish();

                        } else {

                            Exception exp = task.getException();
                            Log.e("LogInActivity", "signIn:failure", exp);

                            if (exp instanceof FirebaseNetworkException) {
                                tVMsg.setText("Network error. Check your connection.");
                            } else {
                                tVMsg.setText("Invalid email or password.");
                            }
                        }
                    }
                });
    }
}