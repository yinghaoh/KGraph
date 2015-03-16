package com.commvault.backend.helper;

import org.neo4j.graphdb.RelationshipType;

/*
 * Class that defines some final variables that are used throughout
 * the project specific to Graph operations.
 * 
 * @author aswin
 */
public class GraphUtility {
	
	//public static final String GRAPHDB_PATH = Utility.HOME+"Neo4j\\default.graphdb";
	
	
	// labels
	public static final String ARTICLELABEL = "Article";
	public static final String SUBARTICLELABEL = "SubArticle";
	
	// node properties
	public static final String TITLE = "title";
	public static final String CONTENT = "content";
	public static final String BOOSTHEADS = "boostheads";
	public static final String URL = "url";
	public static final String H2TAGS = "h2tags";
	public static final String H3TAGS = "h3tags";
	public static final String H4TAGS = "h4tags";
	public static final String H5TAGS = "h5tags";
	public static final String H6TAGS = "h6tags";
	public static final String OUTLINKS = "outlinks";
	public static final String RELATEDLINKS = "relatedlinks";
	public static final String IMAGELINKS = "imglinks";
	public static final String EXTLINKS = "extlinks";
	public static final String BOLDWORDS = "bold";
	
	//tags
	public static final String TYPETAGS = "typetags";
	public static final String TOPICTAGS = "topictags";
	public static final String KEYTAGS = "keytags";
	
	// relationship properties
	public static final String VIA = "via";
	public static final String VIAENDPOINT = "viaendpoint";
	public static final String VIAURL = "viaurl";
	public static final String NAME = "name";
	
	//Index
	public static final String TITLE_FULLTEXT_INDEX = "title_fulltext_index";
	public static final String SUBTITLE_FULLTEXT_INDEX = "subtitle_fulltext_index";
	public static final String ALLTITLE_FULLTEXT_INDEX = "alltitle_fulltext_index";
	public static final String CONTENT_FULLTEXT_INDEX = "content_fulltext_index";
	public static final String TYPE_FULLTEXT_INDEX = "type_fulltext_index";
	public static final String TOPIC_FULLTEXT_INDEX = "topic_fulltext_index";
	public static final String KEY_FULLTEXT_INDEX = "key_fulltext_index";

	
	public static enum RelTypes implements RelationshipType
    {
		CONNECTED_TO,
		H2_PRESENT_IN,
		H3_PRESENT_IN
    }
	
	// Request Params
	
	public static final String GENERICQUERY = "generic";
	public static final String ARTICLEQUERY = "article";
	public static final String SUBARTICLEQUERY = "subarticle";
}
