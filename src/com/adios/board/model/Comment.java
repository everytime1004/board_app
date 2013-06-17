package com.adios.board.model;

public class Comment {
	private int id;
	private String author;
	private String contents;
	private String updated_time;
	private boolean isOwner;

	public Comment(int id, String author, String contents, String updated_time, boolean isOwner) {
		this.id = id;
		this.author = author;
		this.contents = contents;
		this.updated_time = updated_time;
		this.isOwner = isOwner;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
	public int getId(){
		return id;
	}
	
	public void setId(int id){
		this.id = id;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}
	
	public String getUpdated_time(){
		return updated_time;
	}
	
	public void setUpdated_time(String updated_time){
		this.updated_time = updated_time;
	}
	
	public boolean getIsOwner(){
		return isOwner;
	}
	
	public void setIsOwner(boolean isOwner){
		this.isOwner = isOwner;
	}
}
