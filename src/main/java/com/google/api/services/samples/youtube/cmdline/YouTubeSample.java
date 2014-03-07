/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.services.samples.youtube.cmdline;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoStatistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Main class for the YouTube Data API command line sample.
 * Demonstrates how to make an authenticated API call using OAuth 2 helper classes.
 */
public class YouTubeSample {

  /**
   * Be sure to specify the name of your application. If the application name is {@code null} or
   * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
   */
  private static final String APPLICATION_NAME = "MasterThesis/1.0";

  /** Directory to store user credentials. */
  private static final java.io.File DATA_STORE_DIR =
      new java.io.File(System.getProperty("user.home"), ".store/youtube_sample");

  private static final long NUMBER_OF_VIDEOS_RETURNED = 50;
  
	private static XPath xPath = XPathFactory.newInstance().newXPath();


  /**
   * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
   * globally shared instance across your application.
   */
  private static FileDataStoreFactory dataStoreFactory;

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  /** Global instance of the HTTP transport. */
  private static HttpTransport httpTransport;

  /** YouTube objects for authorized and unauthorized queries */
  private static YouTube authorized;
  private static YouTube unauthorized;
  
  /** List of string results to write to CSV */
  private static List<String> result = new ArrayList<String>();

  /** Authorizes the installed application to access user's protected data. */
  private static Credential authorize() throws Exception {
    // load client secrets
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
        new InputStreamReader(YouTubeSample.class.getResourceAsStream("/client_secrets.json")));
    if (clientSecrets.getDetails().getClientId().startsWith("Enter") ||
        clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
      System.out.println(
          "Overwrite the src/main/resources/client_secrets.json file with the client secrets file "
          + "you downloaded from the Quickstart tool or manually enter your Client ID and Secret "
          + "from https://code.google.com/apis/console/?api=youtube#project:462903269886 "
          + "into src/main/resources/client_secrets.json");
      System.exit(1);
    }

