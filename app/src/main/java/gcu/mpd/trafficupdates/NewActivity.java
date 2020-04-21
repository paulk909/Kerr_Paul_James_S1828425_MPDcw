package gcu.mpd.trafficupdates;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/*////////////////////////////
                           //
Student: Paul James Kerr   //
Matric no: S1828425        //
                           //
/////////////////////////// */

public class NewActivity extends AppCompatActivity {

    private String incidentURL = "https://trafficscotland.org/rss//feeds/currentincidents.aspx";
    private String roadworkURL = "https://trafficscotland.org/rss//feeds/roadworks.aspx";
    private String plannedURL = "https://trafficscotland.org/rss//feeds/plannedroadworks.aspx";
    private boolean isItLandscape = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
            Fragment selectedFragment = new MapFragment();

            Bundle args = new Bundle();
            if(findViewById(R.id.layout_land) != null)
            {
                isItLandscape = true;
            }
            args.putBoolean("landscape", isItLandscape);
            args.putString("url", incidentURL);
            selectedFragment.setArguments(args);

            tr.replace(R.id.map_container, selectedFragment, "currentFrag");
            tr.addToBackStack(null);
            tr.commit();
        }
    }



    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = new MapFragment();
                    Bundle args = new Bundle();
                    if(findViewById(R.id.layout_land) != null)
                    {
                        isItLandscape = true;
                    }
                    args.putBoolean("landscape", isItLandscape);


                    switch(menuItem.getItemId()) {
                        case R.id.nav_incidents:
                            args.putString("url", incidentURL);
                            selectedFragment.setArguments(args);
                            break;
                        case R.id.nav_roadworks:
                            args.putString("url", roadworkURL);
                            selectedFragment.setArguments(args);
                            break;
                        case R.id.nav_planned:
                            args.putString("url", plannedURL);
                            selectedFragment.setArguments(args);
                            break;
                    }

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.map_container, selectedFragment, "currentFrag")
                            .commit();



                    getSupportFragmentManager().executePendingTransactions();


                    return true;
                }
            };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.options_details)
        {
            Intent myIntent = new Intent(NewActivity.this, MainActivity.class);
            startActivity(myIntent);
            return false;
        }
        return super.onOptionsItemSelected(item);
    }


}
