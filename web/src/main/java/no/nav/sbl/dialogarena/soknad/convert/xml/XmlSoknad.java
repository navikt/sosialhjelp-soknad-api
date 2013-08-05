package no.nav.sbl.dialogarena.soknad.convert.xml;

import no.nav.sbl.dialogarena.soknad.convert.Soknad;
import org.w3c.dom.Element;

import java.util.Map;

public class XmlSoknad extends XmlElement implements Soknad {


    private static final String SOKNAD_TAG = "soknadId";
    private static final String NAV_GOSYS_ID_TAG = "soknadGosysId";
    private static final String INPUT_TAG = "input";
    private static final String INPUT_FIELD_TAG = "inputField";

    private Map<String, String> inputFields;

    public XmlSoknad(String xmlString) {
        super(xmlString);
        inputFields = XmlUtil.toMap(((Element)xml.getElementsByTagName(INPUT_TAG).item(0)).getElementsByTagName(INPUT_FIELD_TAG));
    }

    public String getSoknadGosysId() {
        return getString(NAV_GOSYS_ID_TAG);
    }

    public Long getSoknadId() {
        return getLong(SOKNAD_TAG);
    }

    public String getValue(String key) {
        if (inputFields.containsKey(key)) {
            return inputFields.get(key);
        }
        return "";
    }
}
