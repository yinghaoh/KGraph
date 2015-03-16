package com.commvault.resources;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
/*
 * Class that defines the welcome page of the Rest API
 * 
 * 
 * @author aswin
 */
public class Welcome extends ServerResource {
	
	/*
	 * Displays the message
	 */
	@Get("txt")
    public String welcome() {
		
		return "try \n GET:\n1. /randomtitle for a random titled article\n"
    			+ "2. /randomarticle for a random article in json\n"
    			+ "3. /searcharticle for an article search by query on title (attach query by ?q=<your query>&t=generic/article/subarticle)\n"
    			+ "4. /autosuggest for an article by query on title (attach partial query by ?q=<your query>&t=generic/article/subarticle)\n"
    			+ "\n POST:\n5. /connectedarticles with json input to get a set of connected articles\n"
    			+ "eg. C:\\Users\\aramesh>curl -X POST http://localhost:8082/KGraph/connectedarticles -H"
    			+ "\"Content-Type: application/json\" -d @sample.json\n"
    			+ "\nwhere sample.json has content like:\n"
    			+ "'{\"concise\": \"false\", \"list\": [\"article?p=deployment/common_upgrade/upgrade_readiness_report.htm\",\"article?p=deployment/upgrade/ma.htm\"]}'\n"
    			+ "\nnote\na. that the keys should not change, \nb. that concise takes true/false for complete article/just the title&url, \nc. there can be any number of urls passed by the list param\n"
    			+ "\n6. /relatedarticles with json input just as above to get a set of related articles\n"
    			+ "the returned json will have scores for the related set of articles for every url passed\n"
    			+ "which can be used to rank the articles\n"
    			+ "\n7. /connectedsubarticles with json input to get a set of connected sub articles\n"
    			+ "\n8. /summary with json input to get a set of summary phrases\n";
    }
}
