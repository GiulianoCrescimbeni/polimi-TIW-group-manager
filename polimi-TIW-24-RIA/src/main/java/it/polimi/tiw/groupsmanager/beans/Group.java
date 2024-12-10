package it.polimi.tiw.groupsmanager.beans;

public class Group {
	private int id;
	private String title;
	private String creationDate;
	private int activityDuration;
	private int minParticipants;
	private int maxParticipants;
	private int creatorId;
	
	public Group() {}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getCreationDate() {
		return this.creationDate;
	}
	
	public void setCreationDate(String date) {
		this.creationDate = date;
	}
	
	public int getActivityDuration() {
		return this.activityDuration;
	}
	
	public void setActivityDuration(int duration) {
		this.activityDuration = duration;
	}
	
	public int getMinParticipants() {
		return this.minParticipants;
	}
	
	public void setMinParticipants(int minParticipants) {
		this.minParticipants = minParticipants;
	}
	
	public int getMaxParticipants() {
		return this.maxParticipants;
	}
	
	public void setMaxParticipants(int maxParticipants) {
		this.maxParticipants = maxParticipants;
	}
	
	public int getCreatorId() {
		return this.creatorId;
	}
	
	public void setCreatorId(int creatorId) {
		this.creatorId = creatorId;
	}
}
