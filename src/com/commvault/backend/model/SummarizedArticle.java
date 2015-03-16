package com.commvault.backend.model;

/*
 * Model class defining the content to be stored for a summarized article
 * 
 * 
 * @author aswin 
 */
public class SummarizedArticle extends Article{
	String summary;
	
	public String getSummary() {
		return summary;
	}
	
	public void setSummary(String summary) {
		this.summary = summary;
	}
}
