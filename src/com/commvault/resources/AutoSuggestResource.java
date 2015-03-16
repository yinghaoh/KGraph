package com.commvault.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
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

/*
 * Class defining the server resource to provide with
 * the auto-suggest feature.
 * 
 * @author aswin
 */
public class AutoSuggestResource extends ServerResource {
	Map<String,Object> propMap;
	GraphDatabaseService db;
	Label label;
	boolean remote;
	
	private Logger logger;
	
	/*
	 * Sets the property map
	 * (non-Javadoc)
	 * @see org.restlet.resource.Resource#doInit()
	 */
	@Override
	protected void doInit() throws ResourceException {
		propMap = getContext().getAttributes();
		remote = true;
		
		logger = Logger.getLogger(this.getClass().getName());
		
		super.doInit();
	}
	
	/*
	 * Serves the auto suggest request
	 */
	@Get("json:javascript")
    public Representation searchArticles() {
		
		long start, end;
		start = System.currentTimeMillis();
		
		Series<Header> headers = ResourceUtility.getMessageHeaders(getResponse());
		headers.add("Access-Control-Allow-Headers", "Content-Type");
		headers.add("Access-Control-Allow-Origin", "*");
		
		db = (GraphDatabaseService) propMap.get("graphdb");
    	List<Article> articleList = new ArrayList<Article>();
    	Article a;
		Node n;
		// Obtain the callback
		String callback = getQueryValue("callback");
		if(callback == null || callback.isEmpty()) {
			logger.info("NOT a Remote call");
			remote = false;
		} else {
			logger.info("Remote call");
		}
			
		// Obtain the keyword to query
    	String partialq  = getQueryValue("q");
    	// Obtain if the search is generic/ article/ sub article
    	String type  = getQueryValue("t");
    	if (partialq == null || partialq.isEmpty()) {
    		logger.warn("Query is empty");
    		if (remote)
    			return new JsonpRepresentation(callback, getStatus(), new GsonRepresentation<String>(Utility.EMPTY_QUERY));
    		else
    			return new GsonRepresentation<String>(Utility.EMPTY_QUERY);
    	}
    	if (type == null || type.isEmpty()) {
    		type = GraphUtility.GENERICQUERY;
    	}
    	if ( db != null ) {
    		logger.info("Request parameters: query = "+partialq+" type = "+type);
    		try(Transaction tx = db.beginTx()) {
    			ResourceIterator<Node> nodes = GraphOperations.autosuggestByTitle(db, type, partialq);
    			if(nodes == null) {
    				logger.info("No response");
    				if (remote)
    					return new JsonpRepresentation(callback, getStatus(), new GsonRepresentation<String>(Utility.NOT_FOUND));
    				else
    					return new GsonRepresentation<String>(Utility.NOT_FOUND);
    			}
    			while(nodes.hasNext()) {
    				n = nodes.next();
    				a = new Article();
    				GraphOperations.nodeToArticleMapper(a, n);
    				//a.setSummary(Operations.getSummary(a.getContent()));
    				articleList.add(a);
    			}
    			tx.success();
    			end = System.currentTimeMillis();
    			logger.info("Request completed in: "+(end-start)+" ms");
    			
    			if (remote)
    				return new JsonpRepresentation(callback, getStatus(), new GsonRepresentation<List<Article>>(articleList)) ;
    			else
    				return new GsonRepresentation<List<Article>>(articleList);
    		}
    	}
    	else {
    		logger.info("DB error");
    		if (remote)
    			return new JsonpRepresentation(callback, getStatus(),new GsonRepresentation<String>(Utility.DB_ERROR));
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
