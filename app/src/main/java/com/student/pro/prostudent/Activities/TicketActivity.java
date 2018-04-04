package com.student.pro.prostudent.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.student.pro.prostudent.Objects.Tickets;
import com.student.pro.prostudent.R;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TicketActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Toolbar & Nav
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
   //Elements
    private ImageView attach;
    private EditText title, content;
    private Button sendBut;
    private ProgressBar ticket_bar;
    private Switch sPrivate;
    //Firebase
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    //Variables
    private String TAG = "Ticketlog";
    private String UserID, discipline, disc_key, ticket_key;
    private Uri filePath = null, imageURL = null;
    private static int RESULT_LOAD_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        //Check for Extras
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            discipline = intent.getStringExtra("Discipline");
            disc_key = intent.getStringExtra("ID");

        } else {
             /*
              This page needs to inherit information about the discipline selected
              if no information is found the user is returned to the home page
             */
            sendtoHome();
        }

        //Toolbar & Drawer
        setNavigationViewListener();
        mToolbar = findViewById(R.id.nav_action);
        setSupportActionBar(mToolbar);
        mDrawerLayout = findViewById(R.id.drawerlayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setTitle(R.string.action_send_ticket);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Instances
        mDatabase = FirebaseDatabase.getInstance().getReference("tickets");
        mStorageRef = FirebaseStorage.getInstance().getReference("tickets");
        //User
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        UserID = user.getUid().toString();
        //Elements
        ticket_bar = findViewById(R.id.ticket_bar);
        title = findViewById(R.id.ticket_title);
        content = findViewById(R.id.ticket_content);
        attach = findViewById(R.id.ticket_attach);
        sPrivate = findViewById(R.id.ticket_private);
        sendBut = findViewById(R.id.ticket_send);
        sendBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        String mystring = this.getResources().getString(R.string.select_img).toString();
        startActivityForResult(Intent.createChooser(intent, mystring), RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                //Updates profile picture on screen
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                attach.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Upload to FireStorage
    private void uploadImage() {
        ticket_key = mDatabase.child(UserID).push().getKey();

        //Checks if file was selected
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(R.string.upload_progress);
            progressDialog.show();
            /*
            Image is saved on the folder "tickets" with the name (ticket_key.jpg)
            Dialog is dismissed when the task ends
             */
            StorageReference ref = mStorageRef.child("tickets/" + ticket_key + ".jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ticket_bar.setVisibility(View.VISIBLE);
                            imageURL = taskSnapshot.getDownloadUrl();
                            Toast.makeText(TicketActivity.this, R.string.upload_success,
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            sendTicket();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(TicketActivity.this, R.string.upload_failed +" - "+ e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage(R.string.upload_sent + (int) progress + "%");
                        }
                    });
        } else {
            ticket_bar.setVisibility(View.VISIBLE);
            sendTicket();
        }
    }

    private void sendTicket() {
        String iurl, ititle, icontent, idate, iprivate;
        if (!title.getText().toString().isEmpty() && !content.getText().toString().isEmpty()) {
            if (imageURL != null) {
                iurl = imageURL.toString();
            } else {
                iurl = "empty";
            }
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            idate = sdf.format(currentDate).toString();
            ititle = title.getText().toString();
            icontent = content.getText().toString();
            if (sPrivate.isChecked()) {
                iprivate = "true";
            } else {
                iprivate = "false";
            }
            Tickets ticket = new Tickets(ititle, icontent, iprivate, UserID, disc_key, discipline, idate, iurl);
            mDatabase.child(UserID).push().setValue(ticket);
            ticket_bar.setVisibility(View.INVISIBLE);
            finish();
        }
    }

    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_home:
                sendtoHome();
                break;
            case R.id.nav_class:
                sendtoFav();
                break;
            case R.id.nav_notes:
                sendtoNotes();
                break;
            case R.id.nav_tickets:
                sendtoTickets();
                break;
            case R.id.nav_account:
                sendtoProfile();
                break;
            case R.id.nav_settings:
                sendtoSettings();
                break;
            case R.id.nav_logout:
                sendtoLogin();
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

      /*
        Menu Button Disabled - Can be enabled in the Future
        if (mToggle.onOptionsItemSelected(item)) {
            return false;
        }*/
        return super.onOptionsItemSelected(item);
    }

    private void sendtoHome() {
        Intent intent = new Intent(TicketActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendtoFav() {
        //send to favorites
    }

    private void sendtoNotes() {
        //Send to my notes
    }

    private void sendtoTickets() {
        //Send to my tickets
    }

    private void sendtoProfile() {
        Intent intent = new Intent(TicketActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void sendtoSettings() {
        Intent intent = new Intent(TicketActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void sendtoLogin() {
        mAuth.signOut();
        Intent intent = new Intent(TicketActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }


}
