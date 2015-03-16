package com.commvault.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import uk.ac.shef.dcs.oak.jate.JATEException;
import uk.ac.shef.dcs.oak.jate.core.algorithm.CValueAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.CValueFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.RAKEAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.RAKEFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderTermNest;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureTermNest;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexBuilderMem;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexMem;
import uk.ac.shef.dcs.oak.jate.core.npextractor.NounPhraseExtractorOpenNLP;
import uk.ac.shef.dcs.oak.jate.model.Corpus;
import uk.ac.shef.dcs.oak.jate.model.CorpusImpl;
import uk.ac.shef.dcs.oak.jate.model.DocumentCustomImpl;
import uk.ac.shef.dcs.oak.jate.model.Term;
import uk.ac.shef.dcs.oak.jate.util.control.Lemmatizer;
import uk.ac.shef.dcs.oak.jate.util.counter.TermFreqCounter;
import uk.ac.shef.dcs.oak.jate.util.counter.WordCounter;

import com.commvault.backend.graphops.GraphOperations;
import com.commvault.backend.helper.GraphUtility;
import com.commvault.backend.helper.ResourceUtility;
import com.commvault.backend.helper.Utility;
import com.commvault.backend.model.GenericList;
import com.commvault.backend.model.SummaryNode;
import com.google.gson.Gson;

/*
 * Class defining the server resource to provide with
 * the summary feature.
 * 
 * @author aswin
 */
public class SummaryResource extends ServerResource{
	Map<String,Object> propMap;
	GraphDatabaseService db;
	Label articlelabel, subarticlelabel;
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
	 * Serves the summary request
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
		subarticlelabel = (Label) propMap.get("subarticlelabel");
		
		
		
		// Obtain the callback
		String callback = getQueryValue("callback");
		if(callback == null || callback.isEmpty()) {
			logger.info("NOT a Remote call");
			remote = false;
		} else {
			logger.info("Remote call");
		}
			
    	
		// Summarization
		Lemmatizer lemmatizer = (Lemmatizer) propMap.get("lemmatizer");
		//noun phrase extractor
		NounPhraseExtractorOpenNLP npextractor = (NounPhraseExtractorOpenNLP) propMap.get("npextractor");
		
		
		List<SummaryNode> summaryNodes = new ArrayList<SummaryNode>();
		
		
		if(npextractor == null) {
			
			logger.debug("Noun Phrase extractor error");
			
			if (remote)
				return new JsonpRepresentation(callback, getStatus(), new GsonRepresentation<String>(Utility.PROCESSING_ERROR));
			else
				return new GsonRepresentation<String>(Utility.PROCESSING_ERROR);
			
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
					
    				SummaryNode n;
    				List<String> rake = null;
    				List<String> cvalue = null;
					for(String url:urls.getList()) {
						n = new SummaryNode();
						// for every url, set the summary
    					n.setUrl(url);
    					try(Transaction tx = db.beginTx()) {
    						// obtain the related articles to this url
    						String content = GraphOperations.getProperty(GraphOperations.getNode(db, articlelabel, subarticlelabel, url), GraphUtility.CONTENT);	
    						
							//rake = getSummaryByRAKE(npextractor, content);
							cvalue = getSummaryByCValue(npextractor, lemmatizer, content);
								
    						tx.success();
    					}
    					n.setCvalue(cvalue);
    					n.setRake(rake);
    					summaryNodes.add(n);
    				}
					end = System.currentTimeMillis();
					logger.info("Request completed in: "+(end-start)+" ms");
					
					if (remote)
						return new JsonpRepresentation(callback, getStatus(), new GsonRepresentation<List<SummaryNode>>(summaryNodes));
					else
						return new GsonRepresentation<List<SummaryNode>>(summaryNodes);
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
	 * Gets the summary by CValue algorithm
	 * @param npextractor 	noun phrase extractor instance
	 * @param lemmatizer
	 * @param content		content to summarize
	 * @return				list of summary phrases sorted by their scores
	 */
	private List<String> getSummaryByCValue(NounPhraseExtractorOpenNLP npextractor, Lemmatizer lemmatizer, String content){
		List<String> summary = new ArrayList<String>();
		//counters
		TermFreqCounter npcounter = new TermFreqCounter();
		WordCounter wordcounter = new WordCounter();

		//create global resource index builder, which indexes global resources, such as documents and terms and their
		//relations
		GlobalIndexBuilderMem builder = new GlobalIndexBuilderMem();
		//build the global resource index
		Corpus c = new CorpusImpl();
		c.add(new DocumentCustomImpl(content));
		GlobalIndexMem termDocIndex = null;
		try {
			termDocIndex = builder.build(c, npextractor);
		} catch (JATEException e) {
			e.printStackTrace();
		}
		
		//build a feature store required by the tfidf algorithm, using the processors instantiated above
		FeatureCorpusTermFrequency termCorpusFreq = null;
		try {
			termCorpusFreq = new FeatureBuilderCorpusTermFrequency(npcounter, wordcounter, lemmatizer).build(termDocIndex);
		} catch (JATEException e) {
			e.printStackTrace();
		}
		
		FeatureTermNest termNest = null;
		try {
			termNest = new FeatureBuilderTermNest().build(termDocIndex);
		} catch (JATEException e) {
			e.printStackTrace();
		}
		Term[] terms = null;
		try {
			terms = new CValueAlgorithm().execute(new CValueFeatureWrapper(termCorpusFreq, termNest));
		} catch (JATEException e) {
			e.printStackTrace();
		}
		for ( Term t: terms)
			summary.add(t.getConcept());
		return summary;
	}

	/*
	 * Gets the summary by RAKE algorithm
	 * @param npextractor 	noun phrase extractor instance
	 * @param lemmatizer
	 * @param content		content to summarize
	 * @return				list of summary phrases sorted by their scores
	 */
	private List<String> getSummaryByRAKE(NounPhraseExtractorOpenNLP npextractor, String content){
		List<String> summary = new ArrayList<String>();
		//get the noun phrases
		Map<String, Set<String>> npmap = null;
		try {
			npmap = npextractor.extract(content);
		} catch (JATEException e) {
			e.printStackTrace();
		}
		Term[] terms = null;
		try {
			terms = new RAKEAlgorithm().execute(new RAKEFeatureWrapper(new ArrayList<>(npmap.keySet())));
		} catch (JATEException e) {
			e.printStackTrace();
		}
		for ( Term t: terms)
			summary.add(t.getConcept());
		return summary;
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
