package gcu.mpd.trafficupdates;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

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
import java.util.Date;

/*////////////////////////////
                           //
Student: Paul James Kerr   //
Matric no: S1828425        //
                           //
/////////////////////////// */

public class ListFragment extends androidx.fragment.app.ListFragment {

    private int responseID = 0;
    private ItemSelected activity;
    private boolean landValue = false;
    private ArrayList<String> titles;
    private ArrayList<RssResponse> incidents;
    private String URL = null;
    private SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MMMM-dd");
    private EditText searchFilter;
    private CustomAdapter adapter;


    public interface ItemSelected
    {
        void onItemSelected(RssResponse incident, int index);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        activity = (ItemSelected) context;
        landValue = this.getArguments().getBoolean("landscape");
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        URL = getArguments().getString("url");
        new ProcessInBackground().execute();
        searchFilter = getActivity().findViewById(R.id.searchFilter);
        searchFilter.setText("");

        searchFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(adapter!=null) {
                    adapter.getFilter().filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    public void selectDefault() {
        if(landValue) {
            activity.onItemSelected(incidents.get(0), 0);
        }
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        ArrayList<RssResponse> newIncidents = (ArrayList<RssResponse>) adapter.getFilteredData();

        if(newIncidents.size() > 0)
        {
            RssResponse incident = newIncidents.get((int)id);
            activity.onItemSelected(incident, position);
        }
        else
        {
            RssResponse defaultIncident = new RssResponse();
            defaultIncident.setDescription("There are no current incidents");
            activity.onItemSelected(defaultIncident, position);
        }
    }


    public InputStream getInputStream(URL url)
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

            titles = new ArrayList<String>();
            incidents = new ArrayList<RssResponse>();

            progressDialog.setMessage("Loading Current Incidents");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<RssResponse> s) {
            super.onPostExecute(s);
            progressDialog.dismiss();

            adapter = new CustomAdapter(getActivity(), incidents);
            setListAdapter(adapter);
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
                            incidentToAdd.setUniqueID(responseID);
                            responseID = responseID + 1;
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
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }

            if(incidents.size() > 0) {
                return incidents;
            }
            else
            {
                RssResponse defaultResponse = new RssResponse();
                defaultResponse.setTitle("No current incidents");
                defaultResponse.setDescription("No incident data");
                incidents.add(defaultResponse);
                return incidents;
            }
        }
    }
}