    // Set up authorization code flow.
    // Ask for only the permissions needed. Asking for more permissions will
    // reduce the number of users who finish the process for giving you access
    // to their accounts. It will also increase the amount of effort you will
    // have to spend explaining to users what you are doing with their data.
    // Here we are listing all of the available scopes. You should remove scopes
    // that you are not actually using.
    Set<String> scopes = new HashSet<String>();
    scopes.add(YouTubeScopes.YOUTUBE);
    scopes.add(YouTubeScopes.YOUTUBE_READONLY);
    scopes.add(YouTubeScopes.YOUTUBE_UPLOAD);
    scopes.add(YouTubeScopes.YOUTUBEPARTNER);
    scopes.add(YouTubeScopes.YOUTUBEPARTNER_CHANNEL_AUDIT);

    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        httpTransport, JSON_FACTORY, clientSecrets, scopes)
        .setDataStoreFactory(dataStoreFactory)
        .build();
    // authorize
    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
  }

  public static void main(String[] args) {
    try {
      // initialize the transport
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();

      // initialize the data store factory
      dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);

      // authorization
      Credential credential = authorize();

      // set up global YouTube instance for authorized queries
      authorized = new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
          .setApplicationName(APPLICATION_NAME).build();

      // set up global YouTube instance for unauthorized queries
      unauthorized = new YouTube.Builder(httpTransport, JSON_FACTORY, new HttpRequestInitializer() {
          public void initialize(HttpRequest request) throws IOException {
          }
      }).setApplicationName("youtube-cmdline-search-sample").build();

getRelatedVideoStatistics();       
 
 //     retrieveDatafiles();
  //    retrieveRelatedVideofiles();
      
//  	YouTube.Videos.List list = authorized.videos().list("id, snippet, contentDetails, recordingDetails, topicDetails");
//  	list.setId("NjHSy6QUv4s");
//  	VideoListResponse response = list.execute();
//  	List<Video> videos = response.getItems();
//  	System.out.println(videos.get(0).getSnippet().getTitle());
//  	System.out.println(videos.get(0).getSnippet().getDescription());
//  	System.out.println("Tags: " + videos.get(0).getSnippet().getTags());
//  	System.out.println("Cat id: " + videos.get(0).getSnippet().getCategoryId());
//  	System.out.println(videos.get(0).getContentDetails().getDuration());
//  	//System.out.println(videos.get(0).getRecordingDetails().getLocation());
//  	//System.out.println(videos.get(0).getRecordingDetails().getRecordingDate());
//  	System.out.println(videos.get(0).getTopicDetails().getTopicIds());
//  	System.out.println(videos.get(0).getTopicDetails().getRelevantTopicIds());
      
      //ArrayList<String> temp = getIndices("D:/workspace/YouTubeAPI/Dataset/extreme bike sports amsterdam 2000 top 50.txt");
      //List<List<SearchResult>> result = new ArrayList<List<SearchResult>>();
//      
//      for (int i = 0; i < temp.size(); i++) {
//    	  List<SearchResult> result = getRelatedVideos(temp.get(i), 50L);
//    	  writeToFile(result.iterator(), temp.get(i));
//    	   //result.add(getRelatedVideos(temp.get(i), 50L));
//      }
      
//      List<List<SearchResult>> result2 = new ArrayList<List<SearchResult>>();
//      for (int i = 0; i < result.size(); i++) {
//    	  result2.add(getRelatedVideos(result.get(i).getId().getVideoId(), 50L));
//      }
      
//      Map<String, Integer> resultmap = new HashMap<String, Integer>();
//      
//      for (int j = 0; j < result.size(); j++) {
//    	  List<SearchResult> res = result.get(j);
//    	  for (int k = 0; k < res.size(); k++) {
//    		  String s = res.get(k).getId().getVideoId();
//    		  if (!(resultmap.containsKey(s))) {
//    			  resultmap.put(s, 1);
//    		  }
//    		  else {
//    			  resultmap.put(s, resultmap.get(s) + 1);
//    		  }    			  
//    	  }
//      }
//      
//      ValueComparator bvc = new ValueComparator(resultmap);
//      TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
//      sorted_map.putAll(resultmap);
//      
//      System.out.println("Total number of videos: " + sorted_map.size());
//      
//      Iterator<Integer> it = sorted_map.values().iterator();
//      int count = 0;
//      int sum = 0;
//      while (it.hasNext()) {
//    	  int res = it.next();
//    	  
//    	  sum += res;
//    	  
//    	  if (res != 1) {
//    		  count++;
//    	  }
//      }
//      
//      System.out.println("Number of videos that occur in more related videos lists: " + count);
//      
//      float mean = sum / (float) sorted_map.size();
//      System.out.println("Sum: " + sum);
//      System.out.println("Mean: " + mean);
//      
//      float var = 0;
//      Iterator<Integer> l = sorted_map.values().iterator();
//      while (l.hasNext()) {
//    	  float a = l.next();
//    	  var += (mean - a) * (mean - a);
//      }
//            
//      float a = var / (float) sorted_map.size();      
//      float stdev = (float) Math.sqrt(a);
//      System.out.println("St dev: " + stdev);
//      System.out.println(sorted_map.toString());
      
  		// queryTerm
//  		String queryTerm = getInputQuery();
////  		  		
////  		// Define the API request for retrieving search results
//  		YouTube.Search.List search = authorized.search().list("id, snippet");   
////	    // Get related videos
////	    //search.setRelatedToVideoId(queryTerm);
////	    // Set developer key
////	   // search.setKey("AIzaSyAAQuVMA9K_bakrLGwFmTC_a4Foml6sv48");
////	    // Restrict the search results to only include videos. See:
////	    // https://developers.google.com/youtube/v3/docs/search/list#type
////	   //search.setType("video");	      
////	    // To increase efficiency, only retrieve the fields that the
////	    // application uses.
////	   //search.setFields("items(etag, id/kind,id/videoId,snippet/title)");
////	   //search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);	
////             
////  		//Set properties
////  		search.setVideoCategoryId("1");
//  		setParameters(queryTerm, search);
////    
////    	// Call the API and print response
//    	SearchListResponse searchResponse = search.execute();
//    	List<SearchResult> searchResultList = searchResponse.getItems();
//    	System.out.println("Number of search results: " + searchResultList.size());
////    
//////    	writeToFile("Waar kerst- en paastijd samenvallen top 10.txt", searchResultList.iterator(), queryTerm);
////  	
//    	prettyPrint(searchResultList.iterator(), queryTerm);
////    	String videoId = searchResultList.iterator().next().getId().getVideoId();
////    	String videoTitle = searchResultList.iterator().next().getSnippet().getTitle();
////    	System.out.println("VideoId: " + videoId);
////    	System.out.println("VideoTitle: " + videoTitle);
    	
      //testAuthorizationDifference();
      //testRelatedVideos();
      
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.exit(1);
  }

	public static ArrayList<String> getIndices(String filename) throws IOException {
		ArrayList<String> res = new ArrayList<String>();

		// read txt files
		BufferedReader br = new BufferedReader(new FileReader(filename));
		try {
			String line = br.readLine();
			
			while (line != null) {
				if (line.contains("Video Id:")) {
					res.add(line.substring(11));
				}
				line = br.readLine();
			}
		}
		finally {
			br.close();
		}
		return res;
	}  
	
	public static ArrayList<String> getTitles(String filename) throws IOException {
		ArrayList<String> res = new ArrayList<String>();

		// read txt files
		BufferedReader br = new BufferedReader(new FileReader(filename));
		try {
			String line = br.readLine();
			
			while (line != null) {
				if (line.contains("Title:")) {
					res.add(line.substring(8));
				}
				line = br.readLine();
			}
		}
		finally {
			br.close();
		}
		return res;
	}  

  
	private static List<SearchResult> getRelatedVideos(String index, Long videosReturned) throws IOException {
		// Define the API request for retrieving search results
		YouTube.Search.List search = authorized.search().list("id, snippet");   
    // Get related videos
    search.setRelatedToVideoId(index);
    // Set developer key
   search.setKey("AIzaSyAAQuVMA9K_bakrLGwFmTC_a4Foml6sv48");
    // Restrict the search results to only include videos. See:
    // https://developers.google.com/youtube/v3/docs/search/list#type
   search.setType("video");	      
    // To increase efficiency, only retrieve the fields that the
    // application uses.
   search.setFields("items(etag, id/kind,id/videoId,snippet/title)");
   search.setMaxResults(videosReturned);	

	// Call the API and print response
	SearchListResponse searchResponse = search.execute();
	List<SearchResult> searchResultList = searchResponse.getItems();
	
	return searchResultList;
	}
	
