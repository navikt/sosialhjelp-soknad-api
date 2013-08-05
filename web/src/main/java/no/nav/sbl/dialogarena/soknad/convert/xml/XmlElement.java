package no.nav.sbl.dialogarena.soknad.convert.xml;

import no.nav.modig.core.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.Serializable;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class XmlElement implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlElement.class);
    protected Element xml;

    public XmlElement(Node node) {
        xml = (Element) node;
    }

    public XmlElement(String xmlString) {
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

    protected Long getLong(String tag) {
        String value = getString(tag);

        if (isNotBlank(value)) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                LOGGER.error("Kunne ikke parse {} til en long", value);
                throw new ApplicationException("Feil i søknadsstrukturen", e);
            }
        }
        return 0L;
    }

    protected Boolean getBoolean(String tag) {
        String value = getString(tag);

        if (isNotBlank(value) && value.equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }

    protected String getString(String tag) {
        NodeList nodeList = xml.getElementsByTagName(tag);

        if (nodeList.getLength() > 0 && nodeList.item(0).hasChildNodes()) {
            return nodeList.item(0).getTextContent().trim();
        }
        return "";
    }
}
