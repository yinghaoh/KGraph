package com.commvault.backend.model;

import java.util.List;

/*
 * Model class defining the content to be stored for a connected article
 * 
 * 
 * @author aswin 
 */

public class ConnectedArticle {
	private String url;
	private List<Article> connectedarticles;
	private List<Float> scores;
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setConnectedarticles(List<Article> connectedarticles) {
		this.connectedarticles = connectedarticles;
	}
	
	public void setScores(List<Float> scores) {
		this.scores = scores;
	}
	
	public String getUrl() {
		return url;
	}
	
	public List<Article> getConnectedarticles() {
		return connectedarticles;
	}
	
	public List<Float> getScores() {
		return scores;
	}
}
