package com.google.api.services.samples.youtube.cmdline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

/**
 * This class writes data about the retrieved videos to csv format for statistical analysis
 * 
 * @author Martijn Rentmeester
 *
 */
public class Statistics {
	private static int MAXIMUM_SEARCH_RESULTS = 50;

	/**
	 * Retrieves statistical data and writes it to CSV
	 * 
	 * @param args
	 * @throws XPathExpressionException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public static void main(String[] args) throws XPathExpressionException,
			SAXException, IOException, ParserConfigurationException,
			TransformerException {
		List<String> zones = new ArrayList<String>();
		zones.add(YouTubeRetriever.TITLE);
		zones.add(YouTubeRetriever.DATE);
		zones.add(YouTubeRetriever.PUBLISHER);
		Dataset dataset = new Dataset();
		
		for (String zone : zones) {
			List<String> metadata_zone = new ArrayList<String>();
			
			if (zone.equals(YouTubeRetriever.TITLE)) {
				metadata_zone = dataset.getTitles();
			}
			
			else if (zone.equals(YouTubeRetriever.PUBLISHER)) {
				metadata_zone = dataset.getPublishers();
			}
			
			else if (zone.equals(YouTubeRetriever.DATE)) {
				metadata_zone = dataset.getDates();
				metadata_zone = YouTubeRetriever.formatDates(metadata_zone);
			}
			
			//writeToCSV(retrieveRelatedVideos(metadata_zone, zone), zone + ".csv");
			writeToCSV(retrieveRelatedofRelatedVideos(metadata_zone, zone), "RelatedofRelated" + zone + ".csv");
		}
	}
	
	private static List<List<String>> retrieveRelatedVideos(List<String> metadata_zone, String zone) throws IOException {
		List<List<String>> result = new ArrayList<List<String>>();					

		for (String meta : metadata_zone) {
			List<String> feature = new ArrayList<String>();
			
			//Add the query
			meta = YouTubeRetriever.correctPath(meta);				
			feature.add(meta);
			
			//Retrieve the indices of the search results
			List<String> indices = getMetadata("Dataset\\" + zone + "\\" + meta + ".txt", "Video Id: ");
			for (String index : indices) {
				//Add index
				index = YouTubeRetriever.correctPath(index);					
				feature.add(index);
				
				//Retrieve the related videos for each search result
				List<String> metadata = getMetadata("Dataset\\" + zone + "\\" + meta + "\\" + index + ".txt", "Video Id: ");
				
				//Add all related videos
				feature.addAll(metadata);
				
				//Add null's up to the maximum number of search results to keep the final matrix in balance
				if (metadata.size() != MAXIMUM_SEARCH_RESULTS) {
					for (int i = metadata.size(); i < MAXIMUM_SEARCH_RESULTS; i++) {
						feature.add("NA");
					}
				}
			}
			
			//Add null's up to the maximum number of search results to keep the final matrix in balance
			if (indices.size() != MAXIMUM_SEARCH_RESULTS) {
				for (int i = indices.size(); i < MAXIMUM_SEARCH_RESULTS; i++){
					feature.add("NA");
					//Add null's for the maximum number of related videos
					for (int j = 0; j < MAXIMUM_SEARCH_RESULTS; j++) {
						feature.add("NA");
					}
				}
			}
			
			//Add the column to the final result
			result.add(feature);
		}
		
		return result;
	}

	private static List<List<String>> retrieveRelatedofRelatedVideos(List<String> metadata_zone, String zone) throws IOException {
		List<List<String>> result = new ArrayList<List<String>>();					

		for (String meta : metadata_zone) {
			List<String> feature = new ArrayList<String>();
			
			//Add the query
			meta = YouTubeRetriever.correctPath(meta);				
			
			String index = "";
			//Retrieve the indices of the search results
			if (!getMetadata("Dataset\\" + zone + "\\" + meta + ".txt", "Video Id: ").isEmpty()) {
				index = getMetadata("Dataset\\" + zone + "\\" + meta + ".txt", "Video Id: ").get(0);
				//Add index
				index = YouTubeRetriever.correctPath(index);					
				feature.add(index);
				//Retrieve the related videos for each search result
				List<String> metadata = getMetadata("Dataset\\" + zone + "\\" + meta + "\\" + index + ".txt", "Video Id: ");
				for (String m : metadata) {
					m = YouTubeRetriever.correctPath(m);
					feature.add(m);
					List<String> data = getMetadata("Dataset\\" + zone + "\\" + meta + "\\" + index + "\\" + m + ".txt", "Video Id: ");
					feature.addAll(data);
					
					if (data.size() != MAXIMUM_SEARCH_RESULTS) {
						for (int i = data.size(); i < MAXIMUM_SEARCH_RESULTS; i++) {
							feature.add("NA");
						}
					}
				}
				
				//Add null's up to the maximum number of search results to keep the final matrix in balance
				if (metadata.size() != MAXIMUM_SEARCH_RESULTS) {
					for (int i = metadata.size(); i < MAXIMUM_SEARCH_RESULTS; i++){
						feature.add("NA");
						//Add null's for the maximum number of related videos
						for (int j = 0; j < MAXIMUM_SEARCH_RESULTS; j++) {
							feature.add("NA");
						}
					}
				}
				
				//Add the column to the final result
				result.add(feature);
			}			
		}
		
		return result;
	}

	/**
	 * Retrieves the metadata from the file
	 * 
	 * @param filename
	 * @param metadata
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String> getMetadata(String filename, String metadata)
			throws IOException {
		ArrayList<String> result = new ArrayList<String>();

		// Read text files
		BufferedReader br = new BufferedReader(new FileReader(filename));
		try {
			String line = br.readLine();

			while (line != null) {
				if (line.contains(metadata)) {
					// Only take e.g. the title/id, not the " Title: " or
					// " Video Id: " part
					result.add(line.substring(metadata.length()));
				}
				line = br.readLine();
			}
		} finally {
			br.close();
		}
		return result;
	}
	
	/**
	 * Writes a matrix to CSV
	 * 
	 * @param results
	 * @param filename
	 * @throws IOException
	 */
	private static void writeToCSV(List<List<String>> results, String filename)
			throws IOException {
		File file = new File(filename);

		// Create file if it doesn't exist
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter writer = new FileWriter(file.getName(), true);
		if (results.size() == 0) {
			System.out.println("No results Found.");
		} else {
			// Append all results to the writer
			for (int i = 0; i < results.size(); i++) {
				List<String> row = results.get(i);
				for (String column : row) {
					writer.append(column.replaceAll(",", ";") + ",");
				}
				writer.append('\n');
			}
		}

		writer.flush();
		writer.close();
	}
}