package com.student.pro.prostudent;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;



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
}