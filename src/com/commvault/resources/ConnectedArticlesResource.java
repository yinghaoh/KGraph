package com.commvault.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
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
import com.commvault.backend.model.Article;
import com.commvault.backend.model.ConnectedArticle;
import com.commvault.backend.model.ConnectedNode;
import com.commvault.backend.model.GenericList;
import com.google.gson.Gson;

/*
 * Class defining the server resource to provide with
 * the connected articles feature.
 * 
 * @author aswin
 */
public class ConnectedArticlesResource extends ServerResource {
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
	 * Serves the connected articles request
	 */
	@Post("json:javascript")
	public Representation connectedArticles(Representation entity) {
		
		long start, end;
		start = System.currentTimeMillis();
		
		Series<Header> headers = ResourceUtility.getMessageHeaders(getResponse());
		headers.add("Access-Control-Allow-Headers", "Content-Type");
		headers.add("Access-Control-Allow-Origin", "*");
		
		db = (GraphDatabaseService) propMap.get("graphdb");
		articlelabel = (Label) propMap.get("articlelabel");
		
		List<ConnectedNode> articleNodes;
		
		// Obtain the callback
		String callback = getQueryValue("callback");
		if(callback == null || callback.isEmpty()) {
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
    				List<ConnectedArticle> connectedArticles = new ArrayList<ConnectedArticle>();
					ConnectedArticle ca;
					for(String url:urls.getList()) {
    					// for every url, the set of connected urls need to be obtained
    					ca = new ConnectedArticle();
    					ca.setUrl(url);
    					try(Transaction tx = db.beginTx()) {
    						// obtain the connected articles to this url
    						articleNodes = GraphOperations.getConnectedArticles(db, articlelabel, url);
    						constructList(ca, articleNodes, urls.getConcise());
    						tx.success();
    					}
    					connectedArticles.add(ca);
    				}
					end = System.currentTimeMillis();
					logger.info("Request completed in: "+(end-start)+" ms");
					
					if (remote)
						return new JsonpRepresentation(callback, getStatus(), new GsonRepresentation<List<ConnectedArticle>>(connectedArticles));
					else
						return new GsonRepresentation<List<ConnectedArticle>>(connectedArticles);
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
	 * @param ca 			ConnectedArticle instance that gets added with the articles
	 * @param articleNodes	list of article nodes to add
	 * @param concise		parameter to determine a concise or verbose list
	 */
	public void constructList(ConnectedArticle ca, List<ConnectedNode> articleNodes, String concise) {
		List<Article> articles;
		List<Float> scores;
		Article a;
		
		if (articleNodes != null && !articleNodes.isEmpty()) {
			articles = new ArrayList<Article>();
			scores = new ArrayList<Float>();
			for(ConnectedNode cn: articleNodes) {
				a = new Article();
				if(concise.equals("true")) {
					// get the title and url alone and assign it to the article
					String title = GraphOperations.getProperty(cn.getNode(),GraphUtility.TITLE);
					a.setTitle(title);
					String conurl = GraphOperations.getProperty(cn.getNode(),GraphUtility.URL);
					a.setUrl(conurl);
				}
				else {
					// perform the mapping b/w the node and article
					GraphOperations.nodeToArticleMapper(a, cn.getNode());
				}
				scores.add(cn.getScore());
				articles.add(a);
			}
			ca.setConnectedarticles(articles);
			ca.setScores(scores);
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
