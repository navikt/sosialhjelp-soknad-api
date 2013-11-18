package no.nav.sbl.dialogarena.print;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class XMLGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(XMLGenerator.class);


    public static void transformToHTML(String outputHTML, Document doc, String xsl) {
        try {
            Transformer transformer = getTransformer(xsl);
            DOMSource domSource = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(outputHTML));
            transformer.transform(domSource, result);
        } catch (TransformerException e) {
            LOG.info("Kunne ikke transformere", e);
        }
    }

    public static String transformToHTML(Document doc, String xsl) throws UnsupportedEncodingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Transformer transformer = getTransformer(xsl);
            DOMSource domSource = new DOMSource(doc);
            StreamResult result = new StreamResult(out);
            transformer.transform(domSource, result);
        } catch (TransformerException e) {
            LOG.info("Kunne ikke transformere", e);
        }
        String str = out.toString("UTF-8");
        try {
            out.close();
        } catch (IOException e) {
            LOG.info("Kunne ikke lukke stream", e);
        }
        return str;
    }

    private static Transformer getTransformer(String xslPath) throws TransformerConfigurationException {
        TransformerFactory factory = TransformerFactory.newInstance();
        StreamSource domSource = new StreamSource(new File(xslPath));
        return factory.newTransformer(domSource);
    }

}
