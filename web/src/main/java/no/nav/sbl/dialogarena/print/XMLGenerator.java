package no.nav.sbl.dialogarena.print;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;


public class XMLGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(XMLGenerator.class);

    public static Document createDocument(String cssFilePath) {
        Document doc = null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootElement = doc.createElement("people");
            doc.appendChild(rootElement);
            if(cssFilePath != null && !cssFilePath.isEmpty()) {
                doc.setXmlStandalone(true);
                ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/css\" href=\"" + cssFilePath + "\"");
                doc.insertBefore(pi, rootElement);
                System.out.println("Add CSS..." + cssFilePath);
                System.out.println("pi.getNodeValue() = " + pi.getNodeValue());
            }

            createPerson(doc, rootElement, "1", "Harald Olsen", "32");
            createPerson(doc, rootElement, "2", "Trine Person", "30");
            createPerson(doc, rootElement, "3", "Kjell Hansen", "2");

        } catch (ParserConfigurationException e) {
            LOG.info("Kunne ikke parse", e);
        }

        return doc;
    }

    public static void insertCSS() {

    }

    public static void createXMLFile(String filePath, Document doc) {
         try {
             Transformer transformer = TransformerFactory.newInstance().newTransformer();
             DOMSource domSource = new DOMSource(doc);
             StreamResult result = new StreamResult(new File(filePath));
             transformer.transform(domSource, result);
        } catch (TransformerException e) {
             LOG.info("Kunne ikke transformere", e);
        }
    }

    public static void createHTMLFile(String outputHTML, Document doc, String xsl) {
        try {
            Transformer transformer = getTransformer(xsl);
            DOMSource domSource = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(outputHTML));
            transformer.transform(domSource, result);
        } catch (TransformerException e) {
            LOG.info("Kunne ikke transformere", e);
        }
    }

    public static void createXMLFile(String xslPath, String inputFile, String outputPath) {
        try {
            Transformer transformer = getTransformer(xslPath);

            StreamSource xml = new StreamSource(new File(inputFile));
            StreamResult result = new StreamResult(new File(outputPath));
            transformer.transform(xml, result);
        } catch (TransformerException e) {
            LOG.info("Kunne ikke transformere", e);
        }
    }

    private static Transformer getTransformer(String xslPath) throws TransformerConfigurationException {
        TransformerFactory factory = TransformerFactory.newInstance();
        StreamSource xsl = new StreamSource(new File(xslPath));
        return factory.newTransformer(xsl);
    }


    public static void addXSLToDocument(String xslPath, String outputFile, Document doc) {
        try {
            Transformer transformer = getTransformer(xslPath);

            doc.setXmlStandalone(true);
            DOMSource domSource = new DOMSource(doc);

            StreamResult result = new StreamResult(new File(outputFile));
            transformer.transform(domSource, result);

        } catch (TransformerException e) {
            LOG.info("Kunne ikke transformere", e);
        }
    }

    private static void createPerson(Document doc, Element rootElement, String idValue, String nameValue, String ageValue) {
        Element person = doc.createElement("person");
        person.setAttribute("id", idValue);
        rootElement.appendChild(person);

        Element name=  doc.createElement("name");
        name.appendChild(doc.createTextNode(nameValue));
        person.appendChild(name);

        Element age=  doc.createElement("age");
        age.appendChild(doc.createTextNode(ageValue));
        person.appendChild(age);
    }
}
