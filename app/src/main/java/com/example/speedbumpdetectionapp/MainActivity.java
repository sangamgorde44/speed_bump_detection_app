package com.example.speedbumpdetectionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    BottomNavigationView bottomNavigationView;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    Fragment_A fragment_a = new Fragment_A();
    Fragment_BumpList fragment_bumpList = new Fragment_BumpList();
    Fragment_addSpeedBump fragment_addSpeedBump = new Fragment_addSpeedBump();
    //----------------------------------------------------------------------------------------------
    @SuppressLint("UseSupportActionBar")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        // Set the overflow icon color to white
        Drawable overflowIcon = toolbar.getOverflowIcon();
        if (overflowIcon != null) {
            overflowIcon.setTint(Color.WHITE);
            toolbar.setOverflowIcon(overflowIcon);
        }

        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Home");
            getSupportActionBar().setHomeAsUpIndicator(getDrawable(R.drawable.baseline_arrow_back_24));
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();
                if(itemId == R.id.type_1){
                    loadFragment(fragment_a, true);
                }
                else if(itemId == R.id.type_2){
                   loadFragment(fragment_addSpeedBump, false);
                }
                else if(itemId == R.id.type_3){
                    loadFragment(fragment_bumpList, false);
                }
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.type_1);
    }

   //------------------------------------------- ToolBar -------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.toolbar_menu_light, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if(itemId == R.id.profile){
            Toast.makeText(this,"Profile", Toast.LENGTH_LONG).show();
        }
        else if(itemId == R.id.signOut){
            Toast.makeText(this,"Sign Out Successfully !! ", Toast.LENGTH_LONG).show();
            auth.signOut();
            Intent gotoLogin = new Intent(MainActivity.this , LoginPage.class);
            startActivity(gotoLogin);
            finish();
        }
        else{
            //if(itemId == android.R.id.home)
            Toast.makeText(this,"Back Button", Toast.LENGTH_LONG).show();
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    //---------------------------------------Bottom App Bar ----------------------------------------
    public void loadFragment(Fragment frag, boolean flag){

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        // Hide all fragments
        if (fm.getFragments() != null) {
            for (Fragment fragment : fm.getFragments()) {
                if (fragment != null && fragment.isVisible()) {
                    ft.hide(fragment);
                }
            }
        }
        // Show the required fragment
        String tag = frag.getClass().getSimpleName();
        Fragment existingFragment = fm.findFragmentByTag(tag);
        if (existingFragment != null) {
            ft.show(existingFragment);
        } else {
            ft.add(R.id.container, frag, tag);
        }
        ft.commit();

                                                                                                                //    FragmentManager fm = getSupportFragmentManager();
                                                                                                                //    FragmentTransaction ft = fm.beginTransaction();
                                                                                                                //   if(flag){
                                                                                                                //       ft.replace(R.id.container, frag);
                                                                                                                //   }
                                                                                                                //   else{
                                                                                                                //       ft.replace(R.id.container, frag);
                                                                                                                //   }
                                                                                                                //    ft.commit();
    }
}

//---------------------------------------------- end -----------------------------------------------