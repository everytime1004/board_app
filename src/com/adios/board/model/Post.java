package com.adios.board.model;

public class Post {
	private int id;
	private String title;
	private String category;
	private String description;
	private String updated_time;
	private String author;

	public Post(int id, String title, String category, String description, String updated_time, String author) {
		this.id = id;
		this.title = title;
		this.category = category;
		this.description = description;
		this.updated_time = updated_time;
		this.author = author;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCategory() {
		return category;
	}

	public void setCompleted(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getUpdated_time(){
		return updated_time;
	}
	
	public void setUpdated_time(String updated_time){
		this.updated_time = updated_time;
	}
	
	public String getAuthor(){
		return author;
	}
	
	public void setAuthor(String author){
		this.author = author;
	}
}
