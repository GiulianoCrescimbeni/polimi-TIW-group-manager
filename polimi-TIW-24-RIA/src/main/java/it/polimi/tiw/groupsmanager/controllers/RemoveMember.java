package it.polimi.tiw.groupsmanager.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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

import it.polimi.tiw.groupsmanager.beans.Group;
import it.polimi.tiw.groupsmanager.dao.GroupDAO;
import it.polimi.tiw.groupsmanager.dao.UserDAO;

@WebServlet("/removemember")
@MultipartConfig
public class RemoveMember extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection;
	
	public RemoveMember() {
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
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(true);
		
		if (session.getAttribute("userId") == null) {
	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        response.getWriter().write("User not logged");
	        return;
		} else {
			int groupId = 0;
			int memberId = 0;
			Group group = null;
			List<Integer> users = null;
			String errorMessage = null;
			UserDAO udao = new UserDAO(connection);
			GroupDAO gdao = new GroupDAO(connection);
			
			if (request.getParameter("groupId") == null || request.getParameter("memberId") == null) {
				errorMessage = "Missing parameters";
			} else {
		    	try {
					groupId = Integer.parseInt(request.getParameter("groupId"));
				} catch (NumberFormatException e) {
					errorMessage = "Group not found";
				}
		    	
		    	try {
					memberId = Integer.parseInt(request.getParameter("memberId"));
				} catch (NumberFormatException e) {
					errorMessage = "Member not found";
				}
		    	
		    	if (errorMessage == null) {
		    		
		    		try {
		    			group = gdao.findGroupById(groupId);
		    			users = gdao.getInvitedUsers(groupId);
		    			
		    			
		    			if (group == null) errorMessage = "Group not found";
		    			
		    			if (users.size() != 0) {
		    				
		    				if (users.size() - 1 >= group.getMinParticipants()) {
		    					
		    					if (users.contains(memberId)) {
			    					gdao.removeFromGroup(memberId, groupId);
			    					response.setStatus(HttpServletResponse.SC_OK);
		    					} else errorMessage = "The member is not part of the group";
		    					

		    				} else errorMessage = "Cannot delete the member";
		    				
		    			} else errorMessage = "Members not found";
		    			
		    		} catch (Exception e) {
		    			e.printStackTrace();
		    		}
		    	}
		    	
		    	if (errorMessage != null) {
	                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	                response.getWriter().println(errorMessage);
	                return;
		    	}
			}
			
		}
	}
	
	
}