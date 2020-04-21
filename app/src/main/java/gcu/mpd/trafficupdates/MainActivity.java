package gcu.mpd.trafficupdates;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;


import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/*////////////////////////////
                           //
Student: Paul James Kerr   //
Matric no: S1828425        //
                           //
/////////////////////////// */

public class MainActivity extends AppCompatActivity implements ListFragment.ItemSelected

{
    private TextView txtDetails;
    private String currentFrag = "incident";
    private ScrollView detailsScrollView;
    private boolean isItLandscape = false;
    private String incidentURL = "https://trafficscotland.org/rss//feeds/currentincidents.aspx";
    private String roadworkURL = "https://trafficscotland.org/rss//feeds/roadworks.aspx";
    private String plannedURL = "https://trafficscotland.org/rss//feeds/plannedroadworks.aspx";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtDetails = findViewById(R.id.txtDetails);
        detailsScrollView = findViewById(R.id.detailsScrollView);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
            Fragment selectedFragment = new ListFragment();

            Bundle args = new Bundle();
            if(findViewById(R.id.layout_land) != null)
            {
                isItLandscape = true;
            }
            args.putBoolean("landscape", isItLandscape);
            args.putString("url", incidentURL);
            selectedFragment.setArguments(args);

                    tr.replace(R.id.fragment_container, selectedFragment, "currentFrag");
                    tr.addToBackStack(null);
                    tr.commit();
        }

        getSupportFragmentManager().executePendingTransactions();

        //The phone is in portrait mode
        if(findViewById(R.id.layout_portrait) != null)
        {
            FragmentManager fm = this.getSupportFragmentManager();
            Fragment f = fm.findFragmentByTag("currentFrag");

            if(f!=null) {
                fm.beginTransaction()
                        .hide(fm.findFragmentById(R.id.details_container))
                        .show(fm.findFragmentById(R.id.fragment_container))
                        .addToBackStack(null)
                        .commit();
            }
        }

        //The phone is in landscape mode
        if(findViewById(R.id.layout_land) != null)
        {
            FragmentManager fm = this.getSupportFragmentManager();
            Fragment f = fm.findFragmentByTag("currentFrag");

            if(f!=null) {
                fm.beginTransaction()
                        .show(fm.findFragmentById(R.id.details_container))
                        .show(fm.findFragmentById(R.id.fragment_container))
                        .addToBackStack(null)
                        .commit();
            }
        }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = new ListFragment();
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
                            currentFrag = "incident";
                            break;
                        case R.id.nav_roadworks:
                            args.putString("url", roadworkURL);
                            selectedFragment.setArguments(args);
                            currentFrag = "roadwork";
                            break;
                        case R.id.nav_planned:
                            args.putString("url", plannedURL);
                            selectedFragment.setArguments(args);
                            currentFrag = "planned";
                            break;
                    }

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment, "currentFrag")
                            .commit();
                    Log.e("Current fragment", currentFrag);



                    getSupportFragmentManager().executePendingTransactions();

                    //The phone is in portrait mode
                    if(findViewById(R.id.layout_portrait) != null)
                    {
                        FragmentManager fm = getSupportFragmentManager();
                        Fragment f = fm.findFragmentByTag("currentFrag");

                        if(f!=null) {
                            fm.beginTransaction()
                                    .hide(fm.findFragmentById(R.id.details_container))
                                    .show(fm.findFragmentById(R.id.fragment_container))
                                    .commit();
                        }
                    }

                    //The phone is in landscape mode
                    if(findViewById(R.id.layout_land) != null)
                    {
                        FragmentManager fm = getSupportFragmentManager();
                        Fragment f = fm.findFragmentByTag("currentFrag");

                        if(f!=null) {
                            fm.beginTransaction()
                                    .show(fm.findFragmentById(R.id.details_container))
                                    .show(fm.findFragmentById(R.id.fragment_container))
                                    .commit();
                        }
                    }

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

        if(id == R.id.options_map)
        {
            Intent myIntent = new Intent(MainActivity.this, NewActivity.class);
            myIntent.putExtra("url", "https://trafficscotland.org/rss//feeds/currentincidents.aspx");
            startActivity(myIntent);

            return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(isItLandscape == true){
            isItLandscape = false;
        } else {
            isItLandscape = true;
        }
    }


    @Override
    public void onItemSelected(RssResponse response, int index) {

        SimpleDateFormat fmt = new SimpleDateFormat("dd-MMMM-yyyy");

        if(response.getIsDefault() == false) {
            StringBuilder output = new StringBuilder();

            output.append("<b>Title: </b>" + "<br /><br />");
            String str = response.getTitle();
            output.append(str+ "<br /><br />");

            output.append("<b>Details: </b>" + "<br /><br />");

            if(response.getStartDate() != null && response.getEndDate() != null) {
                String startDate = fmt.format(response.getStartDate());
                String endDate = fmt.format(response.getEndDate());
                output.append("Start date: " + startDate + "<br /><br />");
                output.append("End date: " + endDate + "<br /><br />");
            }

            str = response.getDescription();
            str = str.replaceAll("<br />", "<br /><br />"); //"\r\n\n");
            output.append(str+ "<br /><br />");

            output.append("<b>Coordinates: </b>" + "<br /><br />");
            str = (response.getCoords()).toString();
            output.append(str+ "<br /><br />");

            output.append("<b>Link: </b>" + "<br /><br />");
            str = response.getLink();
            output.append(str+ "<br /><br />");

            output.append("<b>Published: </b>" + "<br /><br />");
            str = response.getPublished();
            output.append(str);



            txtDetails.setText(Html.fromHtml(output.toString()));
        }
        else {
            txtDetails.setText(response.getDescription());
        }

        if(findViewById(R.id.layout_portrait) != null)
        {
            FragmentManager fm = this.getSupportFragmentManager();
            Fragment f = fm.findFragmentByTag("currentFrag");

            if(f!=null) {
                fm.beginTransaction()
                        .show(fm.findFragmentById(R.id.details_container))
                        .hide(fm.findFragmentById(R.id.fragment_container))
                        .addToBackStack(null)
                        .commit();
            }
        }

        detailsScrollView.smoothScrollTo(0, txtDetails.getTop());

    }

} // End of MainActivity