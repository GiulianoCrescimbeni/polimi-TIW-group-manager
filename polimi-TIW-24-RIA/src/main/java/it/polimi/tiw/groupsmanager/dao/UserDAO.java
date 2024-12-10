package it.polimi.tiw.groupsmanager.dao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.groupsmanager.beans.Group;
import it.polimi.tiw.groupsmanager.beans.User;
import it.polimi.tiw.groupsmanager.exceptions.IllegalCredentialsException;

public class UserDAO {
	private Connection con;
	
	public UserDAO(Connection connection) {
		this.con = connection;
	}
	
	public User findUserById(int userId) throws SQLException {
	    User user = null;
	    String query = "SELECT id, username, name, surname, email FROM users WHERE id = ?";

	    ResultSet result = null;
	    PreparedStatement pstatement = null;

	    try {
	        pstatement = con.prepareStatement(query);
	        pstatement.setInt(1, userId);
	        result = pstatement.executeQuery();
	        if (result.next()) {
	            user = new User();
	            user.setId(result.getInt("id"));
	            user.setUsername(result.getString("username"));
	            user.setName(result.getString("name"));
	            user.setSurname(result.getString("surname"));
	            user.setEmail(result.getString("email"));
	        }
	    } catch (SQLException e) {
	        throw new SQLException(e);
	    } finally {
	        if (result != null) {
	            try {
	                result.close();
	            } catch (SQLException e1) {
	                e1.printStackTrace();
	            }
	        }
	        if (pstatement != null) {
	            try {
	                pstatement.close();
	            } catch (SQLException e2) {
	                e2.printStackTrace();
	            }
	        }
	    }
	    return user;
	}
	
	public User findUserByUsername(String username) throws SQLException {
	    User user = null;
	    String query = "SELECT id, username, name, surname, email FROM users WHERE username = ?";

	    ResultSet result = null;
	    PreparedStatement pstatement = null;

	    try {
	        pstatement = con.prepareStatement(query);
	        pstatement.setString(1, username);
	        result = pstatement.executeQuery();
	        if (result.next()) {
	            user = new User();
	            user.setId(result.getInt("id"));
	            user.setUsername(result.getString("username"));
	            user.setName(result.getString("name"));
	            user.setSurname(result.getString("surname"));
	            user.setEmail(result.getString("email"));
	        }
	    } catch (SQLException e) {
	        throw new SQLException(e);
	    } finally {
	        if (result != null) {
	            try {
	                result.close();
	            } catch (SQLException e1) {
	                e1.printStackTrace();
	            }
	        }
	        if (pstatement != null) {
	            try {
	                pstatement.close();
	            } catch (SQLException e2) {
	                e2.printStackTrace();
	            }
	        }
	    }
	    return user;
	}
	
	public User checkCredentials(String email, String password) throws SQLException, IllegalCredentialsException, NoSuchAlgorithmException {
		String query = "SELECT  id, username, name, surname, email FROM users WHERE email = ? AND password =?";
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
					user.setName(result.getString("name"));
		            user.setSurname(result.getString("surname"));
					user.setEmail(result.getString("email"));
					return user;
				}
			}
		}
	}
	
	public User createUser(String username, String name, String surname, String email, String password) throws SQLException, IllegalCredentialsException, NoSuchAlgorithmException {
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
		
		query = "INSERT into users (username, name, surname, email, password) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement pstatement = null;
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(password.getBytes());
			String passwordHash = new String(messageDigest.digest());
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setString(2, name);
			pstatement.setString(3, surname);
			pstatement.setString(4, email);
			pstatement.setString(5, passwordHash);
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
		query = "SELECT id, username, name, surname, email FROM users WHERE username = ?";
		ResultSet result = null;
		pstatement = null;
		
		try {
			pstatement = con.prepareStatement(query);
		    pstatement.setString(1, username);
		    result = pstatement.executeQuery();
		        
		    if (result.next()) {
		    	user.setId(result.getInt("id"));
		        user.setUsername(result.getString("username"));
		        user.setName(result.getString("name"));
		        user.setSurname(result.getString("surname"));
		        user.setEmail(result.getString("email"));
		    } else {
		        throw new SQLException("User not found after creation");
		    }
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
	
	public List<User> findAllUsers() throws SQLException {
		List<User> users = new ArrayList<User>();
		String query = "SELECT * FROM users";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			result = pstatement.executeQuery();
			while (result.next()) {
				User user = new User();
				user.setId(result.getInt("id"));
				user.setUsername(result.getString("username"));
				user.setName(result.getString("name"));
	            user.setSurname(result.getString("surname"));
				user.setEmail(result.getString("email"));
				users.add(user);
			}
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
		return users;
	}
	
}
