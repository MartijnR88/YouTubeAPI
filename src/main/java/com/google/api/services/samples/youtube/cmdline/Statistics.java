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
	
	public Statistics() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {
		queryTerms = new ArrayList<String>();
		queryTerms.add("Eindhoven de intocht van St. Nicolaas");
		queryTerms.add("Op wie stemt u"); 
		queryTerms.add("Een imitatie van Charlie Chaplin");
		queryTerms.add("Marbles by Daan Roosegaarde");
		queryTerms.add("Internationaal vliegfeest");
		queryTerms.add("Race fly trash");
		queryTerms.add("Arabian gun twirler");
		queryTerms.add("Het grootste klokhuis van ons land");
		queryTerms.add("Pontificale rouwdienst voor paus Pius XII");
		queryTerms.add("Viering van het 30-jarig bestaan van het Nijmeegse studentencorps");
		queryTerms.add("MacBeth 14 33");
		queryTerms.add("Hobby van een luchtmiljonair");
	  
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

		queryTerms.add("Polygoon-Profilti (producent)   Nederlands Instituut voor Beeld en Geluid (beheerder)");
		queryTerms.add("studioroosegaarde");
		queryTerms.add("Unknown (director) Unknown (producer)");
		queryTerms.add("Nationaal Comité 4 en 5 mei");
		queryTerms.add("Keller, Paul");
		queryTerms.add("Edison Manufacturing Co.");
		queryTerms.add("Toneelgroep Amsterdam, Gerardjan Rijnders (regie)   Erik Lint (video)");
		queryTerms.add("ICK Amsterdam, Emio Greco   PC (regie en concept)   Erik Lint (video)");
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

	public void getStatistics(String path) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {
		Dataset dataset = new Dataset();
		List<String> nodes = dataset.getDataset();

		for (int i = 0; i < queryTerms.size(); i++) {
			ArrayList<String> titles = getMetadata(path, " Title: ");
	      	System.out.println("Related videos of: " + queryTerms.get(i));
	      
	      	for (int j = 0; j < titles.size(); j++) {
	      		String title = titles.get(j);
	      		if (nodes.indexOf(title) != -1) {
	      			System.out.println(nodes.get(nodes.indexOf(title)));
	      		}
	      	}
		}
	}
	
	public void getStatistics(List<List<SearchResult>> result) {
		Map<String, Integer> resultmap = new HashMap<String, Integer>();
      
	      for (int j = 0; j < result.size(); j++) {
	    	  List<SearchResult> res = result.get(j);
	    	  
	    	  for (int k = 0; k < res.size(); k++) {
	    		  String id = res.get(k).getId().getVideoId();
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
	      
	      System.out.println("Total number of videos: " + sorted_map.size());
	      
	      Iterator<Integer> iterator = sorted_map.values().iterator();
	      int count = 0;
	      int sum = 0;
	      while (iterator.hasNext()) {
	    	  int res = iterator.next();
	    	  
	    	  sum += res;
	    	  
	    	  if (res != 1) {
	    		  count++;
	    	  }
	      }
	      
	      System.out.println("Number of videos that occur in more related videos lists: " + count);
	      
	      float mean = sum / (float) sorted_map.size();
	      System.out.println("Sum: " + sum);
	      System.out.println("Mean: " + mean);
	      
	      float variance_temp = 0;
	      Iterator<Integer> iterator2 = sorted_map.values().iterator();
	      while (iterator2.hasNext()) {
	    	  float value = iterator2.next();
	    	  variance_temp += (mean - value) * (mean - value);
	      }
	            
	      float variance = variance_temp / (float) sorted_map.size();      
	      float stdev = (float) Math.sqrt(variance);
	      System.out.println("St dev: " + stdev);
	      System.out.println(sorted_map.toString());
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