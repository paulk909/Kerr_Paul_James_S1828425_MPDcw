package gcu.mpd.trafficupdates;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/*////////////////////////////
                           //
Student: Paul James Kerr   //
Matric no: S1828425        //
                           //
/////////////////////////// */

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback
{

    private String URL = null;
    private GoogleMap mMap;
    private ArrayList<RssResponse> incidents;
    private static final LatLng SCOTLAND = new LatLng(56.816918399, -4.1826492694);
    private EditText searchFilter;
    private List<Marker> markers = new ArrayList<Marker>();
    private ImageView displayDate;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Date datePicked = null;
    private String stringTyped = "";
    private SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MMMM-dd");
    private SimpleDateFormat fmt2 = new SimpleDateFormat("dd/MMMM/yyyy");
    private ImageView clearAll;
    private TextView dateOutput;
    private CameraPosition cameraPosition= new CameraPosition.Builder()
            .target(SCOTLAND)
            .zoom(6)
            .bearing(0)
            .tilt(30)
            .build();

   @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        URL = getArguments().getString("url");

        if (getActivity() != null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.map_container);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        }
        new ProcessInBackground().execute();

        dateOutput = getActivity().findViewById(R.id.dateOutput);
        if(datePicked != null)
        {
            dateOutput.setText(fmt2.format(datePicked));
        }
        else
        {
            dateOutput.setText("Date: ");
        }
        searchFilter = getActivity().findViewById(R.id.searchFilter);
        searchFilter.setText("");
        searchFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                stringTyped = searchFilter.getText().toString().toLowerCase(Locale.getDefault());
                Date dateCurrent = datePicked;
                findAndRedraw(stringTyped, dateCurrent);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        clearAll = (ImageView) getActivity().findViewById(R.id.clearAll);

        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFilter.getText().clear();
                stringTyped = "";
                datePicked = null;
                dateOutput.setText("Date: ");
                findAndRedraw(stringTyped, datePicked);
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        displayDate = (ImageView) getActivity().findViewById(R.id.datePicker);

        displayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        getActivity(),
                        android.R.style.Theme_Material_Dialog,
                        dateSetListener,
                        year, month, day);
                dialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                datePicked = new GregorianCalendar(year, month, day).getTime();
                String stringCurrent = stringTyped;
                findAndRedraw(stringCurrent, datePicked);
                if(datePicked != null)
                {
                    dateOutput.setText(fmt2.format(datePicked));
                }
                else
                {
                    dateOutput.setText("Date: ");
                }
            }
        };

    }




    public void findAndRedraw(String chars, Date date)
    {
        ArrayList<RssResponse> listToReturn = new ArrayList<RssResponse>();

        if(incidents.size() > 0 && incidents.get(0).getCoords() != null) {
            if ((chars == null || chars.isEmpty()) && date == null) {
                for (int i = 0; i < incidents.size(); i++) {
                    listToReturn.add(incidents.get(i));
                }
                redrawMarkers(listToReturn);
                ;
            } else if ((chars != null || !chars.isEmpty()) && date == null) {
                for (int i = 0; i < incidents.size(); i++) {
                    String title = incidents.get(i).getTitle();
                    if (title.toLowerCase().contains(chars.toLowerCase())) {
                        listToReturn.add(incidents.get(i));
                    }
                }
                redrawMarkers(listToReturn);
            } else if ((chars == null || chars.isEmpty()) && date != null) {
                for (int i = 0; i < incidents.size(); i++) {
                    Date start = incidents.get(i).getStartDate();
                    Date end = incidents.get(i).getEndDate();
                    if(start != null) {
                        if (!(date.before(start)) && !(date.after(end))) {
                            listToReturn.add(incidents.get(i));
                        }
                    }
                    else
                    {
                        listToReturn.add(incidents.get(i));
                    }
                }
                redrawMarkers(listToReturn);
            } else if ((chars != null || !chars.isEmpty()) && date != null) {
                for (int i = 0; i < incidents.size(); i++) {
                    String title = incidents.get(i).getTitle();
                    Date start = incidents.get(i).getStartDate();
                    Date end = incidents.get(i).getEndDate();
                    if(start != null) {
                        if ((title.toLowerCase().contains(chars.toLowerCase())) && (!(date.before(start)) && !(date.after(end)))) {
                            listToReturn.add(incidents.get(i));
                        }
                    }
                    else
                    {
                        listToReturn.add(incidents.get(i));
                    }
                }
                redrawMarkers(listToReturn);
            }
        }
    }

    public void redrawMarkers(ArrayList<RssResponse> responses) {
        mMap.clear();
        for(int i = 0; i < responses.size(); i++)
            {
                StringBuilder snip = new StringBuilder();
                if (responses.get(i).getStartDate() != null)
                {
                    snip.append("Start date: " +
                            fmt2.format(responses.get(i).getStartDate()) +
                            "\nEnd Date: " +
                            fmt2.format(responses.get(i).getEndDate()) +
                            "\n");

                }
                snip = snip.append("Details: " +
                        responses.get(i).getDescription());
                mMap.addMarker(new MarkerOptions()
                        .position(responses.get(i).getCoords())
                        .title(responses.get(i).getTitle())
                        .snippet(String.valueOf(snip)));
            }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        LayoutInflater layoutInflater = LayoutInflater.from(MapFragment.this.getContext());
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(layoutInflater));

    }


    public InputStream getInputStream(java.net.URL url)
    {
        try
        {
            return url.openConnection().getInputStream();
        }
        catch (IOException e)
        {
            return null;
        }
    }



    public class ProcessInBackground extends AsyncTask<Void, Void, ArrayList<RssResponse>>
    {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

           incidents = new ArrayList<RssResponse>();

            progressDialog.setMessage("Loading Current Incidents");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<RssResponse> s) {
            super.onPostExecute(s);

            progressDialog.dismiss();


            if (incidents.get(0).getCoords() != null) {
                markers = new ArrayList<Marker>();
                for (int i = 0; i < incidents.size(); i++) {
                    StringBuilder snip = new StringBuilder();
                    if (incidents.get(i).getStartDate() != null)
                    {
                        snip.append("Start date: " +
                                fmt2.format(incidents.get(i).getStartDate()) +
                                "\nEnd Date: " +
                                fmt2.format(incidents.get(i).getEndDate()) +
                                "\n");

                    }
                    snip = snip.append("Details: " +
                            incidents.get(i).getDescription());
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(incidents.get(i).getCoords())
                            .title(incidents.get(i).getTitle())
                            .snippet(String.valueOf(snip)));
                    markers.add(marker);
                }
            }
        }

        @Override
        protected ArrayList<RssResponse> doInBackground(Void... params) {

            try
            {
                URL url = new URL(URL);

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(getInputStream(url), "UTF_8");
                boolean insideItem = false;
                RssResponse incidentToAdd = null;

                int eventType = xpp.getEventType();

                while(eventType != XmlPullParser.END_DOCUMENT)
                {
                    if(eventType == XmlPullParser.START_TAG)
                    {
                        if(xpp.getName().equalsIgnoreCase("item"))
                        {
                            insideItem = true;
                            incidentToAdd = new RssResponse();
                            incidentToAdd.setIsDefault(false);
                        }
                        else if(xpp.getName().equalsIgnoreCase("title"))
                        {
                            if(insideItem)
                            {
                                incidentToAdd.setTitle(xpp.nextText());
                            }
                        }else if(xpp.getName().equalsIgnoreCase("description"))
                        {
                            if(insideItem)
                            {
                                String details = xpp.nextText();
                                String[] detailsArray = details.split("<br />+");
                                String detailsRest = "";

                                if(detailsArray.length > 1) {
                                    String[] startArray = detailsArray[0].split("\\s+");
                                    String[] endArray = detailsArray[1].split("\\s+");
                                    Date start = fmt.parse(startArray[5] + "-" + startArray[4] + "-" + startArray[3]);
                                    Date end = fmt.parse(endArray[5] + "-" + endArray[4] + "-" + endArray[3]);
                                    incidentToAdd.setStartDate(start);
                                    incidentToAdd.setEndDate(end);
                                }
                                else {
                                    detailsRest = detailsArray[0];
                                }

                                if(detailsArray.length > 2) {
                                    detailsRest = detailsArray[2];
                                    String[] detailsRestSplit = detailsRest.split("\n+");
                                    if(detailsRestSplit.length > 1)
                                    {
                                        detailsRest = "";
                                        for(int i = 0; i < detailsRestSplit.length - 1; i+=2)
                                        {
                                            detailsRest = detailsRest + detailsRestSplit[i] + " " + detailsRestSplit[i+1] + "<br/><br/>";
                                        }
                                        detailsRest = detailsRest.substring(0, detailsRest.length() - 10);
                                    }
                                }

                                incidentToAdd.setDescription(detailsRest);
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("link"))
                        {
                            if(insideItem) {
                                incidentToAdd.setLink(xpp.nextText());
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("georss:point"))
                        {

                            if(insideItem) {
                                String coords = xpp.nextText();
                                String[] coordArray = coords.split("\\s+");
                                LatLng position = new LatLng(Double.valueOf(coordArray[0]), Double.parseDouble(coordArray[1]));

                                incidentToAdd.setCoords(position);
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("pubDate"))
                        {
                            if(insideItem) {
                                incidentToAdd.setPublished(xpp.nextText());
                            }
                        }
                    }
                    else if(eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item"))
                    {
                        insideItem = false;
                        incidents.add(incidentToAdd);
                    }

                    eventType = xpp.next();
                }
            }
            catch(MalformedURLException e)
            {
                exception = e;
            }
            catch (XmlPullParserException e)
            {
                exception = e;
            }
            catch (IOException e)
            {
                exception = e;
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(incidents.size() > 0) {
                return incidents;
            }
            else
            {
                RssResponse defaultResponse = new RssResponse();
                defaultResponse.setTitle("No incident data");
                defaultResponse.setDescription("No current incidents");
                incidents.add(defaultResponse);
                return incidents;
            }
        }
    }

}