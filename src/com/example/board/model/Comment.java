package com.example.board.model;

public class Comment {
	private String author;
	private String contents;
	private String updated_time;

	public Comment(String author, String contents, String updated_time) {
		this.author = author;
		this.contents = contents;
		this.updated_time = updated_time;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
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
}
