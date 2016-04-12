package com.nuig.trafficappbackend.apis;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableMap;
import com.nuig.trafficappbackend.Constants;
import com.nuig.trafficappbackend.models.Registration;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static com.nuig.trafficappbackend.OfyService.ofy;

/**
 * <p>An endpoint to send messages to devices registered with the backend.</p>
 *
 * <p>For more information, see
 * https://developers.google.com/appengine/docs/java/endpoints/
 * </p>
 *
 * <p>NOTE: This endpoint does not use any form of authorization or
 * authentication! If this app is deployed, anyone can access this endpoint! If
 * you'd like to add authentication, take a look at the documentation.</p>
 */
@Api(name = "trafficApp", version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = Constants.API_OWNER,
                ownerName = Constants.API_OWNER,
                packagePath = Constants.API_PACKAGE_PATH
        )
)
@ApiClass(resource = "messaging",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.API_EXPLORER_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID}
)
public class MessagingEndpoint {

    /**
     * Log output.
     */
    private static final Logger LOG = Logger
            .getLogger(MessagingEndpoint.class.getName());

    /**
     * The maximum number of devices to send a message to.
     */
    private static final int NUMBER_OF_DEVICES = 10;

    /**
     * The maximum number of times GCM will attempt to deliver the message.
     */
    private static final int MAXIMUM_RETRIES = 5;

    /**
     * Send to the first NUMBER_OF_DEVICE devices (you can modify this to send
     * to any number of devices or a specific device).
     * @param payload The message to send
     * @throws java.io.IOException if unable to send the message.
     */
    @ApiMethod(httpMethod = "POST")
    public final void sendMessage(final ImmutableMap<String, String> payload)
            throws IOException {
        if (payload == null || payload.size() == 0) {
            LOG.warning("Not sending message because payload is empty");
            return;
        }

        Sender sender = new Sender(Constants.GCM_API_KEY);
        Message msg = new Message.Builder()
                .setData(payload)
                .build();
        List<Registration> records = ofy().load()
                .type(Registration.class).limit(NUMBER_OF_DEVICES)
                .list();
        for (Registration record : records) {
            Result result = sender.send(msg, record.getRegId(),
                    MAXIMUM_RETRIES);
            if (result.getMessageId() != null) {
                LOG.info("Message sent to " + record.getRegId());
                String canonicalRegId = result.getCanonicalRegistrationId();
                if (canonicalRegId != null) {
                    // if the regId changed, we have to update the datastore
                    LOG.info("Registration Id changed for " + record.getRegId()
                            + " updating to "
                            + canonicalRegId);
                    record.setRegId(canonicalRegId);
                    ofy().save().entity(record).now();
                }
            } else {
                String error = result.getErrorCodeName();
                if (error.equals(com.google.android.gcm.server.
                        Constants.ERROR_NOT_REGISTERED)) {
                    LOG.warning("Registration Id " + record.getRegId()
                            + " no longer registered with GCM, "
                            + "removing from datastore");
                    // if the device is no longer registered with Gcm, remove it
                    // from the datastore
                    ofy().delete().entity(record).now();
                } else {
                    LOG.warning("Error when sending message : " + error);
                }
            }
        }
    }
}
