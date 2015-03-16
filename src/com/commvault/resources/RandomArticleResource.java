package com.commvault.resources;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.restlet.engine.header.Header;
import org.restlet.ext.gson.GsonRepresentation;
import org.restlet.ext.json.JsonpRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;

import com.commvault.backend.graphops.GraphOperations;
import com.commvault.backend.helper.GraphUtility;
import com.commvault.backend.helper.ResourceUtility;
import com.commvault.backend.helper.Utility;
import com.commvault.backend.model.Article;

public class RandomArticleResource extends ServerResource{
	
	Map<String,Object> propMap;
	GraphDatabaseService db;
	boolean remote;
	
	@Override
	protected void doInit() throws ResourceException {
		propMap = getContext().getAttributes();
		remote = true;
		super.doInit();
	}
	
	@Get("json:javascript")
    public Representation randomArticle() {
		
		Series<Header> headers = ResourceUtility.getMessageHeaders(getResponse());
		headers.add("Access-Control-Allow-Headers", "Content-Type");
		headers.add("Access-Control-Allow-Origin", "*");

    	db = (GraphDatabaseService) propMap.get("graphdb");
    	
    	// Obtain the callback
		String callback = getQueryValue("callback");
		if(callback == null || callback.isEmpty()) 
			remote = false;
    	
    	if ( db != null )
    		try(Transaction tx = db.beginTx()) {
    			Node n = GraphOperations.getRandomNode(db);
    			Article a = new Article();
    			a.setTitle((String) n.getProperty(GraphUtility.TITLE));
    			tx.success();
    			if (remote)
    				return new JsonpRepresentation(callback, getStatus(), new GsonRepresentation<Article>(a));
    			else
    				return new GsonRepresentation<Article>(a);
    		}
    		
    	else {
    		if (remote)
    			return new JsonpRepresentation(callback, getStatus(), new GsonRepresentation<String>(Utility.DB_ERROR));
    		else
    			return new GsonRepresentation<String>(Utility.DB_ERROR);
    	}
    		
    }
	
	/*
	 * Serves the options request
	 */
	@Options
	public void doOptions(Representation entity) {
		Series<Header> headers = ResourceUtility.getMessageHeaders(getResponse());
		headers.add("Access-Control-Allow-Headers", "Content-Type");
		headers.add("Access-Control-Allow-Methods", "GET,OPTIONS"); 
		headers.add("Access-Control-Allow-Origin", "*");
	    //responseHeaders.add("Access-Control-Allow-Credentials", "false"); 
	    //responseHeaders.add("Access-Control-Max-Age", "60");
	} 	
	
}
