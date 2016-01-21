package no.nav.sbl.dialogarena.sendsoknad.domain.oppsett;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoknadStrukturXsdGenerator {

    private String path = "src/main/resources/soknader/";
    private String filNavn = "soknadstruktur.xsd";

    public static final Pattern SOKNAD_SEQUENCE_PATTERN = Pattern.compile("<xs:complexType name=\"soknadStruktur\">(\\s+)<xs:sequence>(.*?)</xs:sequence>", Pattern.DOTALL);

    /**
     * Ingen assertions, men genererer ny xsd basert
     * på SoknadStruktur og dens medlemmer
     */
    @Test
    public void genererSkjema() throws JAXBException, IOException {
        String skjemaStreng = lagSkjemaString();
        skjemaStreng = fiksTingDerViErUenigMedJaxbMenFortsattVilHaTingAutogenerert(skjemaStreng);
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

    private String fiksTingDerViErUenigMedJaxbMenFortsattVilHaTingAutogenerert(String skjemaStreng) {
        String fiksetSkjema = gjorConfigOptional(skjemaStreng);
        fiksetSkjema = gjorAtSoknadElementerKanLiggeIVilkarligRekkefolge(fiksetSkjema);
        fiksetSkjema = tvingConstraintFaktumTilAVaereDefinert(fiksetSkjema);
        fiksetSkjema = endreLineEndingsFraJaxb(fiksetSkjema);

        return fiksetSkjema;
    }

    private String gjorConfigOptional(String skjemaStreng) {
        return skjemaStreng.replace("<xs:element name=\"configuration\">", "<xs:element name=\"configuration\" minOccurs=\"0\">");
    }

    private String gjorAtSoknadElementerKanLiggeIVilkarligRekkefolge(String skjemaStreng) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = SOKNAD_SEQUENCE_PATTERN.matcher(skjemaStreng);
        matcher.find();

        matcher.appendReplacement(sb, "<xs:complexType name=\"soknadStruktur\">" + matcher.group(1) + "<xs:choice maxOccurs=\"unbounded\">" + matcher.group(2) + "</xs:choice>");
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String tvingConstraintFaktumTilAVaereDefinert(String skjemaStreng) {
        return skjemaStreng.replace("<xs:element name=\"faktum\" type=\"xs:string\" minOccurs=\"0\"/>", "<xs:element name=\"faktum\" type=\"xs:IDREF\" minOccurs=\"0\"/>");
    }

    private String endreLineEndingsFraJaxb(String skjemaStreng) {
        return skjemaStreng.replace("\n", System.lineSeparator());
    }

    private void skrivTilFil(String skjemaStreng) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path + filNavn), "UTF-8");
        writer.write(skjemaStreng);
        writer.close();
    }

}
