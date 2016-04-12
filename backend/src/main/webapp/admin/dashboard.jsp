<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="com.googlecode.objectify.Key" %>
<%@ page import="com.googlecode.objectify.ObjectifyService" %>
<%@ page import="com.nuig.trafficappbackend.models.Incident" %>
<%@ page import="com.nuig.trafficappbackend.models.UserAccount" %>
<%@ page import="com.nuig.trafficappbackend.apis.UserAccountEndpoint" %>
<%@ page import="com.nuig.trafficappbackend.apis.IncidentEndpoint" %>
<html>
	<head>
	<title>Roadviser Admin Console</title>
		<link rel="stylesheet" href="/css/bootstrap.min.css">
		<link rel="stylesheet" href="/css/custom.css">
		<link rel="shortcut icon" href="/favicon.ico" />
	</head>

	<body role="document" >
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
								 <div class="jumbotron">
									<div class="row">
									
											<h1>Roadviser</h1>
											<p>Welcome to the Roadviser admin console.</p>
											<%
												IncidentEndpoint iep = new IncidentEndpoint();
												UserAccountEndpoint uep = new UserAccountEndpoint();
												
												List<Incident> incidents = iep.listIncidents(user);

												pageContext.setAttribute("num_incidents", Integer.toString(incidents.size()));
												
												List<UserAccount> userAccounts = uep.listUserAccounts(user);
												pageContext.setAttribute("num_users", Integer.toString(userAccounts.size()));
											%>
											<p>
												<b>Current Incidents reported:</b> &nbsp;${fn:escapeXml(num_incidents)}
												<br>
												<b>Current registered users: </b> &nbsp;${fn:escapeXml(num_users)}
											</p>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
																							

		</div>


	</body>
</html>