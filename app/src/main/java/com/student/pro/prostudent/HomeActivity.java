package com.student.pro.prostudent;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String user_id,TAG="dashboard";
    private RecyclerView mView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setNavigationViewListener();

        mToolbar = findViewById(R.id.nav_action);
        setSupportActionBar(mToolbar);
        mDrawerLayout = findViewById(R.id.drawerlayout);
        mToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setTitle(R.string.title_dashboard);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        user_id=user.getUid();


        mDatabase= FirebaseDatabase.getInstance().getReference("courses/course/0/ucs");
        Log.d(TAG, "onCreate: "+mDatabase.toString());
        getClasses();
    }

    private void getClasses()
    {

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int cnt1=0,cnt2=0;
                ArrayList<Disciplines> ucs = new ArrayList<Disciplines>();

                for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    String name,year,tag;

                    name=postSnapshot.child("name").getValue().toString();
                    year= postSnapshot.child("year").getValue().toString();
                    tag=postSnapshot.child("short").getValue().toString();

                    if(postSnapshot.child("year").getValue().toString().equals("1"))
                    {
                        cnt1++;

                    }
                    if(postSnapshot.child("year").getValue().toString().equals("2"))
                    {
                        cnt2++;
                    }
                    Disciplines uc = new Disciplines(name,year,tag);
                    ucs.add(uc);
                }

                initrecycle(ucs,cnt1,cnt2);
                Log.d(TAG, "onDataChange: "+cnt1+" " + cnt2);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void initrecycle(ArrayList<Disciplines> ucs,int cnt1,int cnt2)
    {
        Collections.sort(ucs, new CustomCompare());

        mView = findViewById(R.id.recycler_class);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(ucs,this);

        //This is the code to provide a sectioned list
        List<SectionedAdapter.Section> sections =
                new ArrayList<SectionedAdapter.Section>();

        //Sections
        sections.add(new SectionedAdapter.Section(0,"1st Year"));
        sections.add(new SectionedAdapter.Section(cnt1,"2nd Year"));
        sections.add(new SectionedAdapter.Section(cnt2+cnt1,"3rd Year"));
        SectionedAdapter.Section[] dummy = new SectionedAdapter.Section[sections.size()];
        SectionedAdapter mSectionedAdapter = new
                SectionedAdapter(this,R.layout.header_section,R.id.section_header,adapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));

        mView.setAdapter(mSectionedAdapter);
        mView.setLayoutManager(new LinearLayoutManager(this));


    }

    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu,menu);
        return true;
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId())
        {
            case R.id.nav_account:
                Log.d("Beag", String.valueOf(item.getItemId()));

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
            case R.id.nav_class:
                sendtoClass();
                break;

        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(mToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendtoLogin() {
        Intent intent = new Intent(HomeActivity.this,LoginActivity.class);
        startActivity(intent);
    }

    private void sendtoSettings() {
        Intent intent = new Intent(HomeActivity.this,SettingsActivity.class);
        startActivity(intent);
    }

    private void sendtoProfile() {
        Intent intent = new Intent(HomeActivity.this,ProfileActivity.class);
        startActivity(intent);
    }
    private void sendtoTickets(){
        Intent intent = new Intent(HomeActivity.this,TicketActivity.class);
        startActivity(intent);
    }
    private void sendtoClass(){
        Intent intent = new Intent(HomeActivity.this,ClassActivity.class);
        startActivity(intent);
    }
}
