package com.student.pro.prostudent.Activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.student.pro.prostudent.R;

public class LoginActivity extends AppCompatActivity {

    //Elements
    private Button login, register;
    private String TAG = "Login", email, pass;
    private EditText emailText, passText;
    private ProgressBar loginprogress;
    //Firebase
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //User
        mAuth = FirebaseAuth.getInstance();

        //Elements
        emailText = findViewById(R.id.email_in);
        passText = findViewById(R.id.pass_in);
        login = findViewById(R.id.login_but);
        register = findViewById(R.id.register_but);

        //Click listener for login submission
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Retrieving user input data
                email = emailText.getText().toString();
                pass = passText.getText().toString();
                loginprogress = (ProgressBar) findViewById(R.id.login_prog);
                // Flag used to verify for errors and focus on the form field where the error came from
                boolean cancel = false;
                View focusView = null;

                //If both fields aren't empty a login attempt is initiated
                if (!TextUtils.isEmpty(pass) && !TextUtils.isEmpty(email)) {
                    //Progress bar is changed to visible
                    loginprogress.setVisibility(View.VISIBLE);
                    //Call function to attempt login
                    tryLogin(email, pass);

                }
                if (TextUtils.isEmpty(pass)) {
                    //Shows a warning sign specifying that the field is required
                    passText.setError(getString(R.string.error_field_required));
                    //sets the focus for passText
                    focusView = passText;
                    cancel = true;
                }
                if (TextUtils.isEmpty(email)) {
                    //Shows a warning sign specifying that the field is required
                    emailText.setError(getString(R.string.error_field_required));
                    //sets the focus for emailText
                    focusView = emailText;
                    cancel = true;
                }
                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start of a new activity to create a new account
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    //If the token is present at the start of the activity the user is automatically logged in
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }


    private void tryLogin(String em, String pw) {
        //Firebase sign in with emal and password
        mAuth.signInWithEmailAndPassword(em, pw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Listens for the task success
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(LoginActivity.this, R.string.authentication_success,
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            //We destroy the login activity because we aren't coming back to it once we log in
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, R.string.authentication_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        //Returning the progress bar to invisible
                        loginprogress.setVisibility(View.INVISIBLE);
                    }
                });


    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            sendtoHome();
        }
    }

    private void sendtoHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}