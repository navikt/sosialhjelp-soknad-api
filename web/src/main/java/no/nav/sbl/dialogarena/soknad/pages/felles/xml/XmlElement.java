package no.nav.sbl.dialogarena.soknad.pages.felles.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class XmlElement implements Serializable {

    private static final String KEY_TAG = "key";
    private static final String VALUE_TAG = "value";
    private static final String VISIBLE_TAG = "visible";
    private static final String MODIFIABLE_TAG = "modifiable";

    private Element elem;

    public XmlElement(Node node) {
        elem = (Element) node;
    }

    public String getKey() {
        return getTextValue(KEY_TAG);
    }

    public String getValue() {
        return getTextValue(VALUE_TAG);
    }

    public Boolean isVisible() {
        return getBooleanValue(VISIBLE_TAG);
    }

    public Boolean isModifiable() {
        return getBooleanValue(MODIFIABLE_TAG);
    }

    private Boolean getBooleanValue(String tag) {
        String value = getTextValue(tag);

        if (isNotBlank(value) && value.equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }

    private String getTextValue(String tag) {
        NodeList nodeList = elem.getElementsByTagName(tag);

        if (nodeList.getLength() > 0 && nodeList.item(0).hasChildNodes()) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }
}
