package no.nav.sbl.dialogarena.soknad.pages.felles.xml;

import no.nav.modig.core.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.List;

public class XmlParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlParser.class);
    private static final String SOKNAD_TAG = "soknadId";
    private static final String INPUT_FIELD_TAG = "inputField";
    private Element xml;

    public XmlParser(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xmlDoc = builder.parse(new ByteArrayInputStream(xmlString.getBytes()));
            xml = xmlDoc.getDocumentElement();
            xml.normalize();
        } catch (Exception e) {
            LOGGER.error("Kunne ikke opprette JSON-objekt", e);
            throw new ApplicationException("Kunne ikke bygge søknaden", e);
        }
    }

    public String getSoknadId() {
        return getTextValue(SOKNAD_TAG);
    }

    public List<XmlElement> getInputNodes() {
        return XmlUtil.list(xml.getElementsByTagName(INPUT_FIELD_TAG));
    }

    private Element getNodeElement(String tag, int idx) {
        NodeList nodes = xml.getElementsByTagName(tag);

        if (nodes.getLength() > idx) {
            return (Element) nodes.item(idx);
        }
        return null;
    }

    private String getTextValue(String tag) {
        Element nodeElement = getNodeElement(tag, 0);
        if (nodeElement.hasChildNodes()) {
            return nodeElement.getFirstChild().getNodeValue();
        }
        return "";
    }
}
