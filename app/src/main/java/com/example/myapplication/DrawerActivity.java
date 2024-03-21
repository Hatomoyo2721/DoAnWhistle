package com.example.myapplication;

import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class DrawerActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
           @Override
           public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
               switch (menuItem.getItemId()) {
                   case R.id.home:
                       Toast.makeText(DrawerActivity.this, "Home", Toast.LENGTH_SHORT).show();
                       break;
                   case R.id.about:
                       Toast.makeText(DrawerActivity.this, "About Selected", Toast.LENGTH_SHORT).show();
                       break;
                   case R.id.list_favourite_songs:
                       Toast.makeText(DrawerActivity.this, "List Favorite Songs Selected", Toast.LENGTH_SHORT).show();
                       break;
                   case R.id.share:
                       Toast.makeText(DrawerActivity.this, "Share Selected", Toast.LENGTH_SHORT).show();
                       break;
                   case R.id.rate_us:
                       Toast.makeText(DrawerActivity.this, "Rate us Selected", Toast.LENGTH_SHORT).show();
                       break;
               }
               return true;
           }
       });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }
}