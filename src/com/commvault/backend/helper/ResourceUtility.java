package com.commvault.backend.helper;

/*
 * Class that is meant for utility methods that
 * can be used by server resources.
 * 
 * 
 * @author aswin
 */
import java.util.concurrent.ConcurrentMap;

import org.restlet.Message;
import org.restlet.engine.header.Header;
import org.restlet.util.Series;

public class ResourceUtility {
	 private static final String HEADERS_KEY = "org.restlet.http.headers";
	 
	 /*
	  * Gets the message headers of a given request/response message
	  * @param message	request/response message
	  * @return			headers that are set in the message
	  */
	 @SuppressWarnings("unchecked")
 	 public static Series<Header> getMessageHeaders(Message message) {
        ConcurrentMap<String, Object> attrs = message.getAttributes();
        Series<Header> headers = (Series<Header>) attrs.get(HEADERS_KEY);
        if (headers == null) {
            headers = new Series<Header>(Header.class);
            Series<Header> prev = (Series<Header>) 
                attrs.putIfAbsent(HEADERS_KEY, headers);
            if (prev != null) { headers = prev; }
        }
        return headers;
     }
}
