package it.polimi.tiw.groupsmanager.controllers;

import java.io.IOException;
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
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.groupsmanager.beans.Group;
import it.polimi.tiw.groupsmanager.beans.User;
import it.polimi.tiw.groupsmanager.dao.GroupDAO;
import it.polimi.tiw.groupsmanager.dao.UserDAO;

@WebServlet("/groupdetails")
public class GroupDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
	
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
		
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String path = RedirectionManager.getInstance().checkRedirection(request);
		if(path != null) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.sendRedirect(getServletContext().getContextPath() + path);
		} else {
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
			                
			                HttpSession session = request.getSession(true);
					    	if(group.getCreatorId() != (int) session.getAttribute("userId") && invitedUsersIds.contains((int) session.getAttribute("userId")) == false) {
					    		errorMessage = "Group not found.";
					    	}
				    	} else {
			                errorMessage = "Group not found.";
			            }	
			        } catch (SQLException e) {
			            errorMessage = "An error occurred while retrieving the group details.";
			        }
			    }
		    }
		    
		    final WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
		    ctx.setVariable("group", group);
		    ctx.setVariable("invitedUsers", invitedUsers);
		    ctx.setVariable("creator", creator);
		    ctx.setVariable("error", errorMessage);

		    templateEngine.process("/WEB-INF/groupdetails.html", ctx, response.getWriter());
		}
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
