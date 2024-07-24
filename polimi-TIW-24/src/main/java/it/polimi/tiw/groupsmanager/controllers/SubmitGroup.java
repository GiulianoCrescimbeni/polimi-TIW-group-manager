package it.polimi.tiw.groupsmanager.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.groupsmanager.beans.User;
import it.polimi.tiw.groupsmanager.dao.GroupDAO;
import it.polimi.tiw.groupsmanager.dao.UserDAO;

@WebServlet("/submitgroup")
@MultipartConfig
public class SubmitGroup extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;

	public SubmitGroup() {
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
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    HttpSession session = request.getSession(true);
	    if (session.getAttribute("groupTitle") == null) {
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");
	        response.sendRedirect(getServletContext().getContextPath() + "/homepage");
	    } else {
	    	
	    	final String[] error = { null };
	    	
	    	String[] selectedUserIds = request.getParameterValues("participants");
	        session.setAttribute("selectedUserIds", selectedUserIds);

	    	if(request.getParameter("participants") == null) {
	    		if((int) session.getAttribute("errors") == 2) {
	        		error[0] = "Too many incorrect attempts, recreate the group";
	        		session.removeAttribute("groupTitle");
	        		session.removeAttribute("groupDurationDate");
	        		session.removeAttribute("groupMinParticipants");
	        		session.removeAttribute("groupMaxParticipants");
	        		session.removeAttribute("selectedUserIds");
	        		session.removeAttribute("errors");
	        	} else {
	        		error[0] = "No users selected.";
	        		session.setAttribute("errors", (int) session.getAttribute("errors") + 1);
	        	}
	    	} else {
	    		
		        List<Integer> userIds = Arrays.stream(selectedUserIds)
		                                           .map(Integer::parseInt)
		                                           .collect(Collectors.toList());
		        
		        
		        if(userIds.size() < (int) session.getAttribute("groupMinParticipants") || userIds.size() > (int) session.getAttribute("groupMaxParticipants")) {
		        	if((int) session.getAttribute("errors") == 2) {
		        		error[0] = "Too many incorrect attempts, recreate the group";
		        		session.removeAttribute("groupTitle");
		        		session.removeAttribute("groupDurationDate");
		        		session.removeAttribute("groupMinParticipants");
		        		session.removeAttribute("groupMaxParticipants");
		        		session.removeAttribute("selectedUserIds");
		        		session.removeAttribute("errors");
		        	} else {
		        		if(userIds.size() < (int) session.getAttribute("groupMinParticipants")) {
		        			session.setAttribute("errors", (int) session.getAttribute("errors") + 1);
				        	error[0] = "Too few selected users, add " + String.valueOf((int) session.getAttribute("groupMinParticipants") - userIds.size()); 
		        		} else {
		        			session.setAttribute("errors", (int) session.getAttribute("errors") + 1);
				        	error[0] = "Too many selected users, remove " + String.valueOf(userIds.size() - (int) session.getAttribute("groupMaxParticipants")); 
		        		}
		        	}
		        } else {
		        	UserDAO udao = new UserDAO(connection);
			        userIds.forEach(id -> {
			        	try {
							User user = udao.findUserById((id));
							if(user == null) {
								error[0] = "One of the users doesn't exists";
							}
						} catch (SQLException e) {
							response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
							try {
								response.getWriter().println(e.getMessage());
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							return;
						}
			        });
			        
			        if(error[0] == null) {
			        	GroupDAO gdao = new GroupDAO(connection);
					    try {
					    	connection.setAutoCommit(false);
					    	int groupId = gdao.createGroup((String) session.getAttribute("groupTitle"), (int) session.getAttribute("groupDurationDate"), (int) session.getAttribute("groupMinParticipants"), (int) session.getAttribute("groupMaxParticipants"), (int) session.getAttribute("userId"));
							userIds.forEach(id -> {
								try {
									gdao.inviteInGroup(id, groupId);
								} catch (SQLException e) {
									response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
									e.printStackTrace();
									try {
										connection.rollback();
										connection.setAutoCommit(true);
										return;
									} catch (SQLException e1) {
										e1.printStackTrace();
									}
									
									try {
										response.getWriter().println(e.getMessage());
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
							});
							connection.commit();
							connection.setAutoCommit(true);
					    } catch (SQLException e) {
							e.printStackTrace();
							try {
								connection.rollback();
								connection.setAutoCommit(true);
								return;
							} catch (SQLException e1) {
								e1.printStackTrace();
							}
						}
			        }
		        }
	    	}
		    
		    if (error[0] != null) {
		    	response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.sendRedirect(request.getContextPath() + "/error?message=" + error[0]);
				return;
			} else {
				session.removeAttribute("groupTitle");
        		session.removeAttribute("groupDurationDate");
        		session.removeAttribute("groupMinParticipants");
        		session.removeAttribute("groupMaxParticipants");
        		session.removeAttribute("selectedUserIds");
        		session.removeAttribute("errors");
				response.setStatus(HttpServletResponse.SC_OK);
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    response.sendRedirect(getServletContext().getContextPath() + "/homepage");
			}		    
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
