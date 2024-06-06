package it.polimi.tiw.groupsmanager.controllers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.groupsmanager.beans.User;
import it.polimi.tiw.groupsmanager.dao.UserDAO;
import it.polimi.tiw.groupsmanager.exceptions.IllegalCredentialsException;

@WebServlet("/login")
@MultipartConfig
public class Login extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection;

	public Login() {
		super();
	}
	
	public void init() throws ServletException {
		try {
			ServletContext context = getServletContext();
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
		response.sendRedirect(getServletContext().getContextPath() + "/login.html");
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		
		String error = null;
		
		if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
			error = "Some parameters are empty";
		}
		
		if (error != null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(error);
			return;
		}
		
		UserDAO udao = new UserDAO(connection);
		try {
			User user = udao.checkCredentials(email, password);
		} catch (IllegalCredentialsException | SQLException | NoSuchAlgorithmException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
		} 
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.sendRedirect(getServletContext().getContextPath() + "/index.html");
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
