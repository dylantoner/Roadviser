<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="com.nuig.trafficappbackend.models.Incident" %>
<%@ page import="com.googlecode.objectify.Key" %>
<%@ page import="com.googlecode.objectify.ObjectifyService" %>
<%@ page import="com.nuig.trafficappbackend.apis.IncidentEndpoint" %>
<html>
	<head>
	<title>Roadviser Admin Console</title>
		<link rel="stylesheet" href="/css/bootstrap.min.css">
		<link rel="stylesheet" href="/css/custom.css">
		<link rel="shortcut icon" href="/favicon.ico" />
		<script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js" ></script>
		<script src="//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>
		<script async src="/js/test.js"></script>
	</head>

	<body role="document">
	
  <div class="modal fade" id="myModalNorm" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <!-- Modal Header -->
            <div class="modal-header">
                <button type="button" class="close" 
                   data-dismiss="modal">
                       <span aria-hidden="true">&times;</span>
                       <span class="sr-only">Close</span>
                </button>
                <h4 class="modal-title" id="myModalLabel">New Incident</h4>
            </div>
            
            <!-- Modal Body -->
            <div class="modal-body">
                
                <form role="form" action="/insertIncident" method="post">
                  <div class="form-group">
                    <label for="title">Title</label>
                      <input type="text" class="form-control" name="title" id="title" placeholder="Title"/>
                  
					<label for="category">Category:</label>
					<select class="form-control" name="category" id="category">
						<option>Accident</option>
						<option>Congestion</option>
						<option>Road Works</option>
						<option>Weather</option>
						<option>Road Closure</option>
						<option>Obstruction</option>
						</select>
					</div>
				  <div class="form-group">
					<label for="severity">Severity:</label>
					<select class="form-control" name="severity" id="severity">
						<option>Low</option>
						<option>Medium</option>
						<option>High</option>
    
						</select>
					</div>
					<div class="form-group">
						<label for="description">Description:</label>
						<textarea class="form-control" rows="5" name="description" id="description"></textarea>
					</div>
					<div class="form-group">
						<label for="location">Location:</label>
						<input type="text" class="form-control" name="lat" id="location" placeholder="Latitude"/>
						<input type="text" class="form-control" name="long" id="location" placeholder="Longitude"/>
					</div>
				
                  <button type="submit" class="btn btn-default">Submit</button>
                </form>
                
                
            </div>
            
            <!-- Modal Footer -->
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              
            </div>
        </div>
    </div>
</div>
  
  
  
		<div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
			<div class="container">
				<div class="navbar-header">
					<a class="navbar-brand" href="#">Roadviser Admin Console</a>
				</div>
				<div class="navbar-collapse collapse">
					<ul class="nav navbar-nav">
						<li class="dropdown">
						<li><a href="/_ah/api/explorer">Google Cloud Endpoints API Explorer</a></li>
					</ul>
					<%
						UserService userService = UserServiceFactory.getUserService();
						User user = userService.getCurrentUser();
						if (user != null) {
							pageContext.setAttribute("user", user);
					%>

					<p>Hello, ${fn:escapeXml(user.nickname)}! (You can
						<a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">sign out</a>.)</p>
						<%
							} else {
						%>
					<p>Hello!
						<a href="<%= userService.createLoginURL(request.getRequestURI()) %>">Sign in</a></p>
						<%
							}
						%>
				</div>
			</div>
		</div>
		<div id="wrapper">

		<!-- Sidebar -->
			<div id="sidebar-wrapper">
				<ul class="sidebar-nav">
					<li class="sidebar-brand">
						<a href="#">Start Bootstrap</a>
					</li>
					<li>
						<a href="/admin/dashboard.jsp">Dashboard</a>
					</li>
					<li>
						<a href="/admin/incidents.jsp">Incidents</a>
					</li>
					<li>
						<a href="/admin/users.jsp">Users</a>
					</li>
				</ul>
			</div>
																							

			<!-- Page Content -->
			<div id="page-content-wrapper" style="padding-top: 70px;">
			
				
				
				<div class="container-fluid">
					<div class="row">
						<div class="col-lg-12">
							<div class="container theme-showcase" role="main">
								<h1>Incidents</h1>
								<p>Incidents currently in the system:</p>
							
								<%
									IncidentEndpoint ep = new IncidentEndpoint();
									List<Incident> incidents = ep.listIncidents(user);

									if (incidents.isEmpty()) {
								%>
									<p>No incidents found</p>
								<%
									} else {
								%>
									<table class="table table-striped table-hover table-users">
									<thead>
										<tr>
											<th>Time</th>
											<th>Title</th>
											<th>Details</th>
											<th>Category</th>
											<th>Severity</th>
											<th>Reported By</th>
											<th>Trust Score</th>
											<th>Coordinates</th>
											<th></th>
											<th></th>
										</tr>
									</thead>
									<tbody>
								<%
									// Look at all of our incidents
									for (Incident incident : incidents) {
										pageContext.setAttribute("incident_timestamp", incident.getTimestamp());
										pageContext.setAttribute("incident_title", incident.getTitle());
										pageContext.setAttribute("incident_details", incident.getDescription());
										pageContext.setAttribute("incident_category", incident.getCategory());
										pageContext.setAttribute("incident_severity", incident.getSeverity());
										pageContext.setAttribute("incident_location", incident.getLocation());
										pageContext.setAttribute("incident_reportedBy", incident.getReportedBy());
										pageContext.setAttribute("incident_trustScore", Integer.toString(incident.getTrustScore()));
										pageContext.setAttribute("incident_id", incident.getIncidentId());
										
								%>
									<tr>
									   <td>${fn:escapeXml(incident_timestamp)}</td>
									   <td>${fn:escapeXml(incident_title)}</td>
									   <td>${fn:escapeXml(incident_details)}</td>
									   <td>${fn:escapeXml(incident_category)}</td>
									   <td>${fn:escapeXml(incident_severity)}</td>
									   <td>${fn:escapeXml(incident_reportedBy)}</td>
									   <td>${fn:escapeXml(incident_trustScore)}</td>
									   <td>${fn:escapeXml(incident_location)}</td>
									   <td><form action = "/verifyIncident" method = "post">
										<button class="btn btn-success" type="submit" name="id" value=${fn:escapeXml(incident_id)}>Confirm</button></form></td>
									   <td><form action = "/deleteIncident" method = "post">
										<button class="btn btn-danger" type="submit" name="id" value=${fn:escapeXml(incident_id)}>Delete</button></form></td>
									</tr>
								<%
										}
									}
								%>
								</tbody>
								</table>
								<button class="btn btn-success btn-lg" data-toggle="modal" data-target="#myModalNorm">New Incident</button>
							</div>
						</div>
					</div>
				</div>
			</div>
																						
		</div>
	</body>
</html>