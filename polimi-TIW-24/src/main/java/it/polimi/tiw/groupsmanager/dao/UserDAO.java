package it.polimi.tiw.groupsmanager.dao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.groupsmanager.beans.User;
import it.polimi.tiw.groupsmanager.exceptions.IllegalCredentialsException;

public class UserDAO {
	private Connection con;
	
	public UserDAO(Connection connection) {
		this.con = connection;
	}
	
	public User findUserById(int userId) throws SQLException {
		User user = new User();
		String query = "SELECT id, username, email FROM user WHERE id = ?";
		
		ResultSet result = null;
		PreparedStatement pstatement = null;
		
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, userId);
			result = pstatement.executeQuery();
			user.setId(result.getInt("id"));
			user.setUsername(result.getString("username"));
			user.setEmail(result.getString("email"));
		} catch (SQLException e) {
			throw new SQLException(e);

		} finally {
			try {
				if (result != null) {
					result.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		return user;
	}
	
	public User checkCredentials(String email, String password) throws SQLException, IllegalCredentialsException, NoSuchAlgorithmException {
		String query = "SELECT  id, username, email FROM users WHERE email = ? AND password =?";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(password.getBytes());
			String passwordHash = new String(messageDigest.digest());
			pstatement.setString(1, email);
			pstatement.setString(2, passwordHash);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) 
					throw new IllegalCredentialsException("Invalid Email or Password");
				else {
					result.next();
					User user = new User();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));
					user.setEmail(result.getString("email"));
					return user;
				}
			}
		}
	}
	
	public User createUser(String username, String email, String password) throws SQLException, IllegalCredentialsException, NoSuchAlgorithmException {
		String query = "SELECT id FROM users WHERE email = ?";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, email);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.isBeforeFirst()) 
					throw new IllegalCredentialsException("Email already in use");
			}
		}
		
		query = "SELECT id FROM users WHERE username = ?";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.isBeforeFirst()) 
					throw new IllegalCredentialsException("Username already in use");
			}
		}
		
		query = "INSERT into users (username, email, password)   VALUES(?, ?, ?)";
		PreparedStatement pstatement = null;
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(password.getBytes());
			String passwordHash = new String(messageDigest.digest());
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setString(2, email);
			pstatement.setString(3, passwordHash);
			pstatement.executeUpdate();
		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {

			}
		}
		
		User user = new User();
		query = "SELECT id, username, email FROM users WHERE username = ?";
		ResultSet result = null;
		pstatement = null;
		
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, username);
			result = pstatement.executeQuery();
			user.setId(result.getInt("id"));
			user.setUsername(result.getString("username"));
			user.setEmail(result.getString("email"));
		} catch (SQLException e) {
			throw new SQLException(e);

		} finally {
			try {
				if (result != null) {
					result.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		return user;
	}
	
}
