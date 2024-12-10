package it.polimi.tiw.groupsmanager.controllers;

import java.io.IOException;
import java.io.PrintWriter;
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

import org.json.JSONObject;

import it.polimi.tiw.groupsmanager.beans.User;
import it.polimi.tiw.groupsmanager.dao.UserDAO;

@WebServlet("/checkusername")
@MultipartConfig
public class CheckUsername extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection;

	public CheckUsername() {
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
	
	public void doGet(HttpServletRequest req, HttpServletResponse res) {
		String username = req.getParameter("username");
		UserDAO udao = new UserDAO(connection);
		User toCheck = null;
		boolean exists = false;
		
		try {
			toCheck = udao.findUserByUsername(username);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (toCheck != null) exists = true;
		
        res.setContentType("application/json");
        PrintWriter out = null;
		try {
			out = res.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
        JSONObject json = new JSONObject();
        json.put("exists", exists);
        out.print(json.toString());
        out.flush();
	}
	
}