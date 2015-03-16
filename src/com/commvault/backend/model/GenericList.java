package com.commvault.backend.model;

import java.util.List;

/*
 * Model class defining the request format
 * 
 * 
 * @author aswin
 */

public class GenericList {
	String concise;
	List<String> list;
	
	public List<String> getList() {
		return list;
	}
	
	public void setList(List<String> list) {
		this.list = list;
	}
	
	public String getConcise() {
		return concise;
	}
	
	public void setConcise(String concise) {
		this.concise = concise;
	}
	
}
