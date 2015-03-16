package com.commvault.backend.graphops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import com.commvault.backend.helper.GraphUtility;
import com.commvault.backend.helper.Utility;
import com.commvault.backend.model.Article;
import com.commvault.backend.model.ConnectedNode;
import com.commvault.backend.model.RelatedNode;

/*
 * Class that defines most of the core algorithm to
 * interact with the Graph database to retrieve the 
 * search results.
 * 
 * 
 * @author aswin  
 */
public class GraphOperations {
	
	private static Logger logger = Logger.getLogger("Graph Operations");
	
	/*
	 * Registers a shutdown hook to the graph database to ensure smooth shut down in case of crashes
	 * @param db	the graph database instance to register the shutdown hook for
	 */
	public static void registerShutdownHook( final GraphDatabaseService graphdb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
		logger.warn("Unexpected shut down of database");
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphdb.shutdown();
            }
        } );
    }
	
	/*
	 * Shuts down the database
	 * @param db	the graph database to shutdown
	 */
	public static void shutDown(final GraphDatabaseService db)
    {
		logger.info("Shutting down database ...");
        // START SNIPPET: shutdownServer
        db.shutdown();
        // END SNIPPET: shutdownServer
    }
	
	
	/*
	 * Retrieve the first article given the title
	 * @param db			graph database instance
	 * @param articlelabel
	 * @param title			title of the article
	 * @return 				node from the graph corresponding to the article
	 */
	public static Node retrieveArticle(final GraphDatabaseService db,Label articlelabel, String title) {
		Node a = null;
		try ( Transaction tx = db.beginTx() )
        {
			ResourceIterable<Node> articles = db.findNodesByLabelAndProperty(articlelabel, GraphUtility.TITLE, title);
			for(Node article:articles)
				a = article;
			tx.success();
        }
		return a;
	}
	
	/*
	 * Retrieve the first sub-article given the title
	 * @param db			graph database instance
	 * @param subarticlelabel
	 * @param title			title of the sub-article
	 * @return 				node from the graph corresponding to the sub-article
	 */
	public static Node retrieveSubArticle(final GraphDatabaseService db,Label subarticlelabel, String title) {
		Node s = null;
		try ( Transaction tx = db.beginTx() )
        {
			ResourceIterable<Node> articles = db.findNodesByLabelAndProperty(subarticlelabel, GraphUtility.TITLE, title);
			for(Node article:articles)
				s = article;
			tx.success();
        }
		return s;
	}
	
	/*
	 * Retrieve the node given the URL of the article/sub-article
	 * @param db			graph database instance
	 * @param alabel		article label
	 * @param slabel		sub-article label
	 * @param url			URL of the article
	 * @return				node from the graph that is identified by the url
	 */
	public static Node getNode(final GraphDatabaseService db, Label alabel, Label slabel, String url) {
		Node node = null;
		ResourceIterable<Node> nodes;
		try ( Transaction tx = db.beginTx() )
        {
			nodes = db.findNodesByLabelAndProperty(alabel, GraphUtility.URL, Utility.props.get("GLOBALBASE")+Utility.props.get("VER")+url);
			if ( nodes.iterator().hasNext() )
				for ( Node n:nodes )
					node = n;
			else {
				nodes = db.findNodesByLabelAndProperty(slabel, GraphUtility.URL, Utility.props.get("GLOBALBASE")+Utility.props.get("VER")+url);
				for ( Node n:nodes )
					node = n;
			}	
			tx.success();
        }
		return node;
	}
	
	/*
	 * Gets a random node from the graph database
	 * @param db	graph database instance
	 * @return 		random node from the fraph database
	 */
	public static Node getRandomNode(GraphDatabaseService db) {
		Node n = null;
		do {
			long id = (long)(Math.random()*15001);
			try {
				n = db.getNodeById(id);
			} catch (NotFoundException ne) {
				
			}
		} while (n == null);
		return n;
		
	}
	
	/*
	 * Searches for the article by title
	 * @param db	graph database instance
	 * @param type	type of article to search for (generic, article or sub-article)
	 * @param q		search query
	 * @return 		list of index hits (nodes) for the query
	 */
	public static IndexHits<Node> searchByTitle(GraphDatabaseService db, String type, String q) {
		Index<Node> fulltextTitle;
		IndexManager index = db.index();
		if(type.equals(GraphUtility.GENERICQUERY))
			fulltextTitle = index.forNodes(GraphUtility.ALLTITLE_FULLTEXT_INDEX);
		else if(type.equals(GraphUtility.ARTICLEQUERY))
			fulltextTitle = index.forNodes(GraphUtility.TITLE_FULLTEXT_INDEX);
		else
			fulltextTitle = index.forNodes(GraphUtility.SUBTITLE_FULLTEXT_INDEX);
		IndexHits<Node> hits = fulltextTitle.query(GraphUtility.TITLE, q);
		return hits;
	}
	
	/*
	 * Auto-suggests for the article by title
	 * @param db			graph database instance
	 * @param type			type of article to suggest for (generic, article or sub-article)
	 * @param partialq		partial search query
	 * @return				list of index hits (nodes) for the partial query
	 */
	public static IndexHits<Node> autosuggestByTitle(GraphDatabaseService db, String type, String partialq) {
		Index<Node> fulltextTitle;
		IndexManager index = db.index();
		if(type.equals(GraphUtility.GENERICQUERY))
			fulltextTitle = index.forNodes(GraphUtility.ALLTITLE_FULLTEXT_INDEX);
		else if(type.equals(GraphUtility.ARTICLEQUERY))
			fulltextTitle = index.forNodes(GraphUtility.TITLE_FULLTEXT_INDEX);
		else
			fulltextTitle = index.forNodes(GraphUtility.SUBTITLE_FULLTEXT_INDEX);
		IndexHits<Node> hits = fulltextTitle.query(GraphUtility.TITLE, partialq+"*");
		return hits;
	}
	
	/*
	 * Pulls the related articles for a given article identified by URL
	 * @param db	graph database instance
	 * @param type	type of article (article/sub-article)
	 * @param url	URL of the article for which related articles need to be found
	 * @return 		list of related nodes
	 */
	public static List<RelatedNode> getRelatedArticles(GraphDatabaseService db, Label type, String url) {
		Collection<String> matched, similar, different, original;
		List<Node> connectedArticles;
		RelatedNode ra;
		List<RelatedNode> relatedArticles = new ArrayList<RelatedNode>();
		IndexManager index = db.index();
		Index<Node> fulltextTopic = index.forNodes(GraphUtility.TOPIC_FULLTEXT_INDEX);
		ResourceIterable<Node> nodes = db.findNodesByLabelAndProperty(type, GraphUtility.URL, Utility.props.get("GLOBALBASE")+Utility.props.get("VER")+url);
		for(Node n: nodes) {
			String topics = getProperty(n, GraphUtility.TOPICTAGS);
			String types = getProperty(n, GraphUtility.TYPETAGS);
			if(topics == null || topics.isEmpty()) {
				// The article isn't tagged with any topic, so fallback to the connected article(s)
				connectedArticles = getFallbackConnectedArticles(db, type, url);
				for(Node conNode:connectedArticles) {
					ra = new RelatedNode();
					// setting the type to false to indicate that it is actually a connected article and not exactly a "related" article
					ra.setType(false);
					ra.setNode(conNode);
					relatedArticles.add(ra);
				}
				return relatedArticles;
			}
			Node node;
			String matches;
			String key;
			float score;
			float simsize, difsize;
			Map<String,Boolean> masterList = new HashMap<String,Boolean>();
			String[] taggedTopics = topics.split(",");
			original = new HashSet<String>(Arrays.asList(taggedTopics));
			// finding articles by every topic this article is tagged in
			for(String topic: taggedTopics) {
				if(!topic.isEmpty()) {
					IndexHits<Node> hits = fulltextTopic.query(GraphUtility.TOPICTAGS, topic);
					while(hits.hasNext()) {
						node = hits.next();
						
						// Ensuring no duplicates gets processed
						key = getProperty(node, GraphUtility.URL);
						if(key != null && !key.isEmpty()) {
							if(!masterList.containsKey(key))
								masterList.put(key, true);
							else
								continue;
							// Ensuring the article isn't same as the one picked
							if(key.equals(Utility.props.get("GLOBALBASE")+Utility.props.get("VER")+url))
								continue;
						}
						else
							continue;
						matches = getProperty(node, GraphUtility.TOPICTAGS);
						if(matches == null || matches.isEmpty()) {
							continue;
						}
						matched = new HashSet<String>(Arrays.asList(matches.split(",")));
						similar = new HashSet<String>(original);
						// retaining the similar topics b/w node and original article
						similar.retainAll(matched);
						// if there are no common topics skip to the next article because it is not a good candidate
						if(similar.isEmpty())
							continue;
						different = new HashSet<String>();
						different.addAll(original);
						different.addAll(matched);
						different.removeAll(similar);
						// if the number of topics by which they match is less than the number they differ then it is not a good match
						simsize = similar.size();
						difsize = different.size();
						if(difsize > simsize)
							continue;
						if (difsize == 0)
							score = simsize;
						else
							score = (similar.size()/different.size());
						// improve score based on presence of type information
						if(types != null && !types.isEmpty()) {
							String[] taggedTypes = types.split(",");
							score = score + taggedTypes.length*(0.1f);
						}
						ra = new RelatedNode();
						ra.setNode(node);
						ra.setType(true);
						ra.setScore(score);
						ra.setCommonTopics(similar);
						relatedArticles.add(ra);
					}
				}
				
			}
			
		}
		Collections.sort(relatedArticles);
		return relatedArticles;
	}
	
	
	/*
	 * Pulls the related articles for a given article identified by URL
	 * @param db	graph database instance
	 * @param type	type of article (article/sub-article)
	 * @param url	URL of the article for which related articles need to be found
	 * @return 		list of related nodes
	 */
	public static List<RelatedNode> getImprovedRelatedArticles(GraphDatabaseService db, Label type, String url) {
		Collection<String> matched, similar, different, original;
		List<Node> connectedArticles;
		RelatedNode ra;
		List<RelatedNode> relatedArticles = new ArrayList<RelatedNode>();
		IndexManager index = db.index();
		//Index<Node> fulltextTopic = index.forNodes(GraphUtility.TOPIC_FULLTEXT_INDEX);
		Index<Node> fulltextKey = index.forNodes(GraphUtility.KEY_FULLTEXT_INDEX);
		ResourceIterable<Node> nodes = db.findNodesByLabelAndProperty(type, GraphUtility.URL, Utility.props.get("GLOBALBASE")+Utility.props.get("VER")+url);
		for(Node n: nodes) {
			//String topics = getProperty(n, GraphUtility.TOPICTAGS);
			String types = getProperty(n, GraphUtility.TYPETAGS);
			String keys = getProperty(n, GraphUtility.KEYTAGS);
			if(keys == null || keys.isEmpty()) {
				// The article isn't tagged with any key phrase, so fallback to the connected article(s)
				connectedArticles = getFallbackConnectedArticles(db, type, url);
				for(Node conNode:connectedArticles) {
					ra = new RelatedNode();
					// setting the type to false to indicate that it is actually a connected article and not exactly a "related" article
					ra.setType(false);
					ra.setNode(conNode);
					relatedArticles.add(ra);
				}
				return relatedArticles;
			}
			Node node;
			String matches;
			String key;
			float score;
			float simsize, difsize;
			Map<String,Boolean> masterList = new HashMap<String,Boolean>();
			String[] taggedKeys = keys.split(",");
			original = new HashSet<String>(Arrays.asList(taggedKeys));
			// finding articles by every topic this article is tagged in
			for(String tkey: taggedKeys) {
				if(!tkey.isEmpty()) {
					IndexHits<Node> hits = fulltextKey.query(GraphUtility.KEYTAGS, tkey);
					while(hits.hasNext()) {
						node = hits.next();
						
						// Ensuring no duplicates gets processed
						key = getProperty(node, GraphUtility.URL);
						if(key != null && !key.isEmpty()) {
							if(!masterList.containsKey(key))
								masterList.put(key, true);
							else
								continue;
							// Ensuring the article isn't same as the one picked
							if(key.equals(Utility.props.get("GLOBALBASE")+Utility.props.get("VER")+url))
								continue;
						}
						else
							continue;
						matches = getProperty(node, GraphUtility.KEYTAGS);
						if(matches == null || matches.isEmpty()) {
							continue;
						}
						matched = new HashSet<String>(Arrays.asList(matches.split(",")));
						similar = new HashSet<String>(original);
						// retaining the similar topics b/w node and original article
						similar.retainAll(matched);
						// if there are no common topics skip to the next article because it is not a good candidate
						if(similar.isEmpty())
							continue;
						different = new HashSet<String>();
						different.addAll(original);
						different.addAll(matched);
						different.removeAll(similar);
						// if the number of topics by which they match is less than the number they differ then it is not a good match
						simsize = similar.size();
						difsize = different.size();
						if(difsize > simsize)
							continue;
						if (difsize == 0)
							score = simsize;
						else
							score = (similar.size()/different.size());
						// improve score based on presence of type information
						if(types != null && !types.isEmpty()) {
							String[] taggedTypes = types.split(",");
							score = score + taggedTypes.length*(0.1f);
						}
						ra = new RelatedNode();
						ra.setNode(node);
						ra.setType(true);
						ra.setScore(score);
						ra.setCommonTopics(similar);
						relatedArticles.add(ra);
					}
				}
				
			}
			
		}
		Collections.sort(relatedArticles);
		return relatedArticles;
	}
	
	/*
	 * Pulls the connected articles for a given article identified by URL
	 * @param db	graph database instance
	 * @param type	type of article (article/sub-article)
	 * @param url	URL of the article for which connected articles need to be found
	 * @return 		list of connected nodes
	 */
	public static List<ConnectedNode> getConnectedArticles(GraphDatabaseService db, Label type, String url) {
		List<ConnectedNode> connectedArticles = new ArrayList<ConnectedNode>();
		ResourceIterable<Node> nodes = db.findNodesByLabelAndProperty(type, GraphUtility.URL, Utility.props.get("GLOBALBASE")+Utility.props.get("VER")+url);
		Map<String,Boolean> masterList = new HashMap<String,Boolean>();
		String conurl, via, viaend;
		for(Node n: nodes) {
			// Get all the articles this article has a direct outlink to
			Iterable<Relationship> connections = n.getRelationships(GraphUtility.RelTypes.CONNECTED_TO, Direction.OUTGOING);
			for(Relationship connection:connections) {
				Node connectedArticle = connection.getEndNode();
				ConnectedNode connectedNode = new ConnectedNode();
				conurl = getProperty(connectedArticle, GraphUtility.URL);
				if(!masterList.containsKey(conurl))
					masterList.put(conurl, true);
				else
					continue;
				connectedNode.setNode(connectedArticle);
				via = (String) connection.getProperty(GraphUtility.VIA);
				viaend = (String) connection.getProperty(GraphUtility.VIAENDPOINT);
				if(via.isEmpty() || viaend.isEmpty())
					connectedNode.setScore(0.0f);
				else
					connectedNode.setScore(1.0f);
				connectedArticles.add(connectedNode);
			}
		}
		Collections.sort(connectedArticles);
		return connectedArticles;
	}
	
	/*
	 * Gets the connected articles when there are no related articles found
	 * @param db	graph database instance
	 * @param type	type of article (article/sub-article)
	 * @param url	URL of the article for which connected articles need to be found
	 * @return		list of connected nodes
	 */
	public static List<Node> getFallbackConnectedArticles(GraphDatabaseService db, Label type, String url) {
		List<Node> connectedArticles = new ArrayList<Node>();
		ResourceIterable<Node> nodes = db.findNodesByLabelAndProperty(type, GraphUtility.URL, Utility.props.get("GLOBALBASE")+Utility.props.get("VER")+url);
		Map<String,Boolean> masterList = new HashMap<String,Boolean>();
		String conurl;
		for(Node n: nodes) {
			// Get all the articles this article has a direct outlink to
			Iterable<Relationship> connections = n.getRelationships(GraphUtility.RelTypes.CONNECTED_TO, Direction.OUTGOING);
			for(Relationship connection:connections) {
				Node relatedArticle = connection.getEndNode();
				conurl = getProperty(relatedArticle, GraphUtility.URL);
				if(!masterList.containsKey(conurl))
					masterList.put(conurl, true);
				else
					continue;
				connectedArticles.add(relatedArticle);
			}
		}
		return connectedArticles;
	}
	
	/*
	 * Gets the connected sub-articles (PRESENT_IN) to a given article identified by URL
	 * @param db		graph database instance
	 * @param type		article label
	 * @param url		URL of the article for which connected articles need to be found
	 * @param heading	2/3 corresponding to H2/H3
	 * @return			list of nodes which are present in the article identified by url
	 */
	public static List<Node> getConnectedSubArticles(GraphDatabaseService db, Label type, String url, int heading) {
		List<Node> connectedSubArticles = new ArrayList<Node>();
		ResourceIterable<Node> nodes = db.findNodesByLabelAndProperty(type, GraphUtility.URL, Utility.props.get("GLOBALBASE")+Utility.props.get("VER")+url);
		for(Node n: nodes) {
			Iterable<Relationship> connections;
			if (heading == 2)
				connections = n.getRelationships(GraphUtility.RelTypes.H2_PRESENT_IN, Direction.INCOMING);
			else
				connections = n.getRelationships(GraphUtility.RelTypes.H3_PRESENT_IN, Direction.INCOMING);
		
			for(Relationship connection:connections) {
				Node relatedArticle = connection.getStartNode();
				connectedSubArticles.add(relatedArticle);
			}
		}
		return connectedSubArticles;
	}
	
	/*
	 * Gets the specific property of a node
	 * @param n			node whose property needed
	 * @param promName	property which is to be obtained
	 * @return 			property or null if the property is not found
	 */
	public static String getProperty(Node n, String propName) {
		String property = null;
		try {
			property = (String) n.getProperty(propName);
			property = property.replaceAll("\\[|\\]", "");
		} catch (NotFoundException ne) {
			
		}
		return property;
	}
	
	/*
	 * Maps a node to an article (all the properties to fields)
	 * @param a	article to which the properties retrieved need to be assigned
	 * @param n node in the graph from which properties need to be retrieved
	 */
	public static void nodeToArticleMapper(Article a, Node n) {
		
		String value;
		a.setTitle((String)n.getProperty(GraphUtility.TITLE));		
		if((value = (String)n.getProperty(GraphUtility.CONTENT)) != null)
			a.setContent(value);
		if((value = getProperty(n, GraphUtility.BOOSTHEADS)) != null)
			a.setBoostHeads(Arrays.asList(value.split(",")));
		if((value = getProperty(n, GraphUtility.URL)) != null)
			a.setUrl(value);
		if((value = getProperty(n, GraphUtility.H2TAGS)) != null)
			a.setH2(Arrays.asList(value.split(",")));
		if((value = getProperty(n, GraphUtility.H3TAGS)) != null)
			a.setH3(Arrays.asList(value.split(",")));
		if((value = getProperty(n, GraphUtility.H4TAGS)) != null)
			a.setH4(Arrays.asList(value.split(",")));
		if((value = getProperty(n, GraphUtility.H5TAGS)) != null)
			a.setH5(Arrays.asList(value.split(",")));
		if((value = getProperty(n, GraphUtility.H6TAGS)) != null)
			a.setH6(Arrays.asList(value.split(",")));
		if((value = getProperty(n, GraphUtility.OUTLINKS)) != null)
			a.setOutlinkedArticles(Arrays.asList(value.split(",")));
		if((value = getProperty(n, GraphUtility.RELATEDLINKS)) != null)
			a.setOutlinkedRelatedLinks(Arrays.asList(value.split(",")));
		if((value = getProperty(n, GraphUtility.EXTLINKS)) != null)
			a.setExternalLinks(Arrays.asList(value.split(",")));
		if((value = getProperty(n, GraphUtility.IMAGELINKS)) != null)
			a.setImgLinks(Arrays.asList(value.split(",")));
	}
	
	
}
