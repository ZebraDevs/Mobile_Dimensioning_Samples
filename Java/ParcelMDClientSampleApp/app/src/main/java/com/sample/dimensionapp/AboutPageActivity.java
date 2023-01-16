package com.sample.dimensionapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

/**
 * Class is used to display information such as SDK version, framework version, app version
 * regulatory approval status.
 */
public class AboutPageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        TextView mTextViewVersion = findViewById(R.id.versionTextView);
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        NavigationView mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        String mVersions = getResources().getString(R.string.app_version) + " " + BuildConfig.VERSION_NAME + "\n"
                + getResources().getString(R.string.bundle_version) + " " + DimensioningClientApp.mBundleVersion + "\n"
                + getResources().getString(R.string.framework_version) + " " + DimensioningClientApp.mFrameworkVersion + "\n"
                + getResources().getString(R.string.service_version) + " " + DimensioningClientApp.mServiceVersion + "\n"
                + "\n\n" + getResources().getString(R.string.copyrights);
        mTextViewVersion.setText(mVersions);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_account:
                startActivity(new Intent(this, DimensioningClientApp.class));
                finishAfterTransition();
                return true;
            case R.id.nav_settings:
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            default:
                return false;
        }
    }

    // override the onOptionsItemSelected() function to implement the item click listener callback
    // to open and close the navigation drawer when the icon is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            Log.d("About", "onOptionsItemSelected= " + item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
            startActivity(new Intent(this, DimensioningClientApp.class));
            finishAfterTransition();
        }
    }
}