private static void testRelatedVideos() throws IOException {
	// List all videoId's for which to find the related videos
	List<String> queryTerms = new ArrayList<String>();
    queryTerms.add("gqO5smUIAes");
    queryTerms.add("ArADqrplH6Q");
    queryTerms.add("A3C7_VpPH5g");
    queryTerms.add("escZxOtW8D4");
    queryTerms.add("KVrKitkT1hI");
    queryTerms.add("Q0iI2iBLBrk");
    queryTerms.add("Q7G2wZwDprE");
    queryTerms.add("CZLSDZh-QCU");
    queryTerms.add("K20n8SEhyqY");
    queryTerms.add("oeO92iNgNyI");
        
    for (int i = 0; i < queryTerms.size(); i++) {
  	  	// Get videoId
  	  	String queryTerm = queryTerms.get(i);

	    // Define the API request for retrieving search results
	    YouTube.Search.List search = unauthorized.search().list("id, snippet");
	    
	    // Get related videos
	    search.setRelatedToVideoId(queryTerm);
	    // Set developer key
	    search.setKey("AIzaSyAAQuVMA9K_bakrLGwFmTC_a4Foml6sv48");
	    // Restrict the search results to only include videos. See:
	    // https://developers.google.com/youtube/v3/docs/search/list#type
	    search.setType("video");	      
	    // To increase efficiency, only retrieve the fields that the
	    // application uses.
	    search.setFields("items(etag, id/kind,id/videoId,snippet/title)");
	    search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);	
	    
	    // Call the API and print response
	    SearchListResponse searchResponse = search.execute();
	    List<SearchResult> searchResultList = searchResponse.getItems();
	    
	    // Write the response to CSV
	    if (searchResultList != null) {
	  	  writeToCsv(searchResultList.iterator(), queryTerm);
	    }
    }
}

private static void getSearchResultVideoStatistics() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {
	List<String> queryTerms = new ArrayList<String>();
//	queryTerms.add("Sfeer rond kerstmis, paasfeest nadert al");
//  queryTerms.add("Een imitatie van Charlie Chaplin");
//  queryTerms.add("Marbles by Daan Roosegaarde");
//  queryTerms.add("Slipjacht door duinen en langs het strand");
//  queryTerms.add("Mucki op verboden wegen");
//  queryTerms.add("Internationaal vliegfeest");
//  queryTerms.add("Inspectie politietroepen");
//  queryTerms.add("De angst lag als een deksel over Amsterdam");
//  queryTerms.add("Race fly trash");
//  queryTerms.add("Arabian gun twirler");
//  queryTerms.add("Het grootste klokhuis van ons land");
//  queryTerms.add("Eindhoven  de intocht van St. Nicolaas");
//  queryTerms.add("Pontificale rouwdienst voor paus Pius XII");
//  queryTerms.add("De kermisschool doorkruist weer het land");
//  queryTerms.add("Viering van het 30-jarig bestaan van het Nijmeegse studentencorps");
//  queryTerms.add("De wilde eend 07 25");
//  queryTerms.add("MacBeth 14 33");
//  queryTerms.add("Fra cervello e movimento: Rosso 6 7");
//  queryTerms.add("Hobby van een luchtmiljonair");
//  queryTerms.add("Op wie stemt u ");   
  
//	  queryTerms.add("1 december 1967");
//	  queryTerms.add("29 januari 1934");
//	  queryTerms.add("12 juli 2013");
//	  queryTerms.add("1 januari 1974");
//	  queryTerms.add("1 januari 1920");
//	  queryTerms.add("1 juni 1968");
//	  queryTerms.add("25 juli 1931");
//	  queryTerms.add("19 februari 2010");
//	  queryTerms.add("1 oktober 2000");
//	  queryTerms.add("20 maart 1899");
//	  queryTerms.add("31 januari 1961");
//	  queryTerms.add("1 november 1941");
//	  queryTerms.add("15 oktober 1958");
//	  queryTerms.add("11 april 1958");
//	  queryTerms.add("14 mei 1954");
//	  queryTerms.add("28 februari 2003");
//	  queryTerms.add("27 april 2001");
//	  queryTerms.add("18 december 1997");
//	  queryTerms.add("1 juli 1951");
//	  queryTerms.add("29 juni 1948");   

  queryTerms.add("Polygoon-Profilti (producent)   Nederlands Instituut voor Beeld en Geluid (beheerder)");
  queryTerms.add("studioroosegaarde");
  queryTerms.add("Unknown (director)   Unknown (producer)");
  queryTerms.add("Nationaal Comité 4 en 5 mei");
  queryTerms.add("Keller, Paul");
  queryTerms.add("Edison Manufacturing Co.");
  queryTerms.add("Toneelgroep Amsterdam, Gerardjan Rijnders (regie)   Erik Lint (video)");
  queryTerms.add("ICK Amsterdam, Emio Greco   PC (regie en concept)   Erik Lint (video)");

  List<String> nodes = getAllNodes();

  for (int i = 0; i < queryTerms.size(); i++) {
      ArrayList<String> temp = getTitles("D:/workspace/YouTubeAPI/Dataset/Dataset producer/" + queryTerms.get(i) + " top 50.txt");
      
      System.out.println("Related videos of: " + queryTerms.get(i));
      
      for (int j = 0; j < temp.size(); j++) {
    	  String s = temp.get(j);
    	  if (nodes.indexOf(s) != -1) {
    		  System.out.println(nodes.get(nodes.indexOf(s)));
    	  }
      }
  }
}

