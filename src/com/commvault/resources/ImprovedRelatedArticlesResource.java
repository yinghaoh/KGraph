package com.commvault.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import com.commvault.backend.model.GenericList;
import com.commvault.backend.model.RelatedArticles;
import com.commvault.backend.model.RelatedNode;
import com.google.gson.Gson;
/*
 * Class defining the server resource to provide with
 * the related articles feature.
 * 
 * @author aswin
 */
public class ImprovedRelatedArticlesResource extends ServerResource{
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
	 * Serves the related articles request
	 */
	@Post("json:javascript")
	public Representation relatedArticles(Representation entity) {
		
		long start, end;
		start = System.currentTimeMillis();
		
		Series<Header> headers = ResourceUtility.getMessageHeaders(getResponse());
		headers.add("Access-Control-Allow-Headers", "Content-Type");
		headers.add("Access-Control-Allow-Origin", "*");
		
		db = (GraphDatabaseService) propMap.get("graphdb");
		articlelabel = (Label) propMap.get("articlelabel");
		
		List<RelatedNode> articleNodes;
		
		
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
    				//JsonRepresentation request = new JsonRepresentation(entity);
    				/*
    				JsonParser parser = new JsonParser();
    				JsonObject obj = parser.parse().getAsJsonObject();*/
    				GenericList urls = gson.fromJson(entity.getText(), GenericList.class);
    				if (urls == null) {
    					logger.warn("No request URLs");
    					if (remote)
    		    			return new JsonpRepresentation(callback, getStatus(), new GsonRepresentation<String>(Utility.REQUEST_ERROR));
    		    		else
    		    			return new GsonRepresentation<String>(Utility.REQUEST_ERROR);
    				}
					List<RelatedArticles> relatedArticles = new ArrayList<RelatedArticles>();
					RelatedArticles ra;
					for(String url:urls.getList()) {
						// for every url, the set of related urls need to be obtained
    					ra = new RelatedArticles();
    					ra.setUrl(url);
    					try(Transaction tx = db.beginTx()) {
    						// obtain the related articles to this url
    						articleNodes = GraphOperations.getImprovedRelatedArticles(db, articlelabel, url);
    						constructList(ra, articleNodes, urls.getConcise());
    						tx.success();
    					}
    					relatedArticles.add(ra);
    				}
					end = System.currentTimeMillis();
					logger.info("Request completed in: "+(end-start)+" ms");
					
					if (remote)
						return new JsonpRepresentation(callback, getStatus(), new GsonRepresentation<List<RelatedArticles>>(relatedArticles));
					else
						return new GsonRepresentation<List<RelatedArticles>>(relatedArticles);
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
	 * Constructs the related set of articles
	 * @param ra 			RelatedArticle instance that gets added with the articles
	 * @param articleNodes	list of article nodes to add
	 * @param concise		parameter to determine a concise or verbose list
	 */
	public void constructList(RelatedArticles ra, List<RelatedNode> articleNodes, String concise) {
		List<Article> articles;
		List<String> commonTopics;
		List<Float> scores;
		List<Boolean> type;
		Article a;
		
		if (articleNodes != null && !articleNodes.isEmpty()) {
			articles = new ArrayList<Article>();
			commonTopics = new ArrayList<String>();
			scores = new ArrayList<Float>();
			type = new ArrayList<Boolean>();
			for(RelatedNode n: articleNodes) {
				a = new Article();
				if(concise.equals("true")) {
					// get the title, url, common topics alone and assign it to the article
					String title = GraphOperations.getProperty(n.getNode(),GraphUtility.TITLE);
					a.setTitle(title);
					String relurl = GraphOperations.getProperty(n.getNode(),GraphUtility.URL);
					a.setUrl(relurl);
				}
				else {
					// perform the mapping b/w the node and article
					GraphOperations.nodeToArticleMapper(a, n.getNode());
				}
				
				articles.add(a);
				// If n a related article then set the score and common topics else set common topics to "" and score to 0
				if(n.isType()) {
					scores.add(n.getScore());
					commonTopics.add(StringUtils.join(n.getCommonTopics(),","));
					type.add(true);
				}
				else {
					scores.add(0.0f);
					commonTopics.add("");
					type.add(false);
				}	
				
			}
			// related articles and the common topics it has got with the source article (identified by url)
			ra.setRelatedarticles(articles);
			ra.setScores(scores);
			ra.setCommontopics(commonTopics);
			ra.setTypelist(type);
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
