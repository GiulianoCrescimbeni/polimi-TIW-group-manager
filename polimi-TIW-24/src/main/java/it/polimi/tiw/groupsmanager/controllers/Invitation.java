package it.polimi.tiw.groupsmanager.controllers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.groupsmanager.beans.User;
import it.polimi.tiw.groupsmanager.dao.UserDAO;
import it.polimi.tiw.groupsmanager.exceptions.IllegalCredentialsException;

@WebServlet("/invitation")
@MultipartConfig
public class Invitation extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;

	public Invitation() {
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
	    HttpSession session = request.getSession(true);
	    if (session.getAttribute("groupTitle") == null) {
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");
	        response.sendRedirect(getServletContext().getContextPath() + "/homepage");
	    } else {
	        String path = "/WEB-INF/invitation.html";
	        ServletContext servletContext = getServletContext();
	        UserDAO udao = new UserDAO(connection);
	        List<User> users = new ArrayList<>();
	        try {
	            List<User> allUsers = udao.findAllUsers();
	            User thisUser = udao.findUserById((int) session.getAttribute("userId"));
	            users = allUsers.stream()
	            		.filter(user -> !user.getUsername().equals(thisUser.getUsername()))
	            		.sorted(Comparator.comparing(User::getSurname))
	            		.collect(Collectors.toList());
	            
	        } catch (SQLException e) {
	            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	            response.getWriter().println(e.getMessage());
	            return;
	        }

	        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
	        ctx.setVariable("users", users);
	        templateEngine.process(path, ctx, response.getWriter());
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
