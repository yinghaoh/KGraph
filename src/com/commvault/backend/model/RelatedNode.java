package com.commvault.backend.model;

import java.util.Collection;

import org.neo4j.graphdb.Node;

/*
 * Model class defining the content to be stored in a related node
 * The related node is sortable.
 * 
 * 
 * @author aswin
 */
public class RelatedNode implements Comparable<RelatedNode> {
	boolean type;
	float score;
	Node node;
	Collection<String> commonTopics;
	
	public Node getNode() {
		return node;
	}
	
	public float getScore() {
		return score;
	}
	
	public boolean isType() {
		return type;
	}
	
	public Collection<String> getCommonTopics() {
		return commonTopics;
	}
	
	public void setNode(Node node) {
		this.node = node;
	}
	
	public void setScore(float score) {
		this.score = score;
	}
	
	public void setType(boolean type) {
		this.type = type;
	}
	
	public void setCommonTopics(Collection<String> commonTopics) {
		this.commonTopics = commonTopics;
	}

	@Override
	public int compareTo(RelatedNode rn) {
		if (score > rn.getScore())
			return -1;
		else if (score < rn.getScore())
			return 1;
		else
			return 0;
	}
	
	
}
