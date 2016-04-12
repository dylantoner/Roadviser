package com.nuig.trafficappbackend.apis;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.users.User;
import com.nuig.trafficappbackend.Constants;
import com.nuig.trafficappbackend.models.UserAccount;
import com.nuig.trafficappbackend.utils.EndpointUtil;
import com.nuig.trafficappbackend.models.Incident;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;
import static com.nuig.trafficappbackend.OfyService.ofy;

/**
 * Exposes REST API over Incident resources.
 */
@Api(
        name = "trafficApp",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "trafficappbackend.nuig.com",
                ownerName = "trafficappbackend.nuig.com",
                packagePath=""
        )
)
@ApiClass(resource  = "incidents",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.API_EXPLORER_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID}
)
public class IncidentEndpoint {

    /**
     * Log output.
     */
    private static final Logger LOG = Logger
            .getLogger(IncidentEndpoint.class.getName());


    /**
     * Lists all the entities inserted in datastore.
     * @param user the user requesting the entities.
     * @return List of all Incident entities persisted.
     */
    @SuppressWarnings({"cast", "unchecked"})
    @ApiMethod(httpMethod = "GET")
    public final List<Incident> listIncidents(final User user) {
        return ofy().load().type(Incident.class).list();
    }

    /**
     * Gets the entity having primary key id.
     * @param id the primary key of the java bean Incident entity.
     * @param user the user requesting the entity.
     * @return The entity with primary key id.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "GET")
    public final Incident getIncident(@Named("id") final Long id, final User user)
            throws ServiceException {
        EndpointUtil.throwIfNotAuthenticated(user);

        return findIncident(id);
    }

    /**
     * Inserts the entity into App Engine datastore. It uses HTTP POST method.
     * @param incident the entity to be inserted.
     * @param user the user inserting the entity.
     * @return The inserted entity.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "POST")
    public final Incident insertIncident(final Incident incident, final User user) throws
            ServiceException {
        EndpointUtil.throwIfNotAuthenticated(user);

        UserAccount usr = ofy().load().type(UserAccount.class)
                .filter("email", user.getEmail()).first().now();
        if(!(incident.getReportedBy().equals("Administrator"))) {
            int rep = usr.getReputation();
            int weight = 0;
            if (rep > 0 && rep <= 10)
                weight = 5;
            else if (rep > 10 && rep <= 20)
                weight = 10;
            else if (rep > 20 && rep <= 30)
                weight = 15;
            else if (rep > 30 && rep <= 40)
                weight = 20;
            else if (rep > 40 && rep <= 50)
                weight = 25;
            else if (rep > 50)
                weight = 30;

            incident.setTrustScore(weight);
        }

        ofy().save().entity(incident).now();
        float latit = incident.getLocation().getLatitude();
        float longit = incident.getLocation().getLongitude();

        if(incident.getReportedBy().equals("Administrator"))
            pushIncident(incident.getTitle(), latit, longit, user,true);
        else
            pushIncident(incident.getTitle(), latit, longit, user,false);
        return incident;
    }

    /**
     * Updates an entity. It uses HTTP PUT method.
     * @param incident the entity to be updated.
     * @param user the user modifying the entity.
     * @return The updated entity.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "PUT")
    public final Incident updateIncident(final Incident incident, final User user) throws
            ServiceException {
        EndpointUtil.throwIfNotAuthenticated(user);
        //UserAccount usr = ofy().load().type(UserAccount.class).filter("email", user.getEmail()).first().now();
        if((!incident.getVerifiedBy().contains(user.getEmail()))) {
            incident.verify(user.getEmail());
            ofy().save().entity(incident).now();
        }
        return incident;
    }

    /**
     * Removes the entity with primary key id. It uses HTTP DELETE method.
     * @param id the primary key of the entity to be deleted.
     * @param user the user deleting the entity.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "DELETE")
    public final void removeIncident(@Named("id") final Long id, final User user)
            throws ServiceException {
        EndpointUtil.throwIfNotAuthenticated(user);

        Incident incident = findIncident(id);
        if (incident == null) {
            LOG.info(
                    "Incident " + id + " not found, skipping deletion.");
            return;
        }
        pushRemoveIncident(user);
        ofy().delete().entity(incident).now();
    }

    /**
     * Searches an entity by ID.
     * @param id the incident ID to search
     * @return the Incident associated to id
     */
    private Incident findIncident(final Long id) {
        return ofy().load().type(Incident.class).id(id).now();
    }

    private void pushIncident(final String title,final float latit,final float longit, final User user,final boolean isAdmin) {
        // insert a task to a queue
        try {
            // Prepare JSON containing the GCM message content. What to send and where to send.
            JSONObject jGcmData = new JSONObject();
            JSONObject jData = new JSONObject();
            jData.put("message", title);
            if(isAdmin)
                jData.put("email", "Administrator");
            else
                jData.put("email", user.getEmail());
            jData.put("lat", latit);
            jData.put("long", longit);
            // Where to send GCM message.

                jGcmData.put("to", "/topics/global");

            // What to send in GCM message.
            jGcmData.put("data", jData);

            // Create connection to send GCM Message request.
            URL url = new URL("https://android.googleapis.com/gcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "key=" + Constants.GCM_API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Send GCM message content.
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jGcmData.toString().getBytes());

            // Read GCM response.
            InputStream inputStream = conn.getInputStream();
            String resp = IOUtils.toString(inputStream);
            System.out.println(resp);
            System.out.println("Check your device/emulator for notification or logcat for " +
                    "confirmation of the receipt of the GCM message.");
        } catch (IOException e) {
            System.out.println("Unable to send GCM message.");
            System.out.println("Please ensure that API_KEY has been replaced by the server " +
                    "API key, and that the device's registration token is correct (if specified).");
            e.printStackTrace();
        }
    }
    private void pushRemoveIncident(final User user) {
        // insert a task to a queue
        try {
            // Prepare JSON containing the GCM message content. What to send and where to send.
            JSONObject jGcmData = new JSONObject();
            JSONObject jData = new JSONObject();
            jData.put("message", "delete");
            jData.put("email", user.getEmail());
            // Where to send GCM message.

            jGcmData.put("to", "/topics/global");

            // What to send in GCM message.
            jGcmData.put("data", jData);

            // Create connection to send GCM Message request.
            URL url = new URL("https://android.googleapis.com/gcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "key=" + Constants.GCM_API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Send GCM message content.
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jGcmData.toString().getBytes());

            // Read GCM response.
            InputStream inputStream = conn.getInputStream();
            String resp = IOUtils.toString(inputStream);
            System.out.println(resp);
            System.out.println("Check your device/emulator for notification or logcat for " +
                    "confirmation of the receipt of the GCM message.");
        } catch (IOException e) {
            System.out.println("Unable to send GCM message.");
            System.out.println("Please ensure that API_KEY has been replaced by the server " +
                    "API key, and that the device's registration token is correct (if specified).");
            e.printStackTrace();
        }
    }
}
