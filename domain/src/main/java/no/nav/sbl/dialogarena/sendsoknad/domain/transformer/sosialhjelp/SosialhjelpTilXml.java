package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLSoknadsosialhjelp;
import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AlleredeHandtertException;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

public class SosialhjelpTilXml implements AlternativRepresentasjonTransformer {

    private final MessageSource messageSource;
    private static final Logger logger = getLogger(SosialhjelpTilXml.class);

    public SosialhjelpTilXml(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public AlternativRepresentasjon transform(WebSoknad webSoknad) {
        XMLSoknadsosialhjelp sosialhjelp = tilSoeknadsskjemaSosialhjelp(webSoknad);
        if (brukerErPaaOppsummeringssiden(webSoknad)) {
            validerSkjema(sosialhjelp, webSoknad);
        }

        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        JAXB.marshal(sosialhjelp, xml);
        return new AlternativRepresentasjon()
                .medRepresentasjonsType(getRepresentasjonsType())
                .medMimetype("application/xml")
                .medFilnavn("Sosialhjelp.xml")
                .medUuid(UUID.randomUUID().toString())
                .medContent(xml.toByteArray());
    }

    private boolean brukerErPaaOppsummeringssiden(WebSoknad soknad) {
        return soknad.getDelstegStatus() == DelstegStatus.VEDLEGG_VALIDERT;
    }

    private XMLSoknadsosialhjelp tilSoeknadsskjemaSosialhjelp(WebSoknad webSoknad) {
        XMLSoknadsosialhjelp soknadSosialStonad = new XMLSoknadsosialhjelp();

        return soknadSosialStonad
                .withVersion("1")
                .withPersonalia(new BrukerTilXml().apply(webSoknad))
                .withArbeidUtdanning(new ArbeidOgUtdanningTilXml().apply(webSoknad))
                .withFamiliesituasjon(new FamiliesituasjonTilXml().apply(webSoknad))
                .withBegrunnelse(new BegrunnelseTilXml().apply(webSoknad))
                .withBosituasjon(new BosituasjonTilXml().apply(webSoknad))
                .withInntektFormue(new InntektFormueTilXml().apply(webSoknad))
                .withUtgifterGjeld(new UtgifterGjeldTilXml().apply(webSoknad));
        // TODO legge til inntekter / utgifter
    }

    @Override
    public AlternativRepresentasjonType getRepresentasjonsType() {
        return AlternativRepresentasjonType.XML;
    }

    @Override
    public AlternativRepresentasjon apply(WebSoknad webSoknad) {
        return transform(webSoknad);
    }

    public void validerSkjema(XMLSoknadsosialhjelp sosialStonadSkjema, WebSoknad soknad) {
        QName qname = new QName("http://nav.no/melding/domene/brukerdialog/soeknadsskjemasosialhjelp/v1", "soknadsosialhjelp");
        JAXBElement<XMLSoknadsosialhjelp> skjema = new JAXBElement<>(qname, XMLSoknadsosialhjelp.class, sosialStonadSkjema);
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(
                    new StreamSource(
                            SosialhjelpTilXml.class
                                    .getResourceAsStream("/xsd/sosialhjelp.xsd")
                    )
            );
            Marshaller marshaller = JAXBContext.newInstance(XMLSoknadsosialhjelp.class).createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setSchema(schema);
            Validator validator = schema.newValidator();
            JAXBSource source = new JAXBSource(marshaller, skjema);
            validator.validate(source);
        } catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JAXB.marshal(skjema, baos);
            logger.error("Validering av skjema feilet: " + e + ". Xml: " + baos.toString(), e);
            Event event = MetricsFactory.createEvent("soknad.xmlrepresentasjon.valideringsfeil");
            event.addTagToReport("soknad.xmlrepresentasjon.valideringsfeil.skjemanummer", soknad.getskjemaNummer());
            event.report();
            throw new AlleredeHandtertException();
        }

    }

}