package com.example.spendsmart;

import static com.example.spendsmart.FBRef.refAuth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Activity for user registration (Sign Up).
 * This activity allows new users to create an account
 * using email and password authentication with Firebase.
 * It also saves additional user information (full name and balance)
 * in the Firebase Realtime Database.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This activity enables new user registration.
 *        It validates input fields, checks password confirmation,
 *        creates a new Firebase Authentication account,
 *        stores user data in the database, manages loading progress,
 *        and navigates the user to the budget setup screen
 *        upon successful registration.
 */
public class SignUpActivity extends AppCompatActivity {

    private EditText eTEmail, eTPass, eTCon, eTName;
    private Button btn_register;
    private TextView tVMsg, tvBackToLogin;

    /**
     * Initializes the Sign Up screen.
     *
     * This method:
     * - Sets the layout of the registration screen.
     * - Connects UI components to their XML IDs.
     * - Sets click listeners for:
     *      - Register button
     *      - Back to Login navigation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        eTEmail = findViewById(R.id.et_email);
        eTPass = findViewById(R.id.et_password);
        eTCon = findViewById(R.id.et_confirm_password);
        eTName = findViewById(R.id.et_full_name);
        btn_register = findViewById(R.id.btn_register);
        tVMsg = findViewById(R.id.tv_msg);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);

        btn_register.setOnClickListener(v -> createUser());

        tvBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LogInActivity.class));
            finish();
        });
    }

    /**
     * Creates a new user account.
     *
     * This method:
     * - Retrieves user input (email, password, confirmation, name).
     * - Validates that all fields are filled.
     * - Checks that passwords match.
     * - Displays a progress dialog while processing.
     * - Creates a Firebase Authentication user.
     * - Stores additional user data in Firebase Realtime Database.
     * - Initializes the user session.
     * - Navigates to SetBudgetActivity upon success.
     */
    public void createUser() {

        String email = eTEmail.getText().toString().trim();
        String pass = eTPass.getText().toString().trim();
        String conPass = eTCon.getText().toString().trim();
        String name = eTName.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty() || name.isEmpty()) {
            tVMsg.setText("Please fill all fields");
            return;
        }

        if (!pass.equals(conPass)) {
            tVMsg.setText("Passwords do not match");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Creating user...");
        pd.show();

        refAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser firebaseUser = refAuth.getCurrentUser();
                        String uid = firebaseUser.getUid();

                        User newUser = new User(uid, name, 0.0);

                        FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(uid)
                                .setValue(newUser)
                                .addOnCompleteListener(dbTask -> {

                                    pd.dismiss();

                                    if (dbTask.isSuccessful()) {

                                        // Initialize current user session
                                        UserSession.setCurrentUser(newUser);

                                        Toast.makeText(SignUpActivity.this,
                                                "Registration Successful!",
                                                Toast.LENGTH_SHORT).show();

                                        startActivity(new Intent(SignUpActivity.this,
                                                SetBudgetActivity.class));
                                        finish();

                                    } else {
                                        tVMsg.setText("Database error: "
                                                + dbTask.getException().getMessage());
                                    }
                                });

                    } else {
                        pd.dismiss();
                        tVMsg.setText("Error: "
                                + task.getException().getMessage());
                    }
                });
    }
}