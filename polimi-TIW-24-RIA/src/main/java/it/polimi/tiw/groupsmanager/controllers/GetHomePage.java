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

import it.polimi.tiw.groupsmanager.beans.Group;
import it.polimi.tiw.groupsmanager.dao.GroupDAO;

@WebServlet("/homepage")
@MultipartConfig
public class GetHomePage extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;

	public GetHomePage() {
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
		if(session.getAttribute("userId") == null) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.sendRedirect(getServletContext().getContextPath() + "/signin");
		} else {
			
			if (session.getAttribute("groupTitle") != null) {
				session.removeAttribute("groupTitle");
				session.removeAttribute("groupDurationDate");
				session.removeAttribute("groupMinParticipants");
				session.removeAttribute("groupMaxParticipants");
				session.removeAttribute("errors");
			}
			
			String path = "/WEB-INF/homepage.html";
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
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("createdGroups", createdGroups);
	        ctx.setVariable("invitedGroups", invitedGroups);
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