private static void getRelatedVideoStatistics() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {
	List<String> queryTerms = new ArrayList<String>();
//	  queryTerms.add("Eindhoven de intocht van St. Nicolaas");
//	  queryTerms.add("Op wie stemt u"); 
//	queryTerms.add("Een imitatie van Charlie Chaplin");
//  queryTerms.add("Marbles by Daan Roosegaarde");
//  queryTerms.add("Internationaal vliegfeest");
//  queryTerms.add("Race fly trash");
//  queryTerms.add("Arabian gun twirler");
//  queryTerms.add("Het grootste klokhuis van ons land");
//  queryTerms.add("Pontificale rouwdienst voor paus Pius XII");
//  queryTerms.add("Viering van het 30-jarig bestaan van het Nijmeegse studentencorps");
//  queryTerms.add("MacBeth 14 33");
//  queryTerms.add("Hobby van een luchtmiljonair");
  
//	  queryTerms.add("1 december 1967");
//	  queryTerms.add("29 januari 1934");
//	  queryTerms.add("12 juli 2013");
//	  queryTerms.add("1 januari 1974");
//	  queryTerms.add("1 januari 1920");
//	  queryTerms.add("1 juni 1968");
//	  queryTerms.add("25 juli 1931");
//	  queryTerms.add("19 februari 2010");
//	  queryTerms.add("1 oktober 2000");
//	  queryTerms.add("20 maart 1899");
//	  queryTerms.add("31 januari 1961");
//	  queryTerms.add("1 november 1941");
//	  queryTerms.add("15 oktober 1958");
//	  queryTerms.add("11 april 1958");
//	  queryTerms.add("14 mei 1954");
//	  queryTerms.add("28 februari 2003");
//	  queryTerms.add("27 april 2001");
//	  queryTerms.add("18 december 1997");
//	  queryTerms.add("1 juli 1951");
//	  queryTerms.add("29 juni 1948");   

  queryTerms.add("Polygoon-Profilti (producent)   Nederlands Instituut voor Beeld en Geluid (beheerder)");
//  queryTerms.add("studioroosegaarde");
  //queryTerms.add("Unknown (director) Unknown (producer)");
//  queryTerms.add("Nationaal Comité 4 en 5 mei");
//  queryTerms.add("Keller, Paul");
//  queryTerms.add("Edison Manufacturing Co.");
  //queryTerms.add("Toneelgroep Amsterdam, Gerardjan Rijnders (regie)   Erik Lint (video)");
  //queryTerms.add("ICK Amsterdam, Emio Greco   PC (regie en concept)   Erik Lint (video)");

	for (int a = 0; a < queryTerms.size(); a++) {
	ArrayList<String> indices = getIndices("D:/workspace/YouTubeAPI/Dataset/Dataset producer/" + queryTerms.get(a) + " top 50.txt");
	
  List<String> nodes = getAllNodes();

  for (int i = 0; i < indices.size(); i++) {
      ArrayList<String> temp = getTitles("D:/workspace/YouTubeAPI/Dataset/Dataset producer/Polygoon related videos/Dataset producer " + queryTerms.get(a) + "related videos " + indices.get(i) + " top 50.txt");
      
      System.out.println("Related videos of: " + queryTerms.get(a) + "/" + indices.get(i));
      
      for (int j = 0; j < temp.size(); j++) {
    	  String s = temp.get(j);
    	  if (nodes.indexOf(s) != -1) {
    		  System.out.println(nodes.get(nodes.indexOf(s)));
    	  }
      }
  }
  }
}

