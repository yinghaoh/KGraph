package com.commvault.backend.model;

import java.util.List;

/*
 * Model class defining the content to be stored for an sub-article
 * 
 * 
 * @author aswin 
 */

public class SubArticle extends Article {
	
	private int type;
	private List<String> boldWords;
	private String containingArticle;
	private String containingArticleUrl;
	/*
	 * italics, All CAPS, notes, warning, code snippets, table, lists
	 * 
	 */
	 
	public SubArticle() {
		super();
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	
	public int getType() {
		return type;
	}
	
	public void setBoldWords(List<String> boldWords) {
		this.boldWords = boldWords;
	}
	
	public List<String> getBoldWords() {
		return boldWords;
	}
	
	public void setContainingArticle(String containingArticle) {
		this.containingArticle = containingArticle;
	}
	
	public String getContainingArticle() {
		return containingArticle;
	}
	
	public void setContainingArticleUrl(String containingArticleUrl) {
		this.containingArticleUrl = containingArticleUrl;
	}
	
	public String getContainingArticleUrl() {
		return containingArticleUrl;
	}
}
