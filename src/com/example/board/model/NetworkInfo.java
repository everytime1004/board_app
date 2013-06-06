package com.example.board.model;

public class NetworkInfo {
//	 public static final String IP = "http://192.168.0.74:3000";
	public static final String IP = "http://115.68.27.117";
	// public static final String IP = "http://boardgeneration.herokuapp.com";

	public static final String PROJECT_ID = "43944975330";

	public static final String GCM_URL = IP + "/api/v1/gcms.json";

	public static final String LOGIN_API_ENDPOINT_URL = IP
			+ "/api/v1/sessions.json";

	public static final String REGISTER_API_ENDPOINT_URL = IP
			+ "/api/v1/registrations";

	public static final String CREATE_TASK_ENDPOINT_URL = IP
			+ "/api/v1/posts.json";
	
	public static final String CREATE_COMMENT_ENDPOINT_URL = IP
			+ "/api/v1/comments.json";

	public static final String TASKS_URL = IP + "/api/v1/posts.json";
}