private static void retrieveRelatedVideofiles() throws IOException {
	List<String> queryTerms = new ArrayList<String>();
//	queryTerms.add("Sfeer rond kerstmis, paasfeest nadert al");
//  queryTerms.add("Een imitatie van Charlie Chaplin");
//  queryTerms.add("Marbles by Daan Roosegaarde");
//  queryTerms.add("Slipjacht door duinen en langs het strand");
//  queryTerms.add("Mucki op verboden wegen");
//  queryTerms.add("Internationaal vliegfeest");
//  queryTerms.add("Inspectie politietroepen");
//  queryTerms.add("De angst lag als een deksel over Amsterdam");
//  queryTerms.add("Race fly trash");
//  queryTerms.add("Arabian gun twirler");
//  queryTerms.add("Het grootste klokhuis van ons land");
//  queryTerms.add("Eindhoven  de intocht van St. Nicolaas");
//  queryTerms.add("Pontificale rouwdienst voor paus Pius XII");
//  queryTerms.add("De kermisschool doorkruist weer het land");
//  queryTerms.add("Viering van het 30-jarig bestaan van het Nijmeegse studentencorps");
//  queryTerms.add("De wilde eend 07 25");
//  queryTerms.add("MacBeth 14 33");
//  queryTerms.add("Fra cervello e movimento: Rosso 6 7");
//  queryTerms.add("Hobby van een luchtmiljonair");
//  queryTerms.add("Op wie stemt u ");   

//	  queryTerms.add("1 december 1967");
//	  queryTerms.add("29 januari 1934");
//	  queryTerms.add("12 juli 2013");
//	  queryTerms.add("1 januari 1974");
//	  queryTerms.add("1 januari 1920");
//	  queryTerms.add("1 juni 1968");
//	  queryTerms.add("25 juli 1931");
//	  queryTerms.add("19 februari 2010");
//	  queryTerms.add("1 oktober 2000");
//	  queryTerms.add("20 maart 1899");
//	  queryTerms.add("31 januari 1961");
//	  queryTerms.add("1 november 1941");
//	  queryTerms.add("15 oktober 1958");
//	  queryTerms.add("11 april 1958");
//	  queryTerms.add("14 mei 1954");
//	  queryTerms.add("28 februari 2003");
//	  queryTerms.add("27 april 2001");
//	  queryTerms.add("18 december 1997");
//	  queryTerms.add("1 juli 1951");
//	  queryTerms.add("29 juni 1948");   

  queryTerms.add("Polygoon-Profilti (producent)   Nederlands Instituut voor Beeld en Geluid (beheerder)");
  queryTerms.add("studioroosegaarde");
  queryTerms.add("Unknown (director)   Unknown (producer)");
  queryTerms.add("Nationaal Comité 4 en 5 mei");
  queryTerms.add("Keller, Paul");
  queryTerms.add("Edison Manufacturing Co.");
  queryTerms.add("Toneelgroep Amsterdam, Gerardjan Rijnders (regie)   Erik Lint (video)");
  queryTerms.add("ICK Amsterdam, Emio Greco   PC (regie en concept)   Erik Lint (video)");

  for (int i = 0; i < queryTerms.size(); i++) {
		ArrayList<String> temp = getIndices("D:/workspace/YouTubeAPI/Dataset/Dataset producer/" + queryTerms.get(i) + " top 50.txt");
	    
	    for (int j = 0; j < temp.size(); j++) {
	  	  List<SearchResult> result = getRelatedVideos(temp.get(j), 50L);
	  	  writeToFile(result.iterator(), "Dataset producer/" + queryTerms.get(i) + "related videos/" + temp.get(j));
	    }
  }
}

