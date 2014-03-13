package com.google.api.services.samples.youtube.cmdline;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class Dataset {
	private static final String PATH = "D:/Dropbox/Public/Master Thesis/Dataset/dataset.xml";
	private static XPath xPath = XPathFactory.newInstance().newXPath();
	private static Document document;
	
	public static void main(String[] args) {
		//Dataset dataset = new Dataset();
		//dataset.getUniqueMedium();
		System.out.println("Hoi");
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
	
//	public List<String> getUniqueMedium() throws XPathExpressionException {
//		ArrayList<String> result = new ArrayList<String>();
//		NodeList nodeList = (NodeList) xPath.compile("OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']").evaluate(document, XPathConstants.NODESET);
//		System.out.println(nodeList.item(0).getChildNodes().item(1).getNodeName());
//		System.out.println(nodeList.item(0).getNodeValue());
//
//			for (int i = 0; i < nodeList.getLength(); i++) {
//				System.out.println(nodeList.item(i).getFirstChild());
//				if (nodeList.item(i).getFirstChild() != null){
//					result.add(nodeList.item(i).getFirstChild().getNodeValue());
//				}
//				else {
//					result.add("null");
//				}
//			}
//
//		return result;
//	}

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
	
	public List<String> getTitles() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:title'][@lang='nl']");
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
		return getNodes(document, "OAI-PMH/ListRecords/record/metadata//*[name()='oai_oi:oi']//*[name()='oi:publisher'][@lang='nl']");
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
}
