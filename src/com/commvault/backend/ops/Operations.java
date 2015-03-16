package com.commvault.backend.ops;

import java.util.Collections;
import java.util.List;

import com.commvault.backend.graphops.GraphOperations;
import com.commvault.backend.helper.GraphUtility;
import com.commvault.backend.model.RelatedNode;

import net.sf.classifier4J.summariser.SimpleSummariser;

/*
 * Class that defines some of the operations on the
 * search results
 * 
 * 
 * @author aswin
 */
public class Operations {
	
	/*
	 * Gives a n line summary of the input content
	 * (summarizer that makes use of classifier4j)
	 * @param content	content to be summarized
	 * @param n			number of lines the summary should be
	 * @return 			summarized content
	 */
	public static String getSummary(String content, int n) {
		SimpleSummariser summarizer = new SimpleSummariser();
		String summary = summarizer.summarise(content, n);
		return summary;
	}
	
	/*
	 * Reranks that list of related nodes according to the presence
	 * of the query term in the title
	 * @param relatedArticles	list of related nodes
	 * @param query				query term
	 */
	public static void reRank(List<RelatedNode> relatedArticles, String query) {
		String title;
		for (RelatedNode r: relatedArticles) {
			title = GraphOperations.getProperty(r.getNode(),GraphUtility.TITLE);
			if(title.toLowerCase().indexOf(query.toLowerCase()) != -1)
				r.setScore(r.getScore()+1);
		}
		Collections.sort(relatedArticles);
	}
	
}