private static void retrieveDatafiles() throws IOException {
	// List all videoId's for which to find the related videos
	List<String> queryTerms = new ArrayList<String>();
//    queryTerms.add("Sfeer rond kerstmis, paasfeest nadert al");
//    queryTerms.add("Een imitatie van Charlie Chaplin");
//    queryTerms.add("Marbles by Daan Roosegaarde");
//    queryTerms.add("Slipjacht door duinen en langs het strand");
//    queryTerms.add("Mucki op verboden wegen");
//    queryTerms.add("Internationaal vliegfeest");
//    queryTerms.add("Inspectie politietroepen");
//    queryTerms.add("De angst lag als een deksel over Amsterdam");
//    queryTerms.add("Race fly trash");
//    queryTerms.add("Arabian gun twirler");
//    queryTerms.add("Het grootste klokhuis van ons land");
//    queryTerms.add("Eindhoven: de intocht van St. Nicolaas");
//    queryTerms.add("Pontificale rouwdienst voor paus Pius XII");
//    queryTerms.add("De kermisschool doorkruist weer het land");
//    queryTerms.add("Viering van het 30-jarig bestaan van het Nijmeegse studentencorps");
//    queryTerms.add("De wilde eend 07/25");
//    queryTerms.add("MacBeth 14/33");
//    queryTerms.add("Fra cervello e movimento: Rosso 6/7");
//    queryTerms.add("Hobby van een luchtmiljonair");
//    queryTerms.add("Op wie stemt u?");   
    
//    queryTerms.add("Polygoon-Profilti (producent) / Nederlands Instituut voor Beeld en Geluid (beheerder)");
//    queryTerms.add("studioroosegaarde");
//    queryTerms.add("Unknown (director) / Unknown (producer)");
//    queryTerms.add("Nationaal Comité 4 en 5 mei");
//    queryTerms.add("Keller, Paul");
//    queryTerms.add("Edison Manufacturing Co.");
//    queryTerms.add("Toneelgroep Amsterdam, Gerardjan Rijnders (regie) / Erik Lint (video)");
//    queryTerms.add("ICK Amsterdam, Emio Greco | PC (regie en concept) / Erik Lint (video)");

  queryTerms.add("1 december 1967");
  queryTerms.add("29 januari 1934");
  queryTerms.add("12 juli 2013");
  queryTerms.add("1 januari 1974");
  queryTerms.add("1 januari 1920");
  queryTerms.add("1 juni 1968");
  queryTerms.add("25 juli 1931");
  queryTerms.add("19 februari 2010");
  queryTerms.add("1 oktober 2000");
  queryTerms.add("20 maart 1899");
  queryTerms.add("31 januari 1961");
  queryTerms.add("1 november 1941");
  queryTerms.add("15 oktober 1958");
  queryTerms.add("11 april 1958");
  queryTerms.add("14 mei 1954");
  queryTerms.add("28 februari 2003");
  queryTerms.add("27 april 2001");
  queryTerms.add("18 december 1997");
  queryTerms.add("1 juli 1951");
  queryTerms.add("29 juni 1948");   

    for (int i = 0; i < queryTerms.size(); i++) {
  	  	// Get videoId
  	  	String queryTerm = queryTerms.get(i);

	    // Define the API request for retrieving search results
	    YouTube.Search.List search = unauthorized.search().list("id, snippet");
	    	    
  		//Set properties
  		setParameters(queryTerm, search);
  		
	    // Call the API and print response
	    SearchListResponse searchResponse = search.execute();
	    List<SearchResult> searchResultList = searchResponse.getItems();
	    
	    // Write the response to CSV
	    if (searchResultList != null) {
	  	  writeToFile(searchResultList.iterator(), queryTerm);
	    }
    }
}

