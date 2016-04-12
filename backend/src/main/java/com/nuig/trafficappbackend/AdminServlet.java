package com.nuig.trafficappbackend;

import com.google.api.server.spi.ServiceException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.nuig.trafficappbackend.apis.IncidentEndpoint;
import com.nuig.trafficappbackend.apis.UserAccountEndpoint;
import com.nuig.trafficappbackend.models.Incident;
import com.nuig.trafficappbackend.models.UserAccount;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Created by Dylan Toner on 13/03/2016.
 */
public class AdminServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userPath = req.getServletPath();
        IncidentEndpoint ep;
        UserAccountEndpoint uep;
        if(userPath.equals("/insertIncident")) {
            ep = new IncidentEndpoint();
            UserService userService = UserServiceFactory.getUserService();
            User user = userService.getCurrentUser();
            String title = req.getParameter("title");
            String category = req.getParameter("category");
            String severity = req.getParameter("severity");
            String description = req.getParameter("description");
            float latit = Float.parseFloat(req.getParameter("lat"));
            float longit = Float.parseFloat(req.getParameter("long"));

            Incident temp = new Incident();
            temp.setTitle(title);
            temp.setCategory(category);
            temp.setSeverity(severity);
            temp.setDescription(description);
            temp.setLocation(new GeoPt(latit, longit));
            temp.setReportedBy("Administrator");
            temp.setTrustScore(-1);

            try {
                ep.insertIncident(temp, user);
            }
            catch(ServiceException e)
            {
                e.printStackTrace();
            }


            resp.sendRedirect("/admin/incidents.jsp");
        }//end insert

        else if(userPath.equals("/deleteIncident")) {
            ep = new IncidentEndpoint();
            UserService userService = UserServiceFactory.getUserService();
            User user = userService.getCurrentUser();
            long id = Long.parseLong(req.getParameter("id"));


            try {
                ep.removeIncident(id,user);
            }
            catch(ServiceException e)
            {
                e.printStackTrace();
            }

            resp.sendRedirect("/admin/incidents.jsp");
        }//end delete

        else if(userPath.equals("/editIncident")) {

            resp.sendRedirect("/admin/incidents.jsp");
        }//end edit

        else if(userPath.equals("/verifyIncident")) {
            ep = new IncidentEndpoint();
            UserService userService = UserServiceFactory.getUserService();
            User user = userService.getCurrentUser();
            long id = Long.parseLong(req.getParameter("id"));

            Incident temp=null;
            try {
                temp = ep.getIncident(id,user);

                temp.setTrustScore(-1);
                ep.updateIncident(temp,user);
            }
            catch(ServiceException e)
            {
                e.printStackTrace();
            }

            resp.sendRedirect("/admin/incidents.jsp");
        }//end verify

        else if(userPath.equals("/deleteUser")) {
            uep = new UserAccountEndpoint();

            UserService userService = UserServiceFactory.getUserService();
            User user = userService.getCurrentUser();
            String email = req.getParameter("email");


            try {
                uep.removeUserAccount(email,user);
            }
            catch(ServiceException e)
            {
                e.printStackTrace();
            }
            resp.sendRedirect("/admin/users.jsp");
        }
        else if(userPath.equals("/insertUser")) {
            uep = new UserAccountEndpoint();

            UserService userService = UserServiceFactory.getUserService();
            User user = userService.getCurrentUser();
            String email = req.getParameter("email");
            String name = req.getParameter("name");

            try {
                uep.addUserAccount(email,name,user);
            }
            catch(ServiceException e)
            {
                e.printStackTrace();
            }
            resp.sendRedirect("/admin/users.jsp");
        }

    }
}
