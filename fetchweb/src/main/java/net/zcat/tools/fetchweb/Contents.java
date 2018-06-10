package net.zcat.tools.fetchweb;

import java.util.ArrayList;

public class Contents {
	private String id = null;
	private String title = null;
	private String text = null;
	private String author = null;
	private String time = null;
	private boolean fetched = false;
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
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
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
	
	public boolean isFetched() {
		return fetched;
	}
	public void setFetched(boolean fetched) {
		this.fetched = fetched;
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(id).append(",");
		sb.append(title).append(",");
		sb.append(author).append(",");
		sb.append(time).append(",");
		sb.append(fetched).append(",");
		sb.append(text.replaceAll("\n", "")).append(",[");
		
		for (Comment c : comments) {
			sb.append("{");
			sb.append(c.getAuthor()).append(",");
			sb.append(c.getTime()).append(",");
			sb.append(c.getText()).append("},");
		}
		sb.append("]");
		
		
		return sb.toString();
	}

}
