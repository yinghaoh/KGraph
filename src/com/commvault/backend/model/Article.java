package com.commvault.backend.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/*
 * Model class defining the content to be stored for an article
 * 
 * 
 * @author aswin 
 */

@XmlRootElement
public class Article {
	private String title;
	private String url;
	private List<String> h2;
	private List<String> h3;
	private List<String> h4;
	private List<String> h5;
	private List<String> h6;
	private List<String> outlinkedArticles;
	private List<String> outlinkedRelatedLinks;
	private List<String> imgLinks;
	private List<String> externalLinks;
	private List<String> boostHeads;
	private String content;
	
	/*
	 * keywords, boosted terms
	 */
	
	public Article() {
		
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setBoostHeads(List<String> boostHeads) {
		this.boostHeads = boostHeads;
	}
	
	public List<String> getBoostHeads() {
		return boostHeads;
	}
	
	public void setH2(List<String> h2) {
		this.h2 = h2;
	}
	
	public void setH3(List<String> h3) {
		this.h3 = h3;
	}
	
	public void setH4(List<String> h4) {
		this.h4 = h4;
	}
	
	public void setH5(List<String> h5) {
		this.h5 = h5;
	}
	
	public void setH6(List<String> h6) {
		this.h6 = h6;
	}
	
	public void setImgLinks(List<String> imgLinks) {
		this.imgLinks = imgLinks;
	}
	
	public void setOutlinkedArticles(List<String> outlinkedArticles) {
		this.outlinkedArticles = outlinkedArticles;
	}
	
	public void setOutlinkedRelatedLinks(List<String> outlinkedRelatedLinks) {
		this.outlinkedRelatedLinks = outlinkedRelatedLinks;
	}
	
	public void setExternalLinks(List<String> externalLinks) {
		this.externalLinks = externalLinks;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public List<String> getH2() {
		return h2;
	}
	
	public List<String> getH3() {
		return h3;
	}
	
	public List<String> getH4() {
		return h4;
	}
	
	public List<String> getH5() {
		return h5;
	}
	
	public List<String> getH6() {
		return h6;
	}
	
	public List<String> getImgLinks() {
		return imgLinks;
	}
	
	public List<String> getOutlinkedArticles() {
		return outlinkedArticles;
	}
	
	public List<String> getOutlinkedRelatedLinks() {
		return outlinkedRelatedLinks;
	}
	
	public List<String> getExternalLinks() {
		return externalLinks;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getUrl() {
		return url;
	}
} 

