package com.commvault.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.restlet.data.MediaType;
import org.restlet.engine.header.Header;
import org.restlet.ext.gson.GsonRepresentation;
import org.restlet.ext.json.JsonpRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;

import com.commvault.backend.graphops.GraphOperations;
import com.commvault.backend.helper.GraphUtility;
import com.commvault.backend.helper.ResourceUtility;
import com.commvault.backend.helper.Utility;
import com.commvault.backend.model.ConnectedSubArticle;
import com.commvault.backend.model.GenericList;
import com.commvault.backend.model.SubArticle;
import com.google.gson.Gson;

/*
 * Class defining the server resource to provide with
 * the connected sub-articles feature.
 * 
 * @author aswin
 */
public class ConnectedSubArticlesResource extends ServerResource {
	Map<String,Object> propMap;
	GraphDatabaseService db;
	Label articlelabel;
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
	 * Serves the connected  sub-articles request
	 */
	@Post("json:javascript")
	public Representation connectedSubArticles(Representation entity) {
		
		long start, end;
		start = System.currentTimeMillis();
		
		Series<Header> headers = ResourceUtility.getMessageHeaders(getResponse());
		headers.add("Access-Control-Allow-Headers", "Content-Type");
		headers.add("Access-Control-Allow-Origin", "*");
		
		db = (GraphDatabaseService) propMap.get("graphdb");
		articlelabel = (Label) propMap.get("articlelabel");
		
		List<Node> subarticleNodesofH2, subarticleNodesofH3;
	
		// Obtain the callback
		String callback = getQueryValue("callback");
		if(callback == null || callback.isEmpty())  {
			logger.info("NOT a Remote call");
			remote = false;
		} else {
			logger.info("Remote call");
		}
			
    	
		if ( db != null ) {
    		try {
    			if(entity.getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {
    				Gson gson = new Gson();
    				GenericList urls = gson.fromJson(entity.getText(), GenericList.class);
    				if (urls == null) {
    					logger.warn("No request URLs");
    					if (remote)
    		    			return new JsonpRepresentation(callback, getStatus(), new GsonRepresentation<String>(Utility.REQUEST_ERROR));
    		    		else
    		    			return new GsonRepresentation<String>(Utility.REQUEST_ERROR);
    				}
					List<ConnectedSubArticle> connectedSubArticles = new ArrayList<ConnectedSubArticle>();
					ConnectedSubArticle csa;
					for(String url:urls.getList()) {
    					// for every url, the set of connected urls need to be obtained
    					csa = new ConnectedSubArticle();
    					csa.setUrl(url);
    					try(Transaction tx = db.beginTx()) {
    						// obtain the connected sub articles to this url (h2, h3)
    						subarticleNodesofH2 = GraphOperations.getConnectedSubArticles(db, articlelabel, url, 2);
    						constructList(csa, subarticleNodesofH2, urls.getConcise(), 2);
    						subarticleNodesofH3 = GraphOperations.getConnectedSubArticles(db, articlelabel, url, 3);
    						constructList(csa, subarticleNodesofH3, urls.getConcise(), 3);
    						tx.success();
    					}
    					connectedSubArticles.add(csa);
    				}
					end = System.currentTimeMillis();
					logger.info("Request completed in: "+(end-start)+" ms");
					
					if (remote)
						return new JsonpRepresentation(callback, getStatus(), new GsonRepresentation<List<ConnectedSubArticle>>(connectedSubArticles));
					else
						return new GsonRepresentation<List<ConnectedSubArticle>>(connectedSubArticles);
    			}
    		} catch (IOException e) {
    			logger.error(e.getClass().getName(), e);
    		}
    		
    		logger.info("Parsing error");
    		
    		if (remote)
    			return new JsonpRepresentation(callback, getStatus(), new GsonRepresentation<String>(Utility.PARSING_ERROR));
    		else
    			return new GsonRepresentation<String>(Utility.PARSING_ERROR);
    	}
    	else {
    		
    		logger.info("DB error");
    		
    		if (remote)
    			return new JsonpRepresentation(callback, getStatus(), new GsonRepresentation<String>(Utility.DB_ERROR));
    		else
    			return new GsonRepresentation<String>(Utility.DB_ERROR);
    	}
	}
	
	/*
	 * Constructs the connected set of articles
	 * @param csa 				ConnectedSubArticle instance that gets added with the sub-articles
	 * @param subarticleNodes	list of sub-article nodes to add
	 * @param concise			parameter to determine a concise or verbose list
	 * @param heading			the type of heading 2/3 corresponding to H2/H3
	 */
	public void constructList(ConnectedSubArticle csa, List<Node> subarticleNodes, String concise, int heading) {
		SubArticle sub;
		List<SubArticle> subarticles;
		
		if (subarticleNodes != null && !subarticleNodes.isEmpty()) {
			subarticles = new ArrayList<SubArticle>();
			for(Node n: subarticleNodes) {
				sub = new SubArticle();
				if(concise.equals("true")) {
					// get the title and url alone and assign it to the article
					String title = GraphOperations.getProperty(n,GraphUtility.TITLE);
					sub.setTitle(title);
					String conurl = GraphOperations.getProperty(n,GraphUtility.URL);
					sub.setUrl(conurl);
					String bold = GraphOperations.getProperty(n, GraphUtility.BOLDWORDS);
					if (bold != null && !bold.isEmpty())
						sub.setBoldWords(Arrays.asList(bold.split(",")));
					sub.setType(heading);
					sub.setType(heading);
				}
				else {
					// perform the mapping b/w the node and article
					GraphOperations.nodeToArticleMapper(sub, n);
					String bold = GraphOperations.getProperty(n, GraphUtility.BOLDWORDS);
					if (bold != null && !bold.isEmpty())
						sub.setBoldWords(Arrays.asList(bold.split(",")));
					sub.setType(heading);
				}
				
				subarticles.add(sub);
			}
			if(csa.getConnectedSubArticles() == null)
				csa.setConnectedSubArticles(subarticles);
			else
				csa.addConnectedSubArticles(subarticles);
		}
	}
	
	/*
	 * Serves the options request
	 */
	@Options
	public void doOptions(Representation entity) {
		Series<Header> headers = ResourceUtility.getMessageHeaders(getResponse());
		headers.add("Access-Control-Allow-Headers", "Content-Type");
		headers.add("Access-Control-Allow-Methods", "POST,OPTIONS"); 
		headers.add("Access-Control-Allow-Origin", "*");
	    //responseHeaders.add("Access-Control-Allow-Credentials", "false"); 
	    //responseHeaders.add("Access-Control-Max-Age", "60");
	} 	
}
