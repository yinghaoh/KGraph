package com.commvault.backend.model;

import java.util.List;

/*
 * Model class defining the content to be stored for a connected sub-article
 * 
 * 
 * @author aswin 
 */

public class ConnectedSubArticle {
	private String url;
	private List<SubArticle> connectedsubarticles;
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setConnectedSubArticles(List<SubArticle> connectedsubarticles) {
		this.connectedsubarticles = connectedsubarticles;
	}
	
	public String getUrl() {
		return url;
	}
	
	public List<SubArticle> getConnectedSubArticles() {
		return connectedsubarticles;
	}
	
	public void addConnectedSubArticles(List<SubArticle> connectedsubarticles) {
		this.connectedsubarticles.addAll(connectedsubarticles);
	}
}
