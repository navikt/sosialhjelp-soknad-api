package no.nav.sbl.dialogarena.print.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;



public class XMLTestData {
    private static final Logger LOG = LoggerFactory.getLogger(XMLTestData.class);

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
