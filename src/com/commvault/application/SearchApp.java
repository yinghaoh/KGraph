package com.commvault.application;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Parameter;
import org.restlet.routing.Router;

import uk.ac.shef.dcs.oak.jate.core.npextractor.NounPhraseExtractorOpenNLP;
import uk.ac.shef.dcs.oak.jate.util.control.Lemmatizer;
import uk.ac.shef.dcs.oak.jate.util.control.StopList;

import com.commvault.backend.graphops.GraphOperations;
import com.commvault.backend.helper.GraphUtility;
import com.commvault.backend.helper.Utility;
import com.commvault.resources.ConnectedSubArticlesResource;
import com.commvault.resources.ImprovedRelatedArticlesResource;
import com.commvault.resources.RandomArticleResource;
import com.commvault.resources.ConnectedArticlesResource;
import com.commvault.resources.RelatedArticlesResource;
import com.commvault.resources.RandomTitleResource;
import com.commvault.resources.TitleSearchResource;
import com.commvault.resources.AutoSuggestResource;
import com.commvault.resources.SummaryResource;
import com.commvault.resources.Welcome;

/**
 * Class that defines the Search application. Creates a root 
 * Restlet that will receive all incoming calls. Routes the
 * incoming calls to the respective server resources.
 *
 *
 * @author aswin
 */

public class SearchApp extends Application {

	// Graph entities
	GraphDatabaseService db;
	Label articlelabel, subarticlelabel;
	Map<String,Object> propMap;
	
	private Logger logger;
	
	public SearchApp() throws IOException {
		logger = Logger.getLogger(this.getClass().getName());
		
		// Set graph props to map
		articlelabel = DynamicLabel.label(GraphUtility.ARTICLELABEL);
		subarticlelabel = DynamicLabel.label(GraphUtility.SUBARTICLELABEL);

		propMap = new ConcurrentHashMap<>();
		propMap.put("articlelabel",articlelabel);
		propMap.put("subarticlelabel",subarticlelabel);
		
		// summarization properties
		StopList stop = null;
		try {
			stop = new StopList(true);
		} catch (IOException e) {
			logger.error(e.getClass().getName(), e);
		}
		Lemmatizer lemmatizer = new Lemmatizer();
		propMap.put("lemmatizer", lemmatizer);
		NounPhraseExtractorOpenNLP npextractor = null;
		try {
			npextractor = new NounPhraseExtractorOpenNLP(stop, lemmatizer);
		} catch (IOException e) {
			logger.error(e.getClass().getName(), e);
		}
		propMap.put("npextractor", npextractor);
	}
	
	public void configure(Context cx) {
		for(Parameter param:cx.getParameters())
			Utility.props.put(param.getName(), param.getValue());
		db = new GraphDatabaseFactory().newEmbeddedDatabase( Utility.props.get("HOME") + Utility.props.get("DB_PATH") );
		logger.info("Graph DB initialized");
		GraphOperations.registerShutdownHook( db );
		propMap.put("graphdb", db);
	}
	
	/*
	 * Creates and assigns server resources to inbound routes
	 * 
	 * (non-Javadoc)
	 * @see org.restlet.Application#createInboundRoot()
	 */
	
    @Override
    public Restlet createInboundRoot() {
    	// Configure the application
    	configure(getContext());
    	
    	// Create a router Restlet that routes each call to a
        // new instance of a Resource.
    	Router router = new Router(getContext());
        // Set the graph database properties to the router's context
        router.getContext().setAttributes(propMap);
        logger.info("Property map set");
        // Defines default route
        router.attachDefault(Welcome.class);
        
        // Define access graph route
        router.attach("/randomtitle", RandomTitleResource.class);
        router.attach("/randomarticle", RandomArticleResource.class);
        router.attach("/searcharticle", TitleSearchResource.class);
        router.attach("/connectedarticles", ConnectedArticlesResource.class);
        router.attach("/connectedsubarticles", ConnectedSubArticlesResource.class);
        router.attach("/relatedarticles", RelatedArticlesResource.class);
        router.attach("/autosuggest", AutoSuggestResource.class);
        router.attach("/summary", SummaryResource.class);
        router.attach("/improvedrelatedarticles", ImprovedRelatedArticlesResource.class);
        logger.info("Routing done");
        return router;
    }
    
}
