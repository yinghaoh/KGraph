package com.commvault.resources;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.commvault.backend.graphops.GraphOperations;
import com.commvault.backend.helper.GraphUtility;


public class RandomTitleResource extends ServerResource {
	
	Map<String,Object> propMap;
	GraphDatabaseService db;
	
	@Override
	protected void doInit() throws ResourceException {
		propMap = getContext().getAttributes();
		super.doInit();
	}
	
    @Get("txt")
    public String randomTitle() {

    	db = (GraphDatabaseService) propMap.get("graphdb");
    	if ( db != null )
    		try(Transaction tx = db.beginTx()) {
    			Node n = GraphOperations.getRandomNode(db);
    			String random = (String) n.getProperty(GraphUtility.TITLE);
    			tx.success();
    			return random;
    		}
    		
    	else
    		return "failure";
    }
    
    
    
}
   
