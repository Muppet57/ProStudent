package com.student.pro.prostudent.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.student.pro.prostudent.Adapters.AdapterNote;
import com.student.pro.prostudent.Adapters.AdapterTicket;
import com.student.pro.prostudent.Objects.Notes;
import com.student.pro.prostudent.Objects.Tickets;
import com.student.pro.prostudent.R;

import java.util.ArrayList;

public class ClassActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;

    private DatabaseReference mDB_Disciplines, mDB_Notes, mDB_Tickets;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private String user_id, discipline, disc_key;
    private String TAG = "ticketstest";

    private RecyclerView mRecyclerNotes, mRecyclerTickets;
    private AdapterTicket adapter;
    private Button ticket_add, note_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class);

        //Check for required extras
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
        getSupportActionBar().setTitle(discipline);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Current user
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        user_id = user.getUid();

        //Database Instances
        mDB_Disciplines = FirebaseDatabase.getInstance().getReference("courses/course/0/ucs");
        mDB_Notes = FirebaseDatabase.getInstance().getReference().child("notes");
        mDB_Tickets = FirebaseDatabase.getInstance().getReference().child("tickets");


        //Functions to load Notes and Tickets onto Recycler views
        getKey();
        getNotes();
        getTickets();
        //Elements & Click Listeners
        ticket_add = findViewById(R.id.ticket_add);
        ticket_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClassActivity.this, TicketActivity.class);
                intent.putExtra("ID", disc_key);
                intent.putExtra("Discipline", discipline);
                startActivity(intent);
            }
        });
        note_add = findViewById(R.id.note_add);
        note_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClassActivity.this, NoteActivity.class);
                intent.putExtra("ID", disc_key);
                intent.putExtra("Discipline", discipline);
                startActivity(intent);
            }
        });
    }

    private void getKey() {
        mDB_Disciplines.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                    if (dataSnapshot.getValue().toString().equals(disc_key) &&
                            postsnapshot.child("short").getValue().toString().equals(discipline)) {
                        /*
                           A "security" check is done to confirm the Discipline authenticity against
                           the database with both ID and TAG
                           We could use the inherited ID from the previous page, but since we need to perform
                           this step we will use the ID retrieved from the , because this way we can avoid
                           checking if the result was positive and just keep going.
                         */
                        disc_key = postsnapshot.getKey();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Retrieve the Notes data from the Database
    private void getNotes() {
        mDB_Notes.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Notes> notes = new ArrayList<>();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String title, content, date, id_disc, tag_disc;
                    for (DataSnapshot postpostSnap : postSnapshot.getChildren()) {
                        if (disc_key.equals(postpostSnap.child("id_disc").getValue().toString()) &&
                                discipline.equals(postpostSnap.child("tag_disc").getValue().toString())) {
                            title = postpostSnap.child("title").getValue().toString();
                            content = postpostSnap.child("content").getValue().toString();
                            tag_disc = postpostSnap.child("tag_disc").getValue().toString();
                            id_disc = postpostSnap.child("id_disc").getValue().toString();
                            date = postpostSnap.child("date").getValue().toString();

                            Notes note = new Notes(title, content, tag_disc, id_disc, date);
                            note.setNote_id(postpostSnap.getKey());
                            notes.add(note);
                        }
                    }
                }

                initRecyclerNote(notes);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Initialize Notes Recycler View
    private void initRecyclerNote(ArrayList<Notes> notes) {

        mRecyclerNotes = findViewById(R.id.notes_recyler);
        // Collections.sort(notes,new CustomCompareNotes());
        // Creating our adapter which inherits our Array and context
        AdapterNote adapter = new AdapterNote(notes, this);

        //Display message for an empty recycler
        TextView empty = findViewById(R.id.empty_view);
        if (adapter.getItemCount() == 0) {
            empty.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.GONE);
        }
        //Setting the adapter to the recycler
        mRecyclerNotes.setAdapter(adapter);
        //Setting the layout manager
        mRecyclerNotes.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getTickets() {
        Log.d(TAG, "getTickets:");
        mDB_Tickets.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Tickets> tickets = new ArrayList<>();

                for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                    String ititle, icontent, iuser_id, iprivate, idate, id_disc, tag_disc, iurl, ticket_id;
                    for (DataSnapshot postpostsnap : postsnapshot.getChildren())

                        if (disc_key.equals(postpostsnap.child("id_disc").getValue().toString()) &&
                                discipline.equals(postpostsnap.child("tag_disc").getValue().toString())) {
                            ititle = postpostsnap.child("title").getValue().toString();
                            icontent = postpostsnap.child("content").getValue().toString();
                            iprivate = postpostsnap.child("sprivate").getValue().toString();
                            iuser_id = postpostsnap.child("user_id").getValue().toString();
                            tag_disc = postpostsnap.child("tag_disc").getValue().toString();
                            id_disc = postpostsnap.child("id_disc").getValue().toString();
                            idate = postpostsnap.child("date").getValue().toString();
                            iurl = postpostsnap.child("url").getValue().toString();

                            Tickets ticket = new Tickets(ititle, icontent, iprivate,
                                    iuser_id, id_disc, tag_disc, idate, iurl);
                            ticket.setTicket_id(postsnapshot.getKey());
                            tickets.add(ticket);
                        }
                }
                Log.d(TAG, "onDataChange: GET TICKET DATACHANGE");
                initRecyclerTicket(tickets);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initRecyclerTicket(ArrayList<Tickets> tickets) {
        Log.d(TAG, "initRecyclerTicket:");
        TextView empty = findViewById(R.id.empty_view_tickets);

        mRecyclerTickets = findViewById(R.id.tickets_recycler);

        adapter = new AdapterTicket(tickets, this);

        if (adapter.getItemCount() == 0) {
            empty.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.GONE);
        }
        mRecyclerTickets.setAdapter(adapter);
        mRecyclerTickets.setLayoutManager(new LinearLayoutManager(this));
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
        // Management of navigation view item clicks.
        switch (item.getItemId()) {
            case R.id.nav_account:
                sendtoProfile();
                break;
            case R.id.nav_settings:
                sendtoSettings();
                break;
            case R.id.nav_logout:
                sendtoLogin();
                break;
            case R.id.nav_tickets:
                sendtoTickets();
                break;
            case R.id.nav_home:
                sendtoHome();
                break;

        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // This activity is to be destroyed on exiting
    private void sendtoHome() {
        Intent intent = new Intent(ClassActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
    // This activity is to be destroyed on exiting

    private void sendtoLogin() {
        Intent intent = new Intent(ClassActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    // This activity is to be destroyed on exiting

    private void sendtoSettings() {
        Intent intent = new Intent(ClassActivity.this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }
    // This activity is to be destroyed on exiting

    private void sendtoProfile() {
        Intent intent = new Intent(ClassActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    // This activity isn't destroyed so the user can return to this page and see the newly added ticket
    private void sendtoTickets() {
        Intent intent = new Intent(ClassActivity.this, TicketActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

}