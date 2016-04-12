package com.nuig.trafficappbackend;

        import com.nuig.trafficappbackend.models.Incident;
        import com.googlecode.objectify.Objectify;
        import com.googlecode.objectify.ObjectifyFactory;
        import com.googlecode.objectify.ObjectifyService;
        import com.nuig.trafficappbackend.models.Registration;
        import com.nuig.trafficappbackend.models.UserAccount;

/**
 * Objectify service wrapper so we can statically register our persistence classes
 * More on Objectify here : https://code.google.com/p/objectify-appengine/
 *
 */
public final class OfyService {
    /**
     * Default constructor, never called.
     */
    private OfyService() {
    }

    static {
        factory().register(Incident.class);
        factory().register(Registration.class);
        factory().register(UserAccount.class);

    }

    /**
     * Returns the Objectify service wrapper.
     * @return The Objectify service wrapper.
     */
    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    /**
     * Returns the Objectify factory service.
     * @return The factory service.
     */
    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}

