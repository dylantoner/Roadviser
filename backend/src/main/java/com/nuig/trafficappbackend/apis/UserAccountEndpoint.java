package com.nuig.trafficappbackend.apis;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.nuig.trafficappbackend.Constants;
import com.nuig.trafficappbackend.models.Registration;
import com.nuig.trafficappbackend.models.UserAccount;
import com.nuig.trafficappbackend.utils.EndpointUtil;
import com.nuig.trafficappbackend.models.Incident;
import java.util.List;
import java.util.logging.Logger;
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
@ApiClass(resource  = "userAccounts",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.API_EXPLORER_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID}
)
public class UserAccountEndpoint {

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
    public final List<UserAccount> listUserAccounts(final User user) {
        return ofy().load().type(UserAccount.class).list();
    }

    /**
     * Gets the entity having primary key id.
     * @param email the primary key of the java bean Incident entity.
     * @param user the user requesting the entity.
     * @return The entity with primary key id.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "GET")
    public final UserAccount getUserAccount(@Named("email") final String email, final User user)
            throws ServiceException {
        return findUserAccount(email);
    }

    /**
     * Inserts the entity into App Engine datastore. It uses HTTP POST method.

     * @param user the user inserting the entity.
     * @return The inserted entity.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "POST")
    public final void addUserAccount(@Named("email") final String email, @Named("name") final String name,final User user) throws UnauthorizedException {
        //EndpointUtil.throwIfNotAuthenticated(user);
        if (findUserAccount(email) != null) {
            LOG.info("User " + email + " already registered, skipping register");
            return;
        }
        UserAccount userAccount = new UserAccount();
        userAccount.setEmail(email);
        userAccount.setDisplayName(name);
        ofy().save().entity(userAccount).now();
    }

    @ApiMethod(httpMethod = "DELETE")
    public final void removeUserAccount(@Named("email") final String email, final User user)
            throws ServiceException {
        EndpointUtil.throwIfNotAuthenticated(user);

        UserAccount account = findUserAccount(email);
        if (account == null) {
            LOG.info(
                    "User " + email + " not found, skipping deletion.");
            return;
        }

        ofy().delete().entity(account).now();
    }

    /**
     * Updates an entity. It uses HTTP PUT method.
     * @param userAccount the entity to be updated.
     * @param user the user modifying the entity.
     * @return The updated entity.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "PUT")
    public final UserAccount updateUserAcount(final UserAccount userAccount, final User user) throws
            ServiceException {
        EndpointUtil.throwIfNotAuthenticated(user);

        ofy().save().entity(userAccount).now();

        return userAccount;
    }

    /**
     * Removes the entity with primary key id. It uses HTTP DELETE method.
     * @param id the primary key of the entity to be deleted.
     * @param user the user deleting the entity.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    /*@ApiMethod(httpMethod = "DELETE")
    public final void removeUserAccount(@Named("id") final Long id, final User user)
            throws ServiceException {
        EndpointUtil.throwIfNotAuthenticated(user);

        UserAccount userAccount = findUserAccount(id);
        if (userAccount == null) {
            LOG.info(
                    "User Account " + id + " not found, skipping deletion.");
            return;
        }
        ofy().delete().entity(userAccount).now();
    }*/

    /**
     * Searches an entity by ID.
     * @param email the email address to search
     * @return the Incident associated to id
     */
    private UserAccount findUserAccount(final String email) {
        return ofy().load().type(UserAccount.class)
                .filter("email", email).first().now();
    }
}
