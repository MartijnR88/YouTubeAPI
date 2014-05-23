package com.google.api.services.samples.youtube.cmdline;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class loads an external dataset. Every feature in the dataset can be retrieved using its corresponding method. 
 * 
 * @author Martijn Rentmeester
 *
 */
public class Dataset {
    private static final String PROPERTIES_FILENAME = "youtube.properties";
	private static String PATH;
	private static XPath xPath = XPathFactory.newInstance().newXPath();
	private static Document document;
	
	public static void main(String[] args) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {
		Dataset d = new Dataset();
		d.toVideoIdJSON();
	}
	
	/**
	 * This methods loads the properties, sets the path to the dataset and finally loads the dataset
	 * 
	 * @throws XPathExpressionException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public Dataset() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {
        Properties properties = new Properties();
        try {
            InputStream in = Topics.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
            properties.load(in);

        } catch (IOException e) {
            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
                    + " : " + e.getMessage());
            System.exit(1);
        }
		
        PATH = properties.getProperty("dataset.path");
        loadDataset();
	}
	
	/**
	 * Loads the dataset.
	 * 
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws XPathExpressionException
	 */
	private static void loadDataset() throws SAXException, IOException, ParserConfigurationException, TransformerException, XPathExpressionException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(new FileInputStream(PATH));
	}

	/**
	 * This method retrieves a list of nodes using a given expression
	 * 
	 * @param document
	 * @param expression
	 * @return returns the list of nodes, retrieved using the expression
	 * @throws XPathExpressionException
	 */
	private static List<String> getNodes(Document document, String expression) throws XPathExpressionException {
		ArrayList<String> result = new ArrayList<String>();
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				// Only retrieve the NodeValue if possible
				if (nodeList.item(i).getFirstChild() != null){
					result.add(nodeList.item(i).getFirstChild().getNodeValue());
				}
				else {
					result.add("null");
				}
			}

		return result;
	}	

	/**
	 * Retrieve the complete dataset
	 * 
	 * @return a list of all features in the dataset
	 * @throws XPathExpressionException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public List<String> getDataset() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {		
		List<String> result = new ArrayList<String>();
		result.addAll(getTitles());
		result.addAll(getAlternatives());
		result.addAll(getCreators());
		result.addAll(getSubjects());
		result.addAll(getDescriptions());
		result.addAll(getAbstracts());
		result.addAll(getPublishers());
		result.addAll(getContributors());
		result.addAll(getDates());
		result.addAll(getTypes());
		result.addAll(getExtents());
		result.addAll(getMediums());
		result.addAll(getOIIdentifiers());
		result.addAll(getLanguages());
		result.addAll(getReferences());
		result.addAll(getSpatials());
		result.addAll(getAttributionNames());
		result.addAll(getAttributionURLs());
		result.addAll(getLicenses());
		return result;
	}
	
	/**
	 * Get all titles. Can't be just retrieved using the title tag, since some videos have both an English and Dutch title, while some have only a Dutch or English title.
	 * 
	 * @return a list of all titles in the dataset
	 * @throws XPathExpressionException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */	
	public List<String> getTitles() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {
		List<String> result = new ArrayList<String>();
		NodeList nodeList = (NodeList) xPath.compile("OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']").evaluate(document, XPathConstants.NODESET);
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			NodeList metaData = nodeList.item(i).getChildNodes();
			String title = "";
			
			for (int j = 0; j < metaData.getLength(); j++) {
				String nodeName = metaData.item(j).getNodeName();
				if (nodeName.equals("oi:title") && title.equals("")) {
					title = metaData.item(j).getTextContent();
				}
				//For the case that there are an English and Dutch title and the English is mentioned first, still pick the Dutch
				else if (nodeName.equals("oi:title") && !(title.equals("")) && metaData.item(j).getAttributes().item(0).equals("xml:lang=\"nl\"")) {
					title = metaData.item(j).getTextContent();
				}
			}
			result.add(title);
		}
				
		return result;
	}
	
	/**
	 * Retrieves all Dutch alternatives
	 * 
	 * @return a list of all alternatives in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getAlternatives() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:alternative'][@lang='nl']");		
	}

	/**
	 * Retrieves all Dutch creators
	 * 
	 * @return a list of all creators in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getCreators() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:creator'][@lang='nl']");
	}
	
	/**
	 * Retrieves all Dutch subjects
	 * 
	 * @return a list of all subjects in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getSubjects() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:subject'][@lang='nl']");
	}
	
	/**
	 * Retrieves all Dutch descriptions
	 * 
	 * @return a list of all descriptions in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getDescriptions() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:description'][@lang='nl']");
	}
	
	/**
	 * Retrieves all Dutch abstracts
	 * 
	 * @return a list of all abstracts in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getAbstracts() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:abstract'][@lang='nl']");
	}
	
	/** 
	 * Retrieves all publishers 
	 * 
	 * @return a list of all publishers in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getPublishers() throws XPathExpressionException {
		List<String> result = new ArrayList<String>();
		NodeList nodeList = (NodeList) xPath.compile("OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']").evaluate(document, XPathConstants.NODESET);
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			NodeList metaData = nodeList.item(i).getChildNodes();
			String publisher = "";
			
			for (int j = 0; j < metaData.getLength(); j++) {
				String nodeName = metaData.item(j).getNodeName();
				if (nodeName.equals("oi:publisher") && publisher.equals("")) {
					publisher = metaData.item(j).getTextContent();
				}
				//For the case that there are an publisher with lang nl and one with no attributes, pick the one with lang nl. This is because the one without attributes contains a link.
				else if (nodeName.equals("oi:publisher") && !(publisher.equals(""))) {
					if (metaData.item(j).hasAttributes()) {
						if (metaData.item(j).getAttributes().item(0).equals("xml:lang=\"nl\""))
							publisher = metaData.item(j).getTextContent();
					}						
				}
			}
			result.add(publisher);
		}
				
		return result;
	}

	/**
	 * Retrieves all contributors
	 * 
	 * @return a list of all contributors in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getContributors() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:oi:contributor']");
	}

	/**
	 * Retrieves all dates
	 * 
	 * @return a list of all dates in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getDates() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:date']");
	}

	/**
	 * Retrieves all types, this is mostly 'moving image'
	 * 
	 * @return a list of all types in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getTypes() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:type']");
	}

	/**
	 * Retrieves all extents. Extents are the length of a video
	 * 
	 * @return a list of all extents in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getExtents() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:extent']");
	}

	/**
	 * Retrieves a link to all mpegs. Be aware that not every video has a mpeg link
	 * 
	 * @return a list of links to all mpegs in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getMediums() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:medium'][@format='intermediate']");
	}
	
	/**
	 * Retrieves for every video a link to a video format. HD formats are preferred. 
	 * 
	 * @return a list of links to video formats
	 * @throws XPathExpressionException
	 */
	public List<String> getUniqueMediums() throws XPathExpressionException {
		List<String> list = new ArrayList<String>();
		NodeList nodeList = (NodeList) xPath.compile("OAI-PMH/ListRecords/record").evaluate(document, XPathConstants.NODESET);
			
		for (int i = 0; i < nodeList.getLength(); i++) {
			String webm = "";
			String ogv_sd = "";
			String ogv_hd = "";
			String mp4_sd = "";
			String mp4_hd = "";
			String mpeg = "";
			String mpg = "";
			
			NodeList videoMetadata = nodeList.item(i).getChildNodes().item(3).getChildNodes().item(1).getChildNodes();
			for (int j = 0; j < videoMetadata.getLength(); j++) {
				if (videoMetadata.item(j).getNodeName().equals("oi:medium")) {
					String text = videoMetadata.item(j).getTextContent();
					if (text.contains(".webm")) {
						webm = text;
					}
					//For ogv and mp4 there are sd and hd formats
					else if (text.contains(".ogv")) {
						if (videoMetadata.item(j).getAttributes().item(0).equals("format=\"hd\""))
							ogv_hd = text;
						else
							ogv_sd = text;
					}
					else if (text.contains(".mp4")) {
						if (videoMetadata.item(j).getAttributes().item(0).equals("format=\"hd\""))
							mp4_hd = text;
						else
							mp4_sd = text;
					}
					else if (text.contains(".mpeg")) {
						mpeg = text;
					}
					else if (text.contains(".mpg")) {
						mpg = text;
					}
				}
			}
			
			if (!(mp4_hd.equals(""))) {
				list.add(mp4_hd);
			}
			else if (!(ogv_hd.equals(""))) {
				list.add(ogv_hd);
			}
			else if (!(mp4_sd.equals(""))) {
				list.add(mp4_sd);
			}
			else if (!(ogv_sd.equals(""))) {
				list.add(ogv_sd);
			}
			else if (!(mpeg.equals(""))) {
				list.add(mpeg);
			}
			else if (!(webm.equals(""))) {
				list.add(webm);
			}
			else if (!(mpg.equals(""))) {
				list.add(mpg);
			}
		}		
		
		return list;
	}

	/**
	 * Retrieves a list of OI_identifiers.
	 * 
	 * @return a list of all identifiers in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getOIIdentifiers() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:identifier']");
	}
	
	/**
	 * Retrieves a list of identifiers automatically created. This list contains a identifier for all videos. In the OIIdentifiers list not every video has a identifier.
	 * 
	 * @return a list of all identifiers in the dataset
	 * @throws XPathExpressionException 
	 */
	public List<String> getIdentifiers() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/header/identifier");
	}

	/**
	 * Retrieves a list of all languages for every video
	 * 
	 * @return a list of all languages for every video in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getLanguages() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:language']");
	}

	/**
	 * Retrieves a list of Dutch references
	 * 
	 * @return a list of references in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getReferences() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:references'][@lang='nl']");
	}

	/**
	 * Retrieves a list of locations in Dutch
	 * 
	 * @return a list of locations in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getSpatials() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:spatial'][@lang='nl']");
	}
	
	/**
	 * Retrieves all Dutch AttributionNames
	 * 
	 * @return a list of attributionNames in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getAttributionNames() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:attributionName'][@lang='nl']");
	}

	/**
	 * Retrieves all attributionURLs
	 * 
	 * @return a list of all attributionURLs in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getAttributionURLs() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:attributionURL']");
	}

	/**
	 * Retrieves all licenses for every video
	 * 
	 * @return a list of all licenses per video in the dataset
	 * @throws XPathExpressionException
	 */
	public List<String> getLicenses() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:license']");
	}
	
	/**
	 * Writes per video all the links to a video file to JSON
	 * 
	 * @throws XPathExpressionException
	 */
	private void toVideoIdJSON() throws XPathExpressionException {
		List<String> list = new ArrayList<String>();
		NodeList nodeList = (NodeList) xPath.compile("OAI-PMH/ListRecords/record").evaluate(document, XPathConstants.NODESET);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
				
		for (int i = 0; i < 500; i++) {
			Map<String,String> myJSON = new LinkedHashMap<String, String>();
			String id = nodeList.item(i).getChildNodes().item(1).getChildNodes().item(1).getTextContent();
			String webm = "";
			String ogv = "";
			String mp4 = "";
			String mpeg = "";
			String mpg = "";
			
			NodeList videoMetadata = nodeList.item(i).getChildNodes().item(3).getChildNodes().item(1).getChildNodes();
			for (int j = 0; j < videoMetadata.getLength(); j++) {
				if (videoMetadata.item(j).getNodeName().equals("oi:medium")) {
					String text = videoMetadata.item(j).getTextContent();
					if (text.contains(".webm")) {
						webm = text;
					}
					else if (text.contains(".ogv")) {
						ogv = text;
					}
					else if (text.contains(".mp4")) {
						mp4 = text;				
					}
					else if (text.contains(".mpeg")) {
						mpeg = text;
					}
					else if (text.contains(".mpg")) {
						mpg = text;
					}
				}
			}
			
			myJSON.put("videoId", id);	
			myJSON.put("webm", webm);
			myJSON.put("ogv", ogv);
			myJSON.put("mp4", mp4);
			myJSON.put("mpeg", mpeg);
			myJSON.put("mpg", mpg);
			
			list.add(gson.toJson(myJSON));
		}		
				
		System.out.println(list);
		
		try {
			FileWriter file = new FileWriter("dataset.json");
			file.write(list.toString());
			file.flush();
			file.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
