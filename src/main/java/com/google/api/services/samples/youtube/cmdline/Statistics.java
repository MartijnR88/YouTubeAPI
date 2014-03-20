package com.google.api.services.samples.youtube.cmdline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.google.api.services.youtube.model.SearchResult;

public class Statistics {
	private static List<String> queryTerms;
	
	public static void main(String[] args) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {
		Dataset dataset = new Dataset();
		List<String> titles = dataset.getTitles();
		List<Integer> numberOfSearchResults = new ArrayList<Integer>();
		List<Integer> numberOfRelatedVideos = new ArrayList<Integer>();
		List<Integer> percentages = new ArrayList<Integer>();
		for (String title : titles) {
			List<List<String>> result = new ArrayList<List<String>>();
	    	title = correctPath(title);
			List<String> indices = getMetadata("Dataset titles/" + title + ".txt", "Video Id: ");
			numberOfSearchResults.add(indices.size());
			for (String index : indices) {
				index = correctPath(index);
				List<String> metadata = getMetadata("Dataset titles/" + title + "/" + index + ".txt", "Video Id: ");
				numberOfRelatedVideos.add(metadata.size());
				result.add(metadata);
			}
			
			TreeMap<String, Integer> map = getSortedMap(result);
			percentages.add(getStatistics(map));
		}
		
		Iterator<Integer> iterator = numberOfSearchResults.iterator();
		int count = 0;
		while (iterator.hasNext()){
			int next = (Integer) iterator.next();
			if (next == 0) {
				count++;
			}
		}
		
		System.out.println("Total titles:" + numberOfSearchResults.size());
		System.out.println("Total number of titles giving no result:" + count);
		System.out.println("Mean number of search results" + mean(numberOfSearchResults));
		System.out.println("St dev number of search results" + stdev(numberOfSearchResults));
		
		System.out.println("Mean percentages" + mean(percentages));
		System.out.println("St dev percentages" + stdev(percentages));
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
		  path = path.replaceAll("\\s+","");

		  return path;
	}
	
	private static float stdev(List<Integer> uniques) {
	    float mean = mean(uniques);  
		
		float variance_temp = 0;
	    for (int unique : uniques) {
	    	variance_temp += (mean - unique) * (mean - unique);
	    }
	    float variance = variance_temp / (float) uniques.size();
	    float stdev = (float) Math.sqrt(variance);
	    
	    return stdev;
	}

	private static float mean(List<Integer> uniques) {
	    int sum = 0;
		for (int unique : uniques) {
	    	sum += unique;
	    }
		
		float mean = sum / (float) uniques.size();
	    
	    return mean;
	}

	public Statistics() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {
	}
	
	public static ArrayList<String> getMetadata(String filename, String metadata) throws IOException {
		ArrayList<String> result = new ArrayList<String>();

		// Read text files
		BufferedReader br = new BufferedReader(new FileReader(filename));
		try {
			String line = br.readLine();
			
			while (line != null) {
				if (line.contains(metadata)) {
					//Only take e.g. the title/id, not the " Title: " or " Video Id: " part
					result.add(line.substring(metadata.length()));
				}
				line = br.readLine();
			}
		}
		finally {
			br.close();
		}
		return result;
	}  
		
	public static TreeMap<String, Integer> getSortedMap(List<List<String>> result) {
		Map<String, Integer> resultmap = new HashMap<String, Integer>();
      
	      for (int j = 0; j < result.size(); j++) {
	    	  List<String> res = result.get(j);
	    	  
	    	  for (int k = 0; k < res.size(); k++) {
	    		  String id = res.get(k);
	    		  if (!(resultmap.containsKey(id))) {
	    			  resultmap.put(id, 1);
	    		  }
	    		  else {
	    			  resultmap.put(id, resultmap.get(id) + 1);
	    		  }    			  
	    	  }
	      }
	      
	      ValueComparator comparator = new ValueComparator(resultmap);
	      TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(comparator);
	      sorted_map.putAll(resultmap);
	      
	      System.out.println("Sorted map: " + sorted_map.toString());
	      
	      return sorted_map;      
	}
	
	public static int getStatistics(TreeMap<String, Integer> sorted_map) {
	      Iterator<Integer> iterator = sorted_map.values().iterator();
	      int unique = 0;
	      int multiple = 0;
	      int sum = 0;
	      while (iterator.hasNext()) {
	    	  int res = iterator.next();
	    	  
	    	  sum += res;
	    	  
	    	  if (res == 1) {
	    		  unique++;
	    	  }
	    	  else {
	    		  multiple++;
	    	  }
	      }
	      
	      System.out.println("Number of videos:" + sum);
	      System.out.println("Number of unique videos: " + unique);
	      System.out.println("Number of videos occurring in multiple related video lists: " + multiple);
	      
	      int result;
	      if (!(sum == 0))
	    	  result = unique/sum;
	      else
	    	  result = 0;
	      
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