package it.polimi.tiw.groupsmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import it.polimi.tiw.groupsmanager.beans.Group;

public class GroupDAO {
	private Connection con;
	
	public GroupDAO(Connection connection) {
		this.con = connection;
	}
	
	public Group findGroupById(int groupId) throws SQLException {
		Group group = new Group();
		String query = "SELECT * FROM groups WHERE id = ?";
		
		ResultSet result = null;
		PreparedStatement pstatement = null;
		
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, groupId);
			result = pstatement.executeQuery();
			group.setId(result.getInt("id"));
			group.setTitle(result.getString("title"));
			group.setCreationDate(result.getString("creation_date"));
			group.setActivityDuration(result.getInt("activity_duration"));
			group.setMinParticipants(result.getInt("min_participants"));
			group.setMaxParticipants(result.getInt("max_participants"));
			group.setActive(result.getBoolean("active"));
			group.setCreatorId(result.getInt("creator_id"));
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
		return group;
	}
	
	public List<Group> findGroupsByCreatorId(int creatorId) throws SQLException {
		List<Group> groups = new ArrayList<Group>();
		String query = "SELECT * FROM groups WHERE creator_id = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, creatorId);
			result = pstatement.executeQuery();
			while (result.next()) {
				Group group = new Group();
				group.setId(result.getInt("id"));
				group.setTitle(result.getString("title"));
				group.setCreationDate(result.getString("creation_date"));
				group.setActivityDuration(result.getInt("activity_duration"));
				group.setMinParticipants(result.getInt("min_participants"));
				group.setMaxParticipants(result.getInt("max_participants"));
				group.setActive(result.getBoolean("active"));
				group.setCreatorId(result.getInt("creator_id"));
				groups.add(group);
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

		return groups;
	}
	
	public List<Group> findAllActiveGroups() throws SQLException {
		List<Group> groups = new ArrayList<Group>();
		String query = "SELECT * FROM groups WHERE active = 1";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			result = pstatement.executeQuery();
			while (result.next()) {
				Group group = new Group();
				group.setId(result.getInt("id"));
				group.setTitle(result.getString("title"));
				group.setCreationDate(result.getString("creation_date"));
				group.setActivityDuration(result.getInt("activity_duration"));
				group.setMinParticipants(result.getInt("min_participants"));
				group.setMaxParticipants(result.getInt("max_participants"));
				group.setActive(result.getBoolean("active"));
				group.setCreatorId(result.getInt("creator_id"));
				groups.add(group);
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
		return groups;
	}
	
	public List<Group> getGroupsWhereInvited(int userId) throws SQLException {
		List<Group> groups = new ArrayList<Group>();
		String query = "SELECT g.* FROM groups g JOIN groups_invitation gi ON g.id = gi.id_group WHERE gi.id_user = ? AND g.active = 1;";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, userId);
			result = pstatement.executeQuery();
			while (result.next()) {
				Group group = new Group();
				group.setId(result.getInt("id"));
				group.setTitle(result.getString("title"));
				group.setCreationDate(result.getString("creation_date"));
				group.setActivityDuration(result.getInt("activity_duration"));
				group.setMinParticipants(result.getInt("min_participants"));
				group.setMaxParticipants(result.getInt("max_participants"));
				group.setActive(result.getBoolean("active"));
				group.setCreatorId(result.getInt("creator_id"));
				groups.add(group);
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
		return groups;
	}
	
	public int createGroup(String title, int activityDuration, int minParticipants, int maxParticipants, int creatorId) throws SQLException {
		String query = "INSERT into groups (title, activity_duration, minParticipants, maxParticipants, creatorId)   VALUES(?, ?, ?, ?, ?)";
		int result = 0;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, title);
			pstatement.setInt(2, activityDuration);
			pstatement.setInt(3, minParticipants);
			pstatement.setInt(4, maxParticipants);
			pstatement.setInt(5, creatorId);
			result = pstatement.executeUpdate();
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
		return result;
	}
	
	public int inviteInGroup(int idUser, int idGroup) throws SQLException {
		String query = "INSERT into groups_invitations (id_user, id_group)   VALUES(?, ?)";
		int result = 0;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, idUser);
			pstatement.setInt(2, idGroup);
			result = pstatement.executeUpdate();
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
		return result;
	}

}
