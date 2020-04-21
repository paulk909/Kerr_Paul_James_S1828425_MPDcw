package gcu.mpd.trafficupdates;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/*////////////////////////////
                           //
Student: Paul James Kerr   //
Matric no: S1828425        //
                           //
/////////////////////////// */

public class RssResponse {

    private int uniqueID;
    private String title;
    private Date startDate;
    private Date endDate;
    private String description;
    private String link;
    private LatLng coords;
    private String published;
    private boolean isDefault = true;

    public RssResponse() {
        this.title = "default";
        this.description = "default";
        this.link = "default";
        //this.coords = "default";
        this.published = "default";
    }

    public RssResponse(String title, String description, String link, String coords, String published) {
        this.title = title;
        this.description = description;
        this.link = link;
        //this.coords = coords;
        this.published = published;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(int uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public LatLng getCoords() {
        return coords;
    }

    public void setCoords(LatLng coords) {
        this.coords = coords;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }


}
