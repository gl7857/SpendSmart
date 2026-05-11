package com.example.spendsmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity for user authentication selection.
 * This screen allows the user to choose between
 * signing up for a new account or logging into
 * an existing account.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This activity serves as a navigation screen for authentication.
 *        It provides two options to the user: creating a new account
 *        or logging into an existing one. When the user selects
 *        the Sign Up button, the application opens the SignUpActivity.
 *        When the user selects the Login button, the application
 *        opens the LogInActivity. This screen does not handle
 *        authentication directly but directs the user to the
 *        appropriate activity.
 */
public class AuthActivity extends AppCompatActivity {

    /**
     * Initializes the authentication selection screen.
     * This method sets the layout of the activity,
     * connects the Sign Up and Login buttons to their
     * XML IDs, and defines their click actions to
     * navigate to the corresponding screens.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        Button btnSignUp = findViewById(R.id.button_signup);
        Button btnLogin = findViewById(R.id.button_login);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AuthActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AuthActivity.this, LogInActivity.class);
                startActivity(intent);
            }
        });
    }
}