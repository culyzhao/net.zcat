package net.zcat.tools.fetchweb;

import java.util.ArrayList;

public class Contents {
	private String id = null;
	private String title = null;
	private String text = null;
	private String auther = null;
	private String time = null;
	private ArrayList<Comment> comments = new ArrayList<Comment>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getAuther() {
		return auther;
	}
	public void setAuther(String auther) {
		this.auther = auther;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
	public ArrayList<Comment> getComments() {
		return comments;
	}
	public void setComments(ArrayList<Comment> comments) {
		this.comments = comments;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(id).append(",");
		sb.append(title).append(",");
		sb.append(time).append(",");
		sb.append(text.replaceAll("\n", "")).append(",[");
		
		for (Comment c : comments) {
			sb.append("{");
			sb.append(c.getAuther()).append(",");
			sb.append(c.getTime()).append(",");
			sb.append(c.getText()).append("},");
		}
		sb.append("]");
		
		
		return sb.toString();
	}

}
