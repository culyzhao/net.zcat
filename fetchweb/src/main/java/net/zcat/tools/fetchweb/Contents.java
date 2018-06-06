package net.zcat.tools.fetchweb;

public class Contents {
	private String id = null;
	private String title = null;
	private String filename = "index.html";
	private String text = null;
	private String auther = null;
	private String time = null;
	
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
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
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
	
	public String toString() {
		return id + "," +
				title + "," +
				filename  + "," +
				auther  + "," +
				time  + "," +
				text ;
	}

}
