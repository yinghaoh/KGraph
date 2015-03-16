package com.commvault.backend.model;

import java.util.List;

/*
 * Model class defining the content to be stored for a set of related articles
 * 
 * 
 * @author aswin
 */

public class RelatedArticles {
	private String url;
	private List<Article> relatedarticles;
	private List<Float> scores;
	private List<Boolean> typelist;
	private List<String> commontopics;
	
	public List<String> getCommontopics() {
		return commontopics;
	}
	
	public List<Float> getScores() {
		return scores;
	}
	
	public List<Article> getRelatedarticles() {
		return relatedarticles;
	}
	
	public List<Boolean> getTypelist() {
		return typelist;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setCommontopics(List<String> commontopics) {
		this.commontopics = commontopics;
	}
	
	public void setScores(List<Float> scores) {
		this.scores = scores;
	}
	
	public void setRelatedarticles(List<Article> relatedarticles) {
		this.relatedarticles = relatedarticles;
	}
	
	public void setTypelist(List<Boolean> typelist) {
		this.typelist = typelist;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
}
