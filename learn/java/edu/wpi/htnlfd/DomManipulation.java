package edu.wpi.htnlfd;

import edu.wpi.htnlfd.model.*;
import edu.wpi.htnlfd.question.AskQuestion;
import org.w3c.dom.*;
import java.io.*;
import java.util.Map.Entry;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class DomManipulation {

	public DocumentBuilderFactory factory;

	private DocumentBuilder builder;

	private Document document;

	private Document documentTell;

	/**
	 * Instantiates a new dom manipulation.
	 */
	public DomManipulation() {
		factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);

		try {
			builder = factory.newDocumentBuilder();
			// document = builder.newDocument();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(
					"An error occured while creating the document builder", e);
		}
	}

	/**
	 * Builds the dom.
	 */
	public Node buildDOM(TaskModel taskmodel) {
		return taskmodel.toNode(document);
	}

	/**
	 * Writes dom to file.
	 * 
	 * @param askQuestion
	 */
	public void writeDOM(String fileName, TaskModel taskmodel,
			AskQuestion askQuestion) throws Exception {

		// Writing document into xml file
		document = builder.newDocument();
		documentTell = builder.newDocument();
		DOMSource domSource = new DOMSource(document);
		DOMSource domSourceTell = new DOMSource(documentTell);
		File demonstrationFile = new File(fileName + ".xml");
		File tellFile = new File(askQuestion.filename + ".xml");
		if (!demonstrationFile.exists())
			demonstrationFile.createNewFile();

		if (!tellFile.exists())
			tellFile.createNewFile();

		try (FileOutputStream fileOutputStream = new FileOutputStream(
				demonstrationFile, false)) {

			StreamResult streamResult = new StreamResult(fileOutputStream);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();

			buildDOM(taskmodel);
			writeProperties(fileName, TaskModel.properties);

			// Adding indentation and omitting xml declaration
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
			transformer.transform(domSource, streamResult);

		} catch (Exception e) {

			throw e;
		}

		try (FileOutputStream fileOutputStream = new FileOutputStream(tellFile,
				false)) {

			StreamResult streamResult = new StreamResult(fileOutputStream);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();

			askQuestion.toNode(taskmodel, documentTell);
			writeProperties(askQuestion.filename, AskQuestion.properties); // /???
			// Adding indentation and omitting xml declaration
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
			transformer.transform(domSourceTell, streamResult);

		} catch (Exception e) {

			throw e;
		}

	}

	/**
	 * Writes dom to specified stream.
	 * 
	 * @throws IOException
	 */
	public void writeDOM(PrintStream stream, TaskModel taskmodel)
			throws TransformerException, IOException {
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer;

			transformer = tf.newTransformer();

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");

			Document doc = builder.newDocument();
			DOMSource domSource = new DOMSource(doc);
			taskmodel.toNode(doc);

			writeProperties("models\\Tell", AskQuestion.properties); // /???

			transformer.transform(domSource, new StreamResult(stream));
		} catch (TransformerConfigurationException e) {
			throw e;
		} catch (TransformerException e) {
			throw e;
		}

	}

	/**
	 * Write properties file.
	 */
	public void writeProperties(String fileName, Properties properties)
			throws IOException {
		File demonstrationFile = new File(fileName + ".properties");
		if (!demonstrationFile.exists())
			demonstrationFile.createNewFile();

		try (FileOutputStream fileOutputStream = new FileOutputStream(
				demonstrationFile, false)) {
			for (Entry<Object, Object> property : properties.entrySet()) {
				byte[] contentInBytes = ((String) property.getKey() + " = "
						+ (String) property.getValue() + "\n").getBytes();
				fileOutputStream.write(contentInBytes);
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}

}
