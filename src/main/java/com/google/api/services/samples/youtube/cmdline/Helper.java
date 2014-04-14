package com.google.api.services.samples.youtube.cmdline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

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

public class Helper {
	/**
	 * Be sure to specify the name of your application. If the application name
	 * is {@code null} or blank, the application will log a warning. Suggested
	 * format is "MyCompany-ProductName/1.0".
	 */
	private static final String APPLICATION_NAME = "MasterThesis/1.0";

	/** Directory to store user credentials. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(
			System.getProperty("user.home"), ".store/youtube_sample");

	/**
	 * Global instance of the {@link DataStoreFactory}. The best practice is to
	 * make it a single globally shared instance across your application.
	 */
	private static FileDataStoreFactory dataStoreFactory;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory
			.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport httpTransport;

	/** YouTube objects for authorized and unauthorized queries */
	private static YouTube authorized;
	private static YouTube unauthorized;

	/** Number of videos returned, maximum is 50 */
	private static final long NUMBER_OF_VIDEOS_RETURNED = 50;

	private static final String key = "AIzaSyAAQuVMA9K_bakrLGwFmTC_a4Foml6sv48";

	private static Credential authorize() throws Exception {
		// load client secrets
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
				JSON_FACTORY,
				new InputStreamReader(YouTubeSample.class
						.getResourceAsStream("/client_secrets.json")));
		if (clientSecrets.getDetails().getClientId().startsWith("Enter")
				|| clientSecrets.getDetails().getClientSecret()
						.startsWith("Enter ")) {
			System.out
					.println("Overwrite the src/main/resources/client_secrets.json file with the client secrets file "
							+ "you downloaded from the Quickstart tool or manually enter your Client ID and Secret "
							+ "from https://code.google.com/apis/console/?api=youtube#project:462903269886 "
							+ "into src/main/resources/client_secrets.json");
			System.exit(1);
		}

		// Set up authorization code flow.
		// Ask for only the permissions needed. Asking for more permissions will
		// reduce the number of users who finish the process for giving you
		// access
		// to their accounts. It will also increase the amount of effort you
		// will
		// have to spend explaining to users what you are doing with their data.
		// Here we are listing all of the available scopes. You should remove
		// scopes
		// that you are not actually using.
		Set<String> scopes = new HashSet<String>();
		scopes.add(YouTubeScopes.YOUTUBE);
		scopes.add(YouTubeScopes.YOUTUBE_READONLY);
		scopes.add(YouTubeScopes.YOUTUBE_UPLOAD);
		scopes.add(YouTubeScopes.YOUTUBEPARTNER);
		scopes.add(YouTubeScopes.YOUTUBEPARTNER_CHANNEL_AUDIT);

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport, JSON_FACTORY, clientSecrets, scopes)
				.setDataStoreFactory(dataStoreFactory).build();
		// authorize
		return new AuthorizationCodeInstalledApp(flow,
				new LocalServerReceiver()).authorize("user");
	}

	public static void main(String[] args) throws Exception {
//		 Dataset dataset = new Dataset();
//		 List<String> titles = dataset.getTitles();
//		 Random randomGenerator = new Random();
//		
//		 System.out.println(titles.size());
//		 for (int i = 0; i < 20; i++) {
//		 int value = randomGenerator.nextInt(2544);
//		 System.out.println(titles.get(value));
//		 }

		// initialize the transport
		httpTransport = GoogleNetHttpTransport.newTrustedTransport();

		// initialize the data store factory
		dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);

		// authorization
		Credential credential = authorize();

		// set up global YouTube instance for authorized queries
		authorized = new YouTube.Builder(httpTransport, JSON_FACTORY,
				credential).setApplicationName(APPLICATION_NAME).build();

		// set up global YouTube instance for unauthorized queries
		unauthorized = new YouTube.Builder(httpTransport, JSON_FACTORY,
				new HttpRequestInitializer() {
					public void initialize(HttpRequest request)
							throws IOException {
					}
				}).setApplicationName("youtube-cmdline-search-sample").build();

		String feature = getInputQuery();

		// Define the API request for retrieving search results.
		YouTube.Search.List search = authorized.search().list("id,snippet");
		
		// Set your developer key from the Google Developers Console for
		// non-authenticated requests. See:
		// https://cloud.google.com/console
		search.setKey(key);
		search.setQ(feature);

		// Restrict the search results to only include videos. See:
		// https://developers.google.com/youtube/v3/docs/search/list#type
		search.setType("video");

		// To increase efficiency, only retrieve the fields that the
		// application uses.
		search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
		search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

		// Call the API and print results.
		SearchListResponse searchResponse = search.execute();
		List<SearchResult> searchResultList = searchResponse.getItems();
		if (searchResultList != null) {
			prettyPrint(searchResultList.iterator(), feature);
		}		
	}

	private static String getInputQuery() throws IOException {
		String inputQuery = "";

		System.out.println("Please enter on which feature you want to query: ");
		BufferedReader bReader = new BufferedReader(new InputStreamReader(
				System.in));
		inputQuery = bReader.readLine();

		// If there was no input query, use the dummy, to prevent errors

		return inputQuery;
	}

	private static void prettyPrint(
			Iterator<SearchResult> iteratorSearchResults, String query)
			throws IOException {

		System.out
				.println("\n=============================================================");
		System.out.println("   First " + NUMBER_OF_VIDEOS_RETURNED
				+ " videos for search on \"" + query + "\".");
		System.out
				.println("=============================================================\n");

		if (!iteratorSearchResults.hasNext()) {
			System.out.println(" There aren't any results for your query.");
		}

		while (iteratorSearchResults.hasNext()) {
			SearchResult singleVideo = iteratorSearchResults.next();
			ResourceId rId = singleVideo.getId();

			// Confirm that the result represents a video. Otherwise, the
			// item will not contain a video ID.
			if (rId.getKind().equals("youtube#video")) {
				System.out.println(" Etag: " + singleVideo.getEtag());
				System.out.println(" Video Id: " + rId.getVideoId());
				System.out.println(" Title: "
						+ singleVideo.getSnippet().getTitle());
				YouTube.Videos.List list = authorized.videos().list(
						"id, snippet, statistics");
				list.setId(rId.getVideoId());
				VideoListResponse response = list.execute();
				List<Video> videos = response.getItems();
				BigInteger views = videos.get(0).getStatistics().getViewCount();
				System.out.println(" Views: " + views);
				System.out
						.println("\n-------------------------------------------------------------\n");
				
				YouTube.Search.List search = authorized.search().list("id,snippet");
				search.setRelatedToVideoId(rId.getVideoId());
				search.setType("video");
				search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
				search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
				SearchListResponse searchResponse = search.execute();
				List<SearchResult> searchResultList = searchResponse.getItems();
				Iterator<SearchResult> it = searchResultList.iterator(); 
				while (it.hasNext()) {
					SearchResult singleVideo2 = it.next();
					ResourceId rId2 = singleVideo2.getId();

					// Confirm that the result represents a video. Otherwise, the
					// item will not contain a video ID.
					if (rId2.getKind().equals("youtube#video")) {
						System.out.println(" Related video: ");
						System.out.println(" Etag: " + singleVideo2.getEtag());
						System.out.println(" Video Id: " + rId2.getVideoId());
						System.out.println(" Title: "
								+ singleVideo2.getSnippet().getTitle());
						System.out
								.println("\n-------------------------------------------------------------\n");
					}
				}
			}
		}
	}
}
