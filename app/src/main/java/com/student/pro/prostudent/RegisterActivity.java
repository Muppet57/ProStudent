package com.student.pro.prostudent;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

public class RegisterActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private EditText usernameText,emailText,email_confirmText,passText,pass_confirmText,nameText,surnameText,urlText;
    private Button confirmBut,cancelBut;
    private FirebaseAuth mAuth;
    private String TAG="Tentativa";

    private ProgressBar registerprogress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        usernameText = findViewById(R.id.reg_username);
        emailText = findViewById(R.id.reg_email);
        email_confirmText = findViewById(R.id.reg_confirm_email);
        passText = findViewById(R.id.reg_pass);
        pass_confirmText = findViewById(R.id.reg_confirm_pass);
        nameText = findViewById(R.id.reg_name);
        surnameText = findViewById(R.id.reg_surname);
        urlText = findViewById(R.id.reg_photo_url);
        cancelBut=findViewById(R.id.reg_cancel);
        confirmBut=findViewById(R.id.reg_confirm);

        cancelBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendtoLogin();;
            }
        });
        confirmBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerprogress = (ProgressBar) findViewById(R.id.reg_prog);

                String username = usernameText.getText().toString();
                String email = emailText.getText().toString();
                String email_confirm = email_confirmText.getText().toString();
                String pass = passText.getText().toString();
                String pass_confirm = pass_confirmText.getText().toString();
                String name = nameText.getText().toString();
                String surname = surnameText.getText().toString();
                String url = urlText.getText().toString();

                if(!TextUtils.isEmpty(username) && !TextUtils.isEmpty(email) &&
                        !TextUtils.isEmpty(email_confirm) && !TextUtils.isEmpty(pass) &&
                        !TextUtils.isEmpty(pass_confirm) && !TextUtils.isEmpty(name) &&
                        !TextUtils.isEmpty(surname))
                {
                    registerprogress.setVisibility(View.VISIBLE);
                    if(!TextUtils.equals(email,email_confirm))
                    {
                        //emails not match

                    }
                    if(!TextUtils.equals(pass,pass_confirm))
                    {
                    }
                    else{
                        if(TextUtils.isEmpty(url))
                        {
                            url="https://www.habermanolya.com/wp-content/plugins/all-in-one-seo-pack/images/default-user-image.png";
                        }
                        createAccount(email,pass,username,name,surname,url);
                    }
                }
            }
        });
    }
    @IgnoreExtraProperties
    public static class User {
        public String username, email, name, surname, url;

        public User() { }
        public User(String username, String email, String name, String surname, String url) {
            this.username = username;
            this.email = email;
            this.name = name;
            this.surname = surname;
            this.url = url;
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
       updateUI(currentUser);
    }

    private void createAccount(final String email, String pw, final String username, final String name, final String surname, final String url)
    {
        mAuth.createUserWithEmailAndPassword(email, pw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            String id = mAuth.getCurrentUser().getUid();
                            Log.d(TAG,"Get da ID");

                            User userdb = new User(username,email,name,surname,url);
                            Log.d(TAG,"criação do user");

                            mDatabase.child(id).setValue(userdb);
                            Log.d(TAG,"set do user");
                            updateUI(user);
                            registerprogress.setVisibility(View.INVISIBLE);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser currentUser) {
        if(currentUser!=null)
        {
            sendtoHome();
        }
    }
    private void sendtoHome() {
        Intent intent = new Intent(RegisterActivity.this,HomeActivity.class);
        startActivity(intent);
    }
    private void sendtoLogin() {
        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(intent);
    }
}
