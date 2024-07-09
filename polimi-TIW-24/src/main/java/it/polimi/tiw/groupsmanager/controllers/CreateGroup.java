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
import it.polimi.tiw.groupsmanager.dao.GroupDAO;
import it.polimi.tiw.groupsmanager.dao.UserDAO;


@WebServlet("/creategroup")
@MultipartConfig
public class CreateGroup extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
	
	public CreateGroup() {
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
		if(session.getAttribute("userId") == null) {
			String path = "/WEB-INF/login.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			templateEngine.process(path, ctx, response.getWriter());
		} else {
			String title = request.getParameter("title");
			String durationDateParameter = request.getParameter("duration");
			String minParticipantsParameter = request.getParameter("minimumParticipants");
			String maxParticipantsParameter = request.getParameter("maximumParticipants");
			int durationDate = 0;
			int minParticipants = 0;
			int maxParticipants = 0;

			String error = null;
			
			UserDAO udao = new UserDAO(connection);
			List<User> users = new ArrayList<>();

			if (title == null || title.isEmpty() || durationDateParameter == null || durationDateParameter.isEmpty() || minParticipantsParameter == null
					|| minParticipantsParameter.isEmpty() || maxParticipantsParameter == null || maxParticipantsParameter.isEmpty()) {
				error = "Missing parameters";
			}
			
			try {
				durationDate = Integer.parseInt(durationDateParameter);
			} catch (NumberFormatException e) {
				error = "Incorrect duration date format";
			}
			
			try {
				minParticipants = Integer.parseInt(minParticipantsParameter);
			} catch (NumberFormatException e) {
				error = "Incorrect minimum participants format";
			}
			
			try {
				maxParticipants = Integer.parseInt(maxParticipantsParameter);
			} catch (NumberFormatException e) {
				error = "Incorrect maximum participants format";
			}
			
			if(durationDate <= 0) {
				error = "Incorrect duration format";
			}
			
			if(minParticipants > maxParticipants || minParticipants <= 0 || maxParticipants <= 0) {
				error = "Incorrect participants format";
			}
			
			try {
				users = udao.findAllUsers();
				if(maxParticipants > users.size() - 1) {
					error = "The number of maximum users selected is greater than the number of registered users, try again";
				}
			} catch (SQLException e) {
				throw new UnavailableException("Couldn't get db connection");
			}
			
			
			if (error != null) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println(error);
				return;
			}
			
			session.setAttribute("groupTitle", title);
			session.setAttribute("groupDurationDate", durationDate);
			session.setAttribute("groupMinParticipants", minParticipants);
			session.setAttribute("groupMaxParticipants", maxParticipants);
			session.setAttribute("errors", 0);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.sendRedirect(getServletContext().getContextPath() + "/homepage");

		}
		
	}
	
}
