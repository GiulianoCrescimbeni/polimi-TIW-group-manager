package it.polimi.tiw.groupsmanager.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import it.polimi.tiw.groupsmanager.beans.Group;
import it.polimi.tiw.groupsmanager.beans.User;
import it.polimi.tiw.groupsmanager.dao.GroupDAO;
import it.polimi.tiw.groupsmanager.dao.UserDAO;

@WebServlet("/groupdetails")
public class GroupDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	
	public GroupDetails() {
		super();
	}
	
	public void init() throws ServletException {
		ServletContext context = getServletContext();
		try {
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);

		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	HttpSession session = request.getSession(true);
    	if (session.getAttribute("userId") == null) {
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    		response.getWriter().write("User not logged");
    		return;
    	}

		int groupId = 0;
		GroupDAO gdao = new GroupDAO(connection);
		UserDAO udao = new UserDAO(connection);
		Group group = null;
		List<User> invitedUsers = new ArrayList<>();
		User creator = null;
		String errorMessage = null;
		    
		if(request.getParameter("id") == null) {
		   	errorMessage = "Group not found";
		}  else {
		   	try {
				groupId = Integer.parseInt(request.getParameter("id"));
			} catch (NumberFormatException e) {
				errorMessage = "Group not found";
			}
		    	
		   	if(errorMessage == null) {
		    	try {
			    	group = gdao.findGroupById(groupId);
			    	if (group != null) {
			    		List<Integer> invitedUsersIds = gdao.getInvitedUsers(group.getId());
			    		invitedUsersIds.forEach(id -> {
			    			try {
			    				invitedUsers.add(udao.findUserById(id));
			    			} catch (SQLException e) {
			    				e.printStackTrace();
			    			}
		    	        });
		                creator = udao.findUserById(group.getCreatorId());
			                
			                
				    	if(group.getCreatorId() != (int) session.getAttribute("userId") && invitedUsersIds.contains((int) session.getAttribute("userId")) == false) {
				    		errorMessage = "Group not found";
				    	}
			    	} else {
		                errorMessage = "Group not found";
		            }	
		        } catch (SQLException e) {
		            errorMessage = "An error occurred while retrieving the group details";
		        }
		    }
		}
		    
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		String html = getGroupDetailsHtml(group, invitedUsers, creator, errorMessage);
		boolean isAdmin = false;
		   
		if (group.getCreatorId() == (int) request.getSession(true).getAttribute("userId")) isAdmin = true;
		   
		JsonObject json = new JsonObject();
		json.addProperty("html", html);
		json.addProperty("isAdmin", isAdmin);
		   
		Gson gson = new Gson();
		        
		out.println(gson.toJson(json));
		out.close();
		
	}
    
    private String getGroupDetailsHtml(Group group, List<User> invitedUsers, User creator, String error) {
    	
    	if (error != null) {
    		return "<p>" + error + "</p>";
    	}
    	
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"group-details\">")
        	.append("<p><strong>Name:</strong> <span>").append(group.getTitle()).append("</span></p>")
            .append("<p><strong>Duration:</strong> <span>").append(group.getActivityDuration()).append("</span> days</p>")
            .append("<p><strong>Minimum Participants:</strong> <span>").append(group.getMinParticipants()).append("</span></p>")
            .append("<p><strong>Maximum Participants:</strong> <span>").append(group.getMaxParticipants()).append("</span></p>")
            .append("<p><strong>Creator:</strong> <span>").append(creator.getUsername()).append("</span></p>")
            .append("<p><strong>Creation Date:</strong> <span>").append(group.getCreationDate()).append("</span></p>")
            .append("<p><strong>Members List:</strong></p>")
            .append("</div>");

        if (invitedUsers.isEmpty()) {
            html.append("<p>No users invited to this group.</p>");
        } else {
            html.append("<ul>");
            for (User u : invitedUsers) {
                html.append("<li class=\"member\" data-member-id=\"").append(u.getId()).append("\">")
                    .append("<div class=\"user-info\">")
                    .append("<span>").append(u.getName()).append("</span>")
                    .append("<span>&nbsp;</span>")
                    .append("<span>").append(u.getSurname()).append("</span>")
                    .append("</div>")
                    .append("</li>");
            }
            html.append("</ul>");
        }

        return html.toString();
    }
  
    
    public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {
		}
	}
}
