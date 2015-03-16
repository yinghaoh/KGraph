package com.commvault.backend.model;

import org.neo4j.graphdb.Node;

/*
 * Model class defining the content to be stored for a connected node.
 * The instances are sortable.
 * 
 * 
 * @author aswin 
 */

public class ConnectedNode implements Comparable<ConnectedNode>{
	private String url;
	private Node node;
	private float score;
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setNode(Node node) {
		this.node = node;
	}
	
	public void setScore(float score) {
		this.score = score;
	}
	
	public String getUrl() {
		return url;
	}
	
	public Node getNode() {
		return node;
	}
	
	public float getScore() {
		return score;
	}
	
	@Override
	public int compareTo(ConnectedNode cn) {
		if (score > cn.getScore())
			return -1;
		else if (score < cn.getScore())
			return 1;
		else
			return 0;
	}
}
