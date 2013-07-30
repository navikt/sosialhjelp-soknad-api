package no.nav.sbl.dialogarena.soknad.convert.xml;

import no.nav.sbl.dialogarena.soknad.convert.InputElement;
import org.w3c.dom.Node;

public class XmlInputElement extends XmlElement implements InputElement {

    private static final String KEY_TAG = "key";
    private static final String VALUE_TAG = "value";
    private static final String VISIBLE_TAG = "visible";
    private static final String MODIFIABLE_TAG = "modifiable";

    public XmlInputElement(Node node) {
        super(node);
    }

    public String getKey() {
        return getString(KEY_TAG);
    }

    public String getValue() {
        return getString(VALUE_TAG);
    }

    public Boolean isVisible() {
        return getBoolean(VISIBLE_TAG);
    }

    public Boolean isModifiable() {
        return getBoolean(MODIFIABLE_TAG);
    }
}
