package com.commvault.backend.model;

import java.util.List;

/*
 * Model class defining the content to be stored for an summary node
 * 
 * 
 * @author aswin 
 */

public class SummaryNode {
	String url;
	List<String> cvalue;
	List<String> rake;
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setCvalue(List<String> cvalue) {
		this.cvalue = cvalue;
	}
	
	public void setRake(List<String> rake) {
		this.rake = rake;
	}
	
	public String getUrl() {
		return url;
	}
	
	public List<String> getCvalue() {
		return cvalue;
	}
	
	public List<String> getRake() {
		return rake;
	}
}
