package com.commvault.backend.helper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.log4j.Logger;

/*
 * Class that defines some of the general purpose final 
 * variables used throughout the project.
 * 
 * 
 * @author aswin
 */
public class Utility {
	
	public static Hashtable<String,String> props= new Hashtable<String,String>();
	
	// error messages
	
	public static final String NOT_FOUND = "no results found";
	public static final String DB_ERROR = "database error";
	public static final String EMPTY_QUERY = "empty query";
	public static final String PARSING_ERROR = "parsing error";
	public static final String REQUEST_ERROR = "request error";
	public static final String PROCESSING_ERROR = "processing error";
	
	public static final String DELIM = "|";
	public static final String COMMA = ",";
	public static final String FRAG = "#";
	public static final String RETURN = "\n";
	
	private static Logger logger = Logger.getLogger("Utility");
	
	/*
	 * Configures properties for the application
	 */
	public static void configure() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("config.properties");
			// load a properties file
			prop.load(input);
			// iterate through and set the properties to application property table
			for (String key: prop.stringPropertyNames()) {
				String val = prop.getProperty(key);
				Utility.props.put(key, val);
			}
		} catch (IOException ex) {
			logger.error(ex.getClass().getName(), ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					logger.error(e.getClass().getName(), e);
				}
			}
		}
	}
	
}
