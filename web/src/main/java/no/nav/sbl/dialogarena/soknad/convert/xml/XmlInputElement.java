package no.nav.sbl.dialogarena.soknad.convert.xml;

import no.nav.sbl.dialogarena.soknad.convert.InputElement;
import org.w3c.dom.Node;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class XmlInputElement extends XmlElement implements InputElement {

    private static final String KEY_TAG = "key";
    private static final String VALUE_TAG = "value";
    private static final String VISIBLE_TAG = "visible";
    private static final String MODIFIABLE_TAG = "modifiable";
    private static final String TYPE_TAG = "type";
    private static final String DEFAULT_TYPE = "text";

    public XmlInputElement(Node node) {
        super(node);
    }

    public String getKey() {
        return getString(KEY_TAG);
    }

    public String getValue() {
        return getString(VALUE_TAG);
    }

    public String getType() {
        String type = getString(TYPE_TAG);

        if (isBlank(type)) {
            type = DEFAULT_TYPE;
        }
        return type;
    }

    public Boolean isVisible() {
        return getBoolean(VISIBLE_TAG);
    }

    public Boolean isModifiable() {
        return getBoolean(MODIFIABLE_TAG);
    }
}