private static void writeToFile(Iterator<SearchResult> iteratorSearchResults,
		String query) throws IOException {
	
	query = query.replace('/', ' ');
	query = query.replace(':', ' ');
	query = query.replace('?', ' ');
	query = query.replace('|', ' ');
	
	PrintWriter writer = new PrintWriter("Dataset/" + query + " top " + NUMBER_OF_VIDEOS_RETURNED + ".txt", "UTF-8");
	
    writer.println("\n=============================================================");
    writer.println(
            "   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
    writer.println("=============================================================\n");

    if (!iteratorSearchResults.hasNext()) {
    	writer.println(" There aren't any results for your query.");
    }
        
    while (iteratorSearchResults.hasNext()) {

        SearchResult singleVideo = iteratorSearchResults.next();
        ResourceId rId = singleVideo.getId();        

        // Confirm that the result represents a video. Otherwise, the
        // item will not contain a video ID.
        if (rId.getKind().equals("youtube#video")) {
        	writer.println(" Etag: " + singleVideo.getEtag());
        	writer.println(" Video Id: " + rId.getVideoId());
        	writer.println(" Title: " + singleVideo.getSnippet().getTitle());
        	writer.println("\n-------------------------------------------------------------\n");
        }
    }
	
	writer.close();
}

private static void writeToCsv(Iterator<SearchResult> iterator, String queryTerm) throws IOException {
    File file = new File("RelatedVideos" + queryTerm + ".csv");
    FileWriter writer = null;
	Date date = new Date();

    //Create file if it doesn't exist
    if (!file.exists()) {
      file.createNewFile();
      writer = new FileWriter(file.getName(), true);
      
      //Check if there are results to write
      if (!iterator.hasNext()){
    	  System.out.println("No results found.");
      }
      //Write the queryterm, date and the related videos to the file
      else {
    	  writer.append(queryTerm);
    	  writer.append('\n');
    	  writer.append(date.toString());
    	  writer.append('\n');
    	  while (iterator.hasNext()){
      		  SearchResult singleVideo = iterator.next();
      		  writer.append(singleVideo.getId().getVideoId().replace(',', ';'));
      		  writer.append('\n');
      		  writer.append(singleVideo.getSnippet().getTitle().replace(',', ';'));
      		  writer.append('\n');
    	  }
      }
    }
    
    else {
    	// Read file
    	List<String> result = loadCSV(file.getName());

    	// Create writer, appending is false
    	writer = new FileWriter(file.getName(), false);
    	
    	// Add the queryTerm and date to the next column
    	result.set(0, result.get(0).concat("," + queryTerm));
    	result.set(1, result.get(1).concat("," + date.toString()));	
    	
    	// Start with 2, since 0 and 1 are the queryTerm and date
    	int index = 2;
    	while (iterator.hasNext()) {
    		// Case 1, the new column has more rows
    		SearchResult singleVideo = iterator.next();
    		if (index < result.size()) {
	    		result.set(index, result.get(index).concat("," + singleVideo.getId().getVideoId().replace(',', ';')));
	    		index++;
	    		result.set(index, result.get(index).concat("," + singleVideo.getSnippet().getTitle().replace(',', ';')));
	    		index++;
    		}
    		// If the new data has more rows it should be added instead of concatenated
    		else {
    			result.add("," + singleVideo.getId().getVideoId().replace(',', ';'));
    			result.add("," + singleVideo.getSnippet().getTitle().replace(',', ';'));
    		}
    	}
		// Case 2, the new column has less rows, so concat everywhere a comma, so the next column can be added properly
    	for (int i = index; i < result.size(); i++) {
    		result.set(i, result.get(i).concat(","));
    		index++;
    		result.set(i, result.get(i).concat(","));
    		index++;
    	}
    	
    	// Write all lines to the csv
    	for (String s : result) {
    		writer.append(s);
    		writer.append('\n');
    	}
    }

    writer.flush();
    writer.close();
}

@SuppressWarnings("resource")
private static List<String> loadCSV(String filename) throws IOException {
    List<String> result = new ArrayList<String>();
    String line = "";
    BufferedReader br = null;
    
    //Open readers
    br = new BufferedReader(new FileReader(filename));
    //Read all lines and add them to result
    while ((line = br.readLine()) != null) {
    	result.add(line);
    }
    
    System.out.println("CSV loaded");
    
    return result;
  }

private static void testAuthorizationDifference() throws IOException {
    // List all queries
	List<String> queryTerms = new ArrayList<String>();
    queryTerms.add("Airshow Seppe");
    queryTerms.add("Reportage Marbles Daan Roosegaarde Almere");
    queryTerms.add("Kerstmis paasfeest chocola paasei beeld geluid");
    queryTerms.add("Imitat Charlie Chaplin");
    queryTerms.add("Slipjacht hond paard vos");
    queryTerms.add("Monkey eat wheat cute");
    queryTerms.add("Chief police class insepction, -vehicle");
    queryTerms.add("Februaristaking Amsterdam");
    queryTerms.add("Alleycat bike race Amsterdam");
    queryTerms.add("Arabian gun twirler");
    
    for (int i = 0; i < queryTerms.size(); i++) {
  	  	// Get query
  	  	String queryTerm = queryTerms.get(i);
  	  
  	  	// Define the API request for retrieving search results
        YouTube.Search.List search = unauthorized.search().list("id, snippet");
        
        //Set properties
        setParameters(queryTerm, search);
        
        // Call the API and print response
        SearchListResponse searchResponseUnauthorized = search.execute();
        List<SearchResult> searchResultListUnauthorized = searchResponseUnauthorized.getItems();
        
        // Define the API request for retrieving search results
        search = authorized.search().list("id, snippet");
        
        //Set properties
        setParameters(queryTerm, search);
        
        // Call the API and print response
        SearchListResponse searchResponseAuthorized = search.execute();
        List<SearchResult> searchResultListAuthorized = searchResponseAuthorized.getItems();

        if (searchResultListUnauthorized != null && searchResultListAuthorized != null) {
      	  //prettyPrint(searchResultListAuthorized.iterator(), queryTerm);
      	  addToRow(searchResultListUnauthorized.iterator(), queryTerm);
      	  addToRow(searchResultListAuthorized.iterator(), queryTerm);
        }
    }
	
    //Write the results to CSV
    writeToCsv();	
}

private static void setParameters(
		String queryTerm, com.google.api.services.youtube.YouTube.Search.List search) {
      // Set the developer key from the Google Cloud Console for
      // non-authenticated requests. See:
      // https://cloud.google.com/console
      search.setKey("AIzaSyAAQuVMA9K_bakrLGwFmTC_a4Foml6sv48");
      search.setQ(queryTerm);
      
      // Restrict the search results to only include videos. See:
      // https://developers.google.com/youtube/v3/docs/search/list#type
      search.setType("video");
      
      // To increase efficiency, only retrieve the fields that the
      // application uses.
      search.setFields("items(etag, id/kind,id/videoId,snippet/title,snippet/publishedAt)");
      search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);	
}

