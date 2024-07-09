package it.polimi.tiw.groupsmanager.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class RedirectionManager {
	
	private static RedirectionManager instance;

	private RedirectionManager() {}
	
	public static RedirectionManager getInstance() {
		if (instance == null) instance = new RedirectionManager();
        return instance;
	}
	
	public String checkRedirection(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		String path = null;
		if(session.getAttribute("userId") == null) {
			path = "/login";
		} else if(session.getAttribute("groupTitle") != null) {
			path = "/invitation";
		}
		return path;
	}
	
}
