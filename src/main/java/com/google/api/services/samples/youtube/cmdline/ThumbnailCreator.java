package com.google.api.services.samples.youtube.cmdline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

public class ThumbnailCreator {
	public static void main(String[] args) throws IOException,
			XPathExpressionException, SAXException,
			ParserConfigurationException, TransformerException {
		ThumbnailCreator obj = new ThumbnailCreator();
		Dataset d = new Dataset();
		for (String id : d.getIdentifiers()) {
			System.out.println(id);
			id.replace(':', ' ');
			id = id.replaceAll("\\s+", "");
			String command = "c:/ffmpeg/bin/ffmpeg.exe -i D:/workspace/OIP/data/files/BG/" + id + ".mp4 -ss 00:00:03.435 -f image2 -vframes 1 D:/workspace/OIP/data/workdir_thumbnails/" + id + ".png";
			String output = obj.executeCommand(command);
			System.out.println(output);
			//The time used in OIP:
			//String.format(Locale.US, "%.2f", node.getDoubleValue("time") / 1000);
		}
	}

	private String executeCommand(String command) throws IOException {
		StringBuffer output = new StringBuffer();

		Process p;

		try {
			p = Runtime.getRuntime().exec(command);
//			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				output.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();
	}
}