private static void addToRow(Iterator<SearchResult> iterator, String queryTerm) {
    // If the result is empty, everything should be added
	if (result.isEmpty()){
		//Add the queryTerm, commas should be replaced since it will be written to csv
  	  result.add(queryTerm.replace(',', ';'));
  	  int index = 1;
  	  // Add all titles
  	  while (iterator.hasNext()) {
  		  SearchResult singleVideo = iterator.next();
  		  result.add(singleVideo.getSnippet().getTitle().replace(',', ';'));
  		  index++;
  	  }
  	  for (int i = index; i <= NUMBER_OF_VIDEOS_RETURNED; i++) {
  		  result.add(",");
  	  }
    }
	// If there were already some results, concatenate everything
    else {
  	  result.set(0, result.get(0).concat("," + queryTerm.replace(',', ';')));
  	  int index = 1;
  	  while (iterator.hasNext()) {
      	  result.set(index, result.get(index).concat("," + iterator.next().getSnippet().getTitle().replace(',', ';')));
      	  index++;
      }
  	  for (int i = index; i <= NUMBER_OF_VIDEOS_RETURNED; i++) {
  		  result.set(i, result.get(i).concat(","));
  	  }
    }
}

private static void writeToCsv() throws IOException {
	    File file = new File("ResearchResults.csv");
	    
	    //Create file if it doesn't exist
	    if (!file.exists()) {
	      file.createNewFile();
	    }

	    // Create writer
	    FileWriter writer = new FileWriter(file.getName(), true);
	    if (result.isEmpty()) {
		      System.out.println("No results Found.");
		}
	    else {      
		      //Append all results to the writer
		      for (int i = 0; i < result.size(); i++){
		    	  writer.append(result.get(i));
		    	  writer.append('\n');
		      }
	    }

	    writer.flush();
	    writer.close();
}

private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults,
		String query) throws IOException {
	String videoIds = "";
	
    System.out.println("\n=============================================================");
    System.out.println(
            "   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
    System.out.println("=============================================================\n");

    if (!iteratorSearchResults.hasNext()) {
        System.out.println(" There aren't any results for your query.");
    }
        
    while (iteratorSearchResults.hasNext()) {

        SearchResult singleVideo = iteratorSearchResults.next();
        Video v = new Video();
        ResourceId rId = singleVideo.getId();        

        // Confirm that the result represents a video. Otherwise, the
        // item will not contain a video ID.
        if (rId.getKind().equals("youtube#video")) {
            System.out.println(" Etag: " + singleVideo.getEtag());
            System.out.println(" Video Id: " + rId.getVideoId());
            System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
            //System.out.println(" Published At " + singleVideo.getSnippet().getPublishedAt());
            System.out.println("\n-------------------------------------------------------------\n");
            videoIds += rId.getVideoId() + ",";
        }
    }
    
//	YouTube.Videos.List list = authorized.videos().list("id, snippet, statistics");
//	videoIds = videoIds.substring(0, videoIds.length()-1);
//	list.setId(videoIds);
//	VideoListResponse response = list.execute();
//	List<Video> videos = response.getItems();
//	BigInteger champion = new BigInteger("0");
//	String video = "";
//	for (int i = 0; i < videos.size(); i++) {
//		Video vid = videos.get(i);
//		BigInteger views = vid.getStatistics().getViewCount();
//		System.out.println(vid.getSnippet().getTitle());
//		System.out.println(vid.getId());
//		System.out.println(views);
//		System.out.println(vid.getSnippet().getPublishedAt());
//		if (views.compareTo(champion) > 0) {
//			champion = views;
//			video = vid.getSnippet().getTitle();
//		}
//	}
//	System.out.println(champion);
//	System.out.println(video);
  }

  private static String getInputQuery() throws IOException {
	String inputQuery = "";
	
	System.out.println("Please enter a search term: ");
	BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
	inputQuery = bReader.readLine();
	
	// If there was no input query, use the dummy, to prevent errors
	if (inputQuery.length() < 1) {
		inputQuery = "YouTube Developers Live";
	}
	
	return inputQuery;
  }
  
	private static Document getMetadata() throws SAXException, IOException,
	ParserConfigurationException, TransformerException,
	XPathExpressionException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new FileInputStream("D:/Dropbox/Public/Master Thesis/Dataset/dataset.xml"));

		return document;
	}
	
	private static List<String> getNodes(Document document, String expression)
			throws XPathExpressionException {
		ArrayList<String> result = new ArrayList<String>();
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document,
					XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				if (nodeList.item(i).getFirstChild() != null){
					result.add(nodeList.item(i).getFirstChild().getNodeValue());
				}
				else {
					result.add("null");
				}
			}

		return result;
	}
	
	private static List<String> getAllNodes() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {
		List<String> result = new ArrayList<String>();
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:title'][@lang='nl']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:alternative'][@lang='nl']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:creator'][@lang='nl']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:subject'][@lang='nl']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:description'][@lang='nl']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:abstract'][@lang='nl']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:publisher'][@lang='nl']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:contributor']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:date']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:type']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:extent']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:medium']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:identifier']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:language']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:references'][@lang='nl']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:spatial'][@lang='nl']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:attributionName'][@lang='nl']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:attributionURL']"));
		result.addAll(getNodes(getMetadata(), "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:license']"));
		return result;	
	}
}

  class ValueComparator implements Comparator<String> {

	    Map<String, Integer> base;
	    public ValueComparator(Map<String, Integer> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
  }
