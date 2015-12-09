package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;

import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

public class SoknadStrukturXsdGenerator {

    private String path = "src/main/resources/soknader/";
    private String filNavn = "soknadstruktur.xsd";

    /**
     * Ingen assertions, men genererer ny xsd basert
     * på SoknadStruktur og dens medlemmer
     */
    @Test
    public void genererSkjema() throws JAXBException, IOException {
        String skjemaStreng = lagSkjemaString();
        skjemaStreng = fiksStreng(skjemaStreng);
        skrivTilFil(skjemaStreng);
    }

    private String lagSkjemaString() throws JAXBException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance(SoknadStruktur.class);
        final StringWriter writer = new StringWriter();

        jaxbContext.generateSchema(new SchemaOutputResolver() {
            @Override
            public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                StreamResult result = new StreamResult(writer);
                result.setSystemId(suggestedFileName);
                return result;
            }
        });

        return writer.toString();
    }

    private String fiksStreng(String skjemaStreng) {
        return skjemaStreng.replace("<xs:element name=\"configuration\">", "<xs:element name=\"configuration\" minOccurs=\"0\">");
    }

    private void skrivTilFil(String skjemaStreng) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path + filNavn), "UTF-8");
        writer.write(skjemaStreng);
        writer.close();
    }

    public static void main(String[] args) throws JAXBException, IOException {
        new SoknadStrukturXsdGenerator().genererSkjema();
    }

}
