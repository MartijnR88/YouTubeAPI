package com.google.api.services.samples.youtube.cmdline;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

public class Dataset {
	private static final String PATH = "D:/Dropbox/Public/Master Thesis/Dataset/dataset.xml";
	private static XPath xPath = XPathFactory.newInstance().newXPath();
	private static Document document;
	
	public static void main(String[] args) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {
		Dataset dataset = new Dataset();
		dataset.getTitles();
	}
	
	public Dataset() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {
		loadDataset();
	}
	
	private static void loadDataset() throws SAXException, IOException, ParserConfigurationException, TransformerException, XPathExpressionException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(new FileInputStream(PATH));
	}

	private static List<String> getNodes(Document document, String expression) throws XPathExpressionException {
		ArrayList<String> result = new ArrayList<String>();
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
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
		result.addAll(getIdentifiers());
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
	 * @return
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
	
	public List<String> getAlternatives() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:alternative'][@lang='nl']");		
	}

	public List<String> getCreators() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:creator'][@lang='nl']");
	}
	
	public List<String> getSubjects() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:subject'][@lang='nl']");
	}
	
	public List<String> getDescriptions() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:description'][@lang='nl']");
	}
	
	public List<String> getAbstracts() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:abstract'][@lang='nl']");
	}
	
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
				//For the case that there are an publisher with lang nl and one with no attributes, pick the one with lang nl
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

	public List<String> getContributors() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:oi:contributor']");
	}

	public List<String> getDates() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:date']");
	}

	public List<String> getTypes() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:type']");
	}

	public List<String> getExtents() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:extent']");
	}

	public List<String> getMediums() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:medium'][@format='intermediate']");
	}
	
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

	public List<String> getIdentifiers() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:identifier']");
	}

	public List<String> getLanguages() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:language']");
	}

	public List<String> getReferences() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:references'][@lang='nl']");
	}

	public List<String> getSpatials() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:spatial'][@lang='nl']");
	}

	public List<String> getAttributionNames() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:attributionName'][@lang='nl']");
	}

	public List<String> getAttributionURLs() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:attributionURL']");
	}

	public List<String> getLicenses() throws XPathExpressionException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:license']");
	}
	
	private void toVideoIdJSON() throws XPathExpressionException {
		List<String> list = new ArrayList<String>();
		NodeList nodeList = (NodeList) xPath.compile("OAI-PMH/ListRecords/record").evaluate(document, XPathConstants.NODESET);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
				
		for (int i = 0; i < nodeList.getLength(); i++) {
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
