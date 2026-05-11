package com.example.spendsmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Main entry activity of the SpendSmart application.
 * This screen is displayed when the app starts and provides
 * a "Get Started" button that navigates the user to the
 * authentication screen.
 *
 * @author      Gali Lavi <gl7857@bs.amalnet.k12.il>
 * @version     1.0
 * @since       11/05/2026
 *
 * short description:
 *        This activity serves as the launch screen of the application.
 *        It presents the main interface to the user and includes
 *        a button that directs the user to the authentication
 *        process. When the user clicks the "Get Started" button,
 *        the application opens the AuthActivity to allow login
 *        or registration.
 */
public class MainActivity extends AppCompatActivity {

    Button btn_get_started;

    /**
     * Initializes the main screen.
     * This method sets the layout of the activity,
     * connects the "Get Started" button to its XML ID,
     * and defines its click behavior to navigate
     * to the authentication screen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_get_started = findViewById(R.id.btn_get_started);

        btn_get_started.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                startActivity(intent);
            }
        });
    }
}