<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="com.nuig.trafficappbackend.models.UserAccount" %>
<%@ page import="com.googlecode.objectify.Key" %>
<%@ page import="com.googlecode.objectify.ObjectifyService" %>
<%@ page import="com.nuig.trafficappbackend.apis.UserAccountEndpoint" %>
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

	<body role="document" >
	
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
                <h4 class="modal-title" id="myModalLabel">New User</h4>
            </div>
            
            <!-- Modal Body -->
            <div class="modal-body">
                
                <form role="form" action="/insertUser" method="post">
                  <div class="form-group">
                    <label for="email">Email</label>
                      <input type="text" class="form-control" name="email" id="email" placeholder="Email"/>
				</div>
				<div class="form-group">
                    <label for="Name">Name</label>
                      <input type="text" class="form-control" name="name" id="name" placeholder="Name"/>
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
								<h1>Users</h1>
								<p>Users currently registered:</p>
							
								<%
									UserAccountEndpoint ep = new UserAccountEndpoint();
									List<UserAccount> userAccounts = ep.listUserAccounts(user);
									

									if (userAccounts.isEmpty()) {
								%>
									<p>No users found</p>
								<%
									} else {
								%>
									<table class="table table-striped">
									<thead>
										<tr>
											<th>Name</th>
											<th>Email</th>
											<th>Date Joined</th>
											<th>Reputation</th>
											<th>Incidents Reported</th>
											<th></th>
										</tr>
									</thead>
									<tbody>
								<%
									// Look at all of our users
									for (UserAccount usr : userAccounts) {
										pageContext.setAttribute("user_name", usr.getDisplayName());
										pageContext.setAttribute("user_email", usr.getEmail());
										pageContext.setAttribute("user_dateJoined", usr.getDateJoined());
										pageContext.setAttribute("user_reputation", Integer.toString(usr.getReputation()));
										pageContext.setAttribute("user_numIncidents", Integer.toString(usr.getNumIncidents()));
										
										
								%>
									<tr>
									   <td>${fn:escapeXml(user_name)}</td>
									   <td>${fn:escapeXml(user_email)}</td>
									   <td>${fn:escapeXml(user_dateJoined)}</td>
									   <td>${fn:escapeXml(user_reputation)}</td>
									   <td>${fn:escapeXml(user_numIncidents)}</td>
									   <td><form action = "/deleteUser" method = "post">
										<button class="btn btn-danger" type="submit" name="email" value=${fn:escapeXml(user_email)}>Delete</button></form></td>
									</tr>
								<%
										}
									}
								%>
								</tbody>
								</table>
								<button class="btn btn-success btn-lg" data-toggle="modal" data-target="#myModalNorm">New User</button>
							</div>
						</div>
					</div>
				</div>
			</div>
																							

		</div>


	</body>
</html>