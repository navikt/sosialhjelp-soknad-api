package no.nav.sbl.dialogarena.soknad.convert.xml;

import java.util.List;

public class XmlSoknad extends XmlElement {


    private static final String SOKNAD_TAG = "soknadId";
    private static final String INPUT_FIELD_TAG = "inputField";

    public XmlSoknad(String xmlString) {
        super(xmlString);
    }

    public String getSoknadId() {
        return getString(SOKNAD_TAG);
    }

    public List<XmlInputElement> getInputNodes() {
        return XmlUtil.toList(xml.getElementsByTagName(INPUT_FIELD_TAG));
    }
}
