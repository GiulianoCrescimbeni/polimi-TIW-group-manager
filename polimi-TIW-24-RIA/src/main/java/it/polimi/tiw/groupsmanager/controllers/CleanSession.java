package it.polimi.tiw.groupsmanager.controllers;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/clean")
@MultipartConfig
public class CleanSession extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public CleanSession() {
		super();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(true);
		session.removeAttribute("groupTitle");
		session.removeAttribute("groupDurationDate");
		session.removeAttribute("groupMinParticipants");
		session.removeAttribute("groupMaxParticipants");
		session.removeAttribute("errors");
		
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
