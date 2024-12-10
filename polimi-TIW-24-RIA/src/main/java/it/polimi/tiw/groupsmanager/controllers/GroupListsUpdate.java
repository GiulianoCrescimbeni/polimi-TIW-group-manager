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
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import it.polimi.tiw.groupsmanager.beans.Group;
import it.polimi.tiw.groupsmanager.dao.GroupDAO;

@WebServlet("/updategroups")
@MultipartConfig
public class GroupListsUpdate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;

	public GroupListsUpdate() {
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
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(true);
		String error = null;
		
    	if (session.getAttribute("userId") == null) {
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    		response.getWriter().write("User not logged");
    		return;
    	}
		
		GroupDAO gDAO = new GroupDAO(connection);
		List<Group> createdGroups = new ArrayList<>();
		List<Group> invitedGroups = new ArrayList<>();
		
        try {
			createdGroups = gDAO.findGroupsByCreatorId((int) session.getAttribute("userId"));
			invitedGroups = gDAO.getGroupsWhereInvited((int) session.getAttribute("userId"));
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
            return;
		}
        
        StringBuilder html = new StringBuilder();
        
        html.append("<div class=\"user-groups\">")
        	.append("<h2>Groups created by you:</h2>");
        
        if (createdGroups.isEmpty()) {
        	html.append("<p>You haven't created any groups yet.</p>")
        		.append("</div>");
        } else {
        	html.append("<ul>");
        	for (Group g : createdGroups) {
        	    html.append("<li>")
        	    	.append("<a href=\"javascript:void(0)\" class=\"group-link\" data-group-id=\"").append(g.getId()).append("\">").append(g.getTitle()).append("</a>")
        	    	.append("</li>");
        	}
        	
        	html.append("</ul>")
        		.append("</div>");
        }
        
        
        html.append("<div class=\"invited-groups\">")
            .append("<h2>Groups where you are invited:</h2>");
            if (invitedGroups.isEmpty()) {
            	html.append("<p>You haven't created any groups yet.</p>")
            		.append("</div>");
            } else {
            	html.append("<ul>");
            	for (Group g : invitedGroups) {
            	    html.append("<li>")
            	    	.append("<a href=\"javascript:void(0)\" class=\"group-link\" data-group-id=\"").append(g.getId()).append("\">").append(g.getTitle()).append("</a>")
            	    	.append("</li>");
            	}
            	
            	html.append("</ul>")
            		.append("</div>");
            }
            
            response.setContentType("application/json");
            
            String toSend = html.toString();
            
            if (error != null) toSend = error;
            
    		JsonObject json = new JsonObject();
    		json.addProperty("html", toSend);
    		
    		 
    		Gson gson = new Gson();
    		
    		
    		PrintWriter out = response.getWriter();
    		out.println(gson.toJson(json));
    		out.close();
            
	}
	
}
