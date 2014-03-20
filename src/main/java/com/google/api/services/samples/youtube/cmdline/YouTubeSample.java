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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.google.api.client.util.Sleeper;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

/**
 * Main class for the YouTube Data API command line sample. Demonstrates how to
 * make an authenticated API call using OAuth 2 helper classes.
 */
public class YouTubeSample {

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

	/** Authorizes the installed application to access user's protected data. */
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

	public static void main(String[] args) throws IOException,
			InterruptedException {
		while (true) {
			try {
				// initialize the transport
				httpTransport = GoogleNetHttpTransport.newTrustedTransport();

				// initialize the data store factory
				dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);

				// authorization
				Credential credential = authorize();

				// set up global YouTube instance for authorized queries
				authorized = new YouTube.Builder(httpTransport, JSON_FACTORY,
						credential).setApplicationName(APPLICATION_NAME)
						.build();

				// set up global YouTube instance for unauthorized queries
				unauthorized = new YouTube.Builder(httpTransport, JSON_FACTORY,
						new HttpRequestInitializer() {
							public void initialize(HttpRequest request)
									throws IOException {
							}
						}).setApplicationName("youtube-cmdline-search-sample")
						.build();

				String feature = "Producer";// getInputQuery();
				Dataset dataset = new Dataset();
				List<String> queries = new ArrayList<String>();

				if (feature.equals("Title")) {
					queries = dataset.getTitles();
				}

				else if (feature.equals("Date")) {
					queries = formatDates(dataset.getDates());
				}

				else if (feature.equals("Producer")) {
					queries = dataset.getPublishers();
				}

				else {
					System.out
							.println("You can only choose between Title, Date or Producer");
				}

				if (!queries.isEmpty()) {
					retrieveVideos(queries);

					for (String query : queries) {
						query = correctPath(query);
						File dir = new File("Dataset\\" + query);
						boolean dirCreated = dir.mkdirs();
						System.out.println(dirCreated);
						if (dirCreated)
							retrieveRelatedVideos(getIndices(query + ".txt"),
									dir);
					}
				}

			} catch (IOException e) {
				System.err.println("This message is a IOException message: "
						+ e.getMessage());
			} catch (Throwable t) {
				System.out.println("This is a Throwable: ");
				t.printStackTrace();
			}
		}
	}

	private static List<String> formatDates(List<String> dates) {
		ArrayList<String> result = new ArrayList<String>();
		for (String date : dates) {
			String[] parts = date.split("-");
			String year = parts[0];
			String month = parts[1];
			String day = parts[2];

			if (day.startsWith("0")) {
				day = day.substring(1);
			}

			int month_int = Integer.parseInt(month);
			String monthString;
			switch (month_int) {
			case 1:
				monthString = "January";
				break;
			case 2:
				monthString = "February";
				break;
			case 3:
				monthString = "March";
				break;
			case 4:
				monthString = "April";
				break;
			case 5:
				monthString = "May";
				break;
			case 6:
				monthString = "June";
				break;
			case 7:
				monthString = "July";
				break;
			case 8:
				monthString = "August";
				break;
			case 9:
				monthString = "September";
				break;
			case 10:
				monthString = "October";
				break;
			case 11:
				monthString = "November";
				break;
			case 12:
				monthString = "December";
				break;
			default:
				monthString = "Invalid month";
				break;
			}

			String res = day + " " + monthString + " " + year;
			result.add(res);
		}

		return result;
	}

	private static String correctPath(String path) {
		path = path.replace('#', ' ');
		path = path.replace('%', ' ');
		path = path.replace('&', ' ');
		path = path.replace('{', ' ');
		path = path.replace('}', ' ');
		path = path.replace('\"', ' ');
		path = path.replace('<', ' ');
		path = path.replace('>', ' ');
		path = path.replace('*', ' ');
		path = path.replace('?', ' ');
		path = path.replace('/', ' ');
		path = path.replace('$', ' ');
		path = path.replace('!', ' ');
		path = path.replace('\'', ' ');
		path = path.replace('\"', ' ');
		path = path.replace(':', ' ');
		path = path.replace('@', ' ');
		path = path.replace('+', ' ');
		path = path.replace('`', ' ');
		path = path.replace('|', ' ');
		path = path.replace('=', ' ');
		path = path.replaceAll("\\s+", "");

		return path;
	}

	private static void retrieveVideos(List<String> queryTerms)
			throws IOException {

		int count = 0;

		for (int i = 0; i < queryTerms.size(); i++) {
			// Get videoId
			String queryTerm = queryTerms.get(i);
			System.out.println("Processing file " + i + " of "
					+ queryTerms.size());
			File file = new File(correctPath(queryTerm) + ".txt");

			if (file.exists()) {
				count++;
				System.out.println("File with query " + queryTerm
						+ " already exists");
			}

			else if (!file.exists()) {
				System.out.println("Creating new file...");
				// Define the API request for retrieving search results
				YouTube.Search.List search = authorized.search().list(
						"id, snippet");

				// Set properties
				setParameters(queryTerm, search);

				// Call the API and print response
				SearchListResponse searchResponse = search.execute();
				List<SearchResult> searchResultList = searchResponse.getItems();

				if (searchResultList == null)
					System.out.println("SEARCH RESULT IS NULL!!!!!!!!!");

				// Write the response to CSV
				else if (searchResultList != null) {
					writeToFile(searchResultList.iterator(), file, queryTerm);
				}
			}
		}
		System.out.println("Total doubles: " + count);
	}

	private static void setParameters(String queryTerm,
			com.google.api.services.youtube.YouTube.Search.List search) {
		// Set the developer key from the Google Cloud Console for
		// non-authenticated requests. See:
		// https://cloud.google.com/console
		search.setKey(key);
		search.setQ(queryTerm);

		// Restrict the search results to only include videos. See:
		// https://developers.google.com/youtube/v3/docs/search/list#type
		search.setType("video");

		// To increase efficiency, only retrieve the fields that the
		// application uses.
		search.setFields("items(etag, id/kind,id/videoId,snippet/title,snippet/publishedAt)");
		search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
	}

	private static void retrieveRelatedVideos(ArrayList<String> indices,
			File dir) throws IOException {
		for (int j = 0; j < indices.size(); j++) {
			List<SearchResult> result = getRelatedVideos(indices.get(j),
					NUMBER_OF_VIDEOS_RETURNED);
			File file = new File(dir, indices.get(j) + ".txt");
			writeToFile(result.iterator(), file, indices.get(j));
		}
	}

	public static ArrayList<String> getIndices(String filename)
			throws IOException {
		ArrayList<String> result = new ArrayList<String>();

		// Read text files
		BufferedReader br = new BufferedReader(new FileReader(filename));
		try {
			String line = br.readLine();

			while (line != null) {
				if (line.contains("Video Id:")) {
					// Only take the id, not the "Video Id: " part
					result.add(line.substring(11));
				}
				line = br.readLine();
			}
		} finally {
			br.close();
		}

		return result;
	}

	private static List<SearchResult> getRelatedVideos(String index,
			Long videosReturned) throws IOException {
		// Define the API request for retrieving search results
		YouTube.Search.List search = authorized.search().list("id, snippet");
		// Get related videos
		search.setRelatedToVideoId(index);
		// Set developer key
		search.setKey(key);
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

	private static void writeToFile(
			Iterator<SearchResult> iteratorSearchResults, File path,
			String query) throws IOException {
		// path = path.getPath().replace('/', ' ');
		// path = path.replace('?', ' ');
		// path = path.replace('|', ' ');
		// path = path.replace(':', ' ');
		// path = path.replace('!', ' ');
		//
		PrintWriter writer = new PrintWriter(path, "UTF-8");

		writer.println("\n=============================================================");
		writer.println("   First " + NUMBER_OF_VIDEOS_RETURNED
				+ " videos for search on \"" + query + "\".");
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
				System.out
						.println("\n-------------------------------------------------------------\n");
			}
		}
	}

	private static void printVideoStatistics(String videoIds)
			throws IOException {
		YouTube.Videos.List list = authorized.videos().list(
				"id, snippet, statistics");
		list.setId(videoIds);
		VideoListResponse response = list.execute();
		List<Video> videos = response.getItems();
		BigInteger champion = new BigInteger("0");
		String video = "";
		for (int i = 0; i < videos.size(); i++) {
			Video vid = videos.get(i);
			BigInteger views = vid.getStatistics().getViewCount();
			System.out.println(vid.getSnippet().getTitle());
			System.out.println(vid.getId());
			System.out.println(views);
			System.out.println(vid.getSnippet().getPublishedAt());
			if (views.compareTo(champion) > 0) {
				champion = views;
				video = vid.getSnippet().getTitle();
			}
		}

		System.out.println(champion);
		System.out.println(video);
	}
}