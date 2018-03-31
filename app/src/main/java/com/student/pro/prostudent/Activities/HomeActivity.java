package com.student.pro.prostudent.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
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
import com.student.pro.prostudent.Adapters.AdapterProfessor;
import com.student.pro.prostudent.Adapters.AdapterStudent;
import com.student.pro.prostudent.Comparators.CustomCompareDiscipline;
import com.student.pro.prostudent.Objects.Course_Class;
import com.student.pro.prostudent.Objects.Disciplines;
import com.student.pro.prostudent.R;
import com.student.pro.prostudent.Adapters.SectionedAdapter;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    private DatabaseReference mDatabase, mDB_User, mDB_Professor, mDB_Student;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String UserID, TAG = "Homelog";
    private RecyclerView mView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setNavigationViewListener();

        mToolbar = findViewById(R.id.nav_action);
        setSupportActionBar(mToolbar);
        mDrawerLayout = findViewById(R.id.drawerlayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        //getSupportActionBar().setTitle(R.string.title_home);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        UserID = user.getUid();
        mDB_User = FirebaseDatabase.getInstance().getReference("users").child(UserID).child("status");
        mDB_Professor = FirebaseDatabase.getInstance().getReference("professors_ucs/" + UserID + "/course");
        mDB_Student = FirebaseDatabase.getInstance().getReference("students_courses");
        getStatus();
    }

    private void getStatus() {
        mDB_User.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean flag = false;
                if (dataSnapshot.getValue().toString().equals("student")) {
                    getCourseStudent();
                    flag = true;
                } else {
                    getCoursesProfessor();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getCourseStudent() {

        mDB_Student.child(UserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getClassesStudent(dataSnapshot.child("course_id").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getClassesStudent(String course) {
        mDatabase = FirebaseDatabase.getInstance().getReference("courses/course/" + course + "/ucs");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int cnt1 = 0, cnt2 = 0;
                ArrayList<Disciplines> ucs = new ArrayList<Disciplines>();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String name, year, tag, id;

                    name = postSnapshot.child("name").getValue().toString();
                    year = postSnapshot.child("year").getValue().toString();
                    tag = postSnapshot.child("short").getValue().toString();
                    id = postSnapshot.getKey().toString();
                    if (postSnapshot.child("year").getValue().toString().equals("1")) {
                        cnt1++;

                    }
                    if (postSnapshot.child("year").getValue().toString().equals("2")) {
                        cnt2++;
                    }
                    Disciplines uc = new Disciplines(name, year, tag, id);
                    ucs.add(uc);
                }

                initRecycleStudent(ucs, cnt1, cnt2);
                mDatabase.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getCoursesProfessor() {
        mDB_Professor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Course_Class> courses = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    for (DataSnapshot postpostSnap : postSnapshot.child("ucs").getChildren()) {
                        Course_Class course = new Course_Class();
                        course.setCourse_id(postSnapshot.getKey());
                        course.setClass_id(postpostSnap.child("id_disc").getValue().toString());
                        courses.add(course);
                    }
                }
                getClassesProfessor(courses);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void getClassesProfessor(final ArrayList<Course_Class> courses) {

        mDatabase = FirebaseDatabase.getInstance().getReference("courses/course/");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Disciplines> ucs = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    if (containsId(courses, postSnapshot.getKey())) {

                        for (int i = 0; i < courses.size(); i++) {
                            for (DataSnapshot postpostSnap : postSnapshot.child("ucs").getChildren()) {
                               if (courses.get(i).getClass_id().equals(postpostSnap.getKey()) && courses.get(i).getCourse_id().toString().equals(postSnapshot.getKey())) {
                                  String name, year, tag, id;
                                    name = postpostSnap.child("name").getValue().toString();
                                    year = postpostSnap.child("year").getValue().toString();
                                    tag = postpostSnap.child("short").getValue().toString();
                                    id = postpostSnap.getKey().toString();
                                    Disciplines uc = new Disciplines(name, year, tag, id);
                                    ucs.add(uc);
                                    break;
                                }
                            }
                        }
                    }
                }
                initRecycleProfessor(ucs);
                mDatabase.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static boolean containsId(ArrayList<Course_Class> courses, String id) {
        for (Course_Class object : courses) {
            if (object.getCourse_id().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void initRecycleStudent(ArrayList<Disciplines> ucs, int cnt1, int cnt2) {
        Collections.sort(ucs, new CustomCompareDiscipline());

        mView = findViewById(R.id.recycler_class);
        AdapterStudent adapter = new AdapterStudent(ucs, this);

        //Criar
        List<SectionedAdapter.Section> sections =
                new ArrayList<>();

        sections.add(new SectionedAdapter.Section(0, "1st Year"));
        sections.add(new SectionedAdapter.Section(cnt1, "2nd Year"));
        sections.add(new SectionedAdapter.Section(cnt2 + cnt1, "3rd Year"));
        SectionedAdapter.Section[] dummy = new SectionedAdapter.Section[sections.size()];
        SectionedAdapter mSectionedAdapter = new
                SectionedAdapter(this, R.layout.header_section, R.id.section_header, adapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));

        mView.setAdapter(mSectionedAdapter);
        mView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initRecycleProfessor(ArrayList<Disciplines> ucs) {
        Collections.sort(ucs, new CustomCompareDiscipline());

        mView = findViewById(R.id.recycler_class);
        AdapterProfessor adapter = new AdapterProfessor(ucs, this);

        List<SectionedAdapter.Section> sections =
                new ArrayList<>();

        sections.add(new SectionedAdapter.Section(0, "My Disciplines"));
        SectionedAdapter.Section[] dummy = new SectionedAdapter.Section[sections.size()];
        SectionedAdapter mSectionedAdapter = new
                SectionedAdapter(this, R.layout.header_section, R.id.section_header, adapter);
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

        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendtoHome() {
        Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
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
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void sendtoSettings() {
        Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void sendtoLogin() {
        mAuth.signOut();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Do you want to log out?");
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.action_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                sendtoLogin();
            }
        });
        builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}
