package com.nuig.trafficappbackend.models;

import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Dylan Toner on 25/01/2016.
 */
@Entity
public class Incident implements Serializable{

    @Id
    private Long incidentId;

    private  String title;
    private String severity;
    private String description;
    private GeoPt location;
    private String category;
    private String reportedBy;
    private int trustScore;
    private Date timestamp;
    private List<String> verifiedBy;
    public Incident()
    {
        this.trustScore = 0;
        this.timestamp = new Date();
        this.verifiedBy = new ArrayList<String>();
    }
    public Long getIncidentId() {
        return incidentId;
    }

    public final void setIncidentId(final Long incidentId) {
        this.incidentId = incidentId;
    }

    public final String getTitle() {
        return title;
    }

    public final void setTitle(final String title) {
        this.title = title;
    }

    public final String getSeverity() {
        return severity;
    }

    public final void setSeverity(final String severity) {
        this.severity = severity;
    }

    public final String getDescription() {
        return description;
    }

    public final void setDescription(final String description) {
        this.description = description;
    }

    public final GeoPt getLocation() {
        return location;
    }

    public final void setLocation(final GeoPt location) {
        this.location = location;
    }

    public String getCategory() {return category; }

    public void setCategory(String category) {this.category = category; }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public int getTrustScore() {return trustScore;}

    public void setTrustScore(int trustScore) {this.trustScore = trustScore;}

    public Date getTimestamp() {return timestamp;}

    public List<String> getVerifiedBy() {
        if(verifiedBy==null)
            verifiedBy = new ArrayList<>();

        return verifiedBy;
    }

    public void verify(String user){
        this.verifiedBy.add(user);
    }
}
