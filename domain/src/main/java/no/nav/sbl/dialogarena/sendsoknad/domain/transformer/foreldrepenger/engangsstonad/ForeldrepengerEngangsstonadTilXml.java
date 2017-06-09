package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
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

public class ForeldrepengerEngangsstonadTilXml implements AlternativRepresentasjonTransformer {

    private final MessageSource messageSource;
    private static final Logger logger = getLogger(ForeldrepengerEngangsstonadTilXml.class);

    public ForeldrepengerEngangsstonadTilXml(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public AlternativRepresentasjon transform(WebSoknad webSoknad) {
        SoeknadsskjemaEngangsstoenad engangsstonad = tilSoeknadsskjemaEngangsstoenad(webSoknad);
        if(brukerErPaaOppsummeringssiden(webSoknad)){
            validerSkjema(engangsstonad,webSoknad);
        }

        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        JAXB.marshal(engangsstonad, xml);
        return new AlternativRepresentasjon()
                .medRepresentasjonsType(getRepresentasjonsType())
                .medMimetype("application/xml")
                .medFilnavn("Engangsstonad.xml")
                .medUuid(UUID.randomUUID().toString())
                .medContent(xml.toByteArray());
    }

    private boolean brukerErPaaOppsummeringssiden(WebSoknad soknad){
        return soknad.getDelstegStatus() == DelstegStatus.VEDLEGG_VALIDERT;
    }

    private SoeknadsskjemaEngangsstoenad tilSoeknadsskjemaEngangsstoenad(WebSoknad webSoknad) {
        SoeknadsskjemaEngangsstoenad soeknadsskjemaEngangsstoenad = new SoeknadsskjemaEngangsstoenad();
        String tilleggsopplysninger = webSoknad.getValueForFaktum("tilleggsopplysninger.fritekst");
        if (!tilleggsopplysninger.equals("")) {
            soeknadsskjemaEngangsstoenad.withTilleggsopplysninger(tilleggsopplysninger);
        }
        return soeknadsskjemaEngangsstoenad
                .withBruker(new BrukerTilXml().apply(webSoknad))
                .withRettigheter(new RettigheterTilXml().apply(webSoknad))
                .withTilknytningNorge(new TilknytningTilXml().apply(webSoknad))
                .withOpplysningerOmMor(new OpplysningerOmMorTilXml().apply(webSoknad))
                .withOpplysningerOmFar(new OpplysningerOmFarTilXml().apply(webSoknad))
                .withOpplysningerOmBarn(new OpplysningerOmBarnTilXml().apply(webSoknad))
                .withVedleggListes(new VedleggTilXml().apply(webSoknad))
                .withSoknadsvalg(new SoknadsvalgTilXml().apply(webSoknad));
    }

    @Override
    public AlternativRepresentasjonType getRepresentasjonsType() {
        return AlternativRepresentasjonType.XML;
    }

    @Override
    public AlternativRepresentasjon apply(WebSoknad webSoknad) {
        return transform(webSoknad);
    }

    public void validerSkjema(SoeknadsskjemaEngangsstoenad engangsstoenadSkjema, WebSoknad soknad) {
        QName qname = new QName("http://nav.no/melding/virksomhet/soeknadsskjemaEngangsstoenad/v1", "soeknadsskjemaengangsstoenad");
        JAXBElement<SoeknadsskjemaEngangsstoenad> skjema = new JAXBElement<>(qname, SoeknadsskjemaEngangsstoenad.class, engangsstoenadSkjema);
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(
                            new StreamSource(
                                    ForeldrepengerEngangsstonadTilXml.class
                                            .getResourceAsStream("/tjenestespesifikasjon/no/nav/melding/virksomhet/soeknadsskjemaEngangsstoenad/v1/v1.xsd")
                            )
            );
            Marshaller marshaller = JAXBContext.newInstance(SoeknadsskjemaEngangsstoenad.class).createMarshaller();
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
            event.addTagToReport("soknad.xmlrepresentasjon.valideringsfeil.skjemanummer",soknad.getskjemaNummer());
            event.report();
            throw new AlleredeHandtertException();
        }

    }

}
