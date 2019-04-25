package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class XmlServiceTest {


    @Test
    public void fjernerXmlHeadersFraInkludertFil() throws IOException {
        sammenlign("xmlinkluder/fjernxmlheaders/", "root.xml", "fasit.xml");
    }

    @Test
    public void drarUtInnholdOmInkludertFilInneholderSoknadElement() throws IOException {
        sammenlign("xmlinkluder/drautinnhold/", "root.xml", "fasit.xml");
    }

    @Test
    public void inkludererRekursivtMedForskjelligeMapper() throws IOException {
        sammenlign("xmlinkluder/rekursiv/", "root.xml", "fasit.xml");
    }

    private void sammenlign(String filsti, String filnavn, String fasitFil) throws IOException {
        XmlService xmlService = new XmlService();
        String xml = xmlService.lastXmlFilMedInclude(filsti, filnavn);

        String fasitXml = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(filsti + fasitFil));

        assertEquals(fjernWhitespace(fasitXml), fjernWhitespace(xml));
    }

    private String fjernWhitespace(String xml) {
        return xml.replaceAll(">\\s*<", "><");
    }

}