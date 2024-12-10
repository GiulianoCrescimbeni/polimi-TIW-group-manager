package it.polimi.tiw.groupsmanager.controllers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.groupsmanager.beans.User;
import it.polimi.tiw.groupsmanager.dao.UserDAO;
import it.polimi.tiw.groupsmanager.exceptions.IllegalCredentialsException;

@WebServlet("/register")
@MultipartConfig
public class Register extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection;
	
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

	public Register() {
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
		HttpSession session = request.getSession(true);
		if(session.getAttribute("userId") == null) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.sendRedirect(getServletContext().getContextPath() + "/signin");
		} else {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.sendRedirect(getServletContext().getContextPath() + "/homepage");
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String name = request.getParameter("name");
        String surname = request.getParameter("surname");
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String verifyPassword = request.getParameter("verifypassword");
		
		String error = null;
		
		if (username == null || username.isEmpty() || name == null || name.isEmpty() || surname == null || surname.isEmpty() || email == null || email.isEmpty() || password == null || password.isEmpty() || verifyPassword == null || verifyPassword.isEmpty()) {
			error = "Missing parameters";
		}
		
		if (!isValidEmail(email)) {
			error = "Email is not valid";
		}
		
		if(!password.equals(verifyPassword)) {
			error = "Passwords do not match";
		}
		
		UserDAO udao = new UserDAO(connection);
		
		try {
			if(udao.findUserByUsername(username) != null) {
				error = "Nickname already in use";
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		if (error != null) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.sendRedirect(request.getContextPath() + "/error?message=" + error);
			return;
		}
		
		try {
			HttpSession session = request.getSession(true);
			User user = udao.createUser(username, name, surname, email, password);
			session.setAttribute("userId", user.getId());
		} catch (IllegalCredentialsException | SQLException | NoSuchAlgorithmException e) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.sendRedirect(request.getContextPath() + "/error?message=" + e.getMessage());
			return;
		} 
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.sendRedirect(getServletContext().getContextPath() + "/homepage");
	}
	
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {
		}
	}
	

    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }
	
}
