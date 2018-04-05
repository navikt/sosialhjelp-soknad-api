package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.dagpenger.ordinaer;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static java.util.stream.Collectors.joining;
import static org.slf4j.LoggerFactory.getLogger;

public class DagpengerOrdinaerTilXml implements AlternativRepresentasjonTransformer {

    private final MessageSource messageSource;
    private static final Logger logger = getLogger(DagpengerOrdinaerTilXml.class);

    public DagpengerOrdinaerTilXml(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public AlternativRepresentasjonType getRepresentasjonsType() {
        return AlternativRepresentasjonType.XML;
    }

    @Override
    public AlternativRepresentasjon apply(WebSoknad webSoknad) {
        return transform(webSoknad);
    }

    private AlternativRepresentasjon transform(WebSoknad webSoknad) {

        String alternativRepresentasjon = new StringBuilder("Dette er en alternativ representasjon av dagpengesøknad\n")
                .append("Bruker behandlingsid: " + webSoknad.getBrukerBehandlingId())
                .append("\nAntall fakta: " + webSoknad.antallFakta())
                .append("\nEr dagpengesøknad? " + webSoknad.erDagpengeSoknad())
                .append("\nEr ordinær dagpengesøknad? " + webSoknad.erOrdinaerDagpengeSoknad())
                .append("\nStatus: " + webSoknad.getStatus())
                .append("Fakta: ")
                .append(webSoknad
                        .getFakta()
                        .stream()
                        .map(f -> f.getKey() + ": " + f.getValue())
                        .collect(joining("\n"))).toString();

        //SoeknadsskjemaEngangsstoenad engangsstonad = tilSoeknadsskjemaEngangsstoenad(webSoknad);
        //if(brukerErPaaOppsummeringssiden(webSoknad)){
        //    validerSkjema(engangsstonad,webSoknad);
        //}

        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        JAXB.marshal(alternativRepresentasjon, xml);
        return new AlternativRepresentasjon()
                .medRepresentasjonsType(getRepresentasjonsType())
                .medMimetype("application/xml")
                .medFilnavn("DagpengerOrinaer.xml")
                .medUuid(UUID.randomUUID().toString())
                .medContent(xml.toByteArray());
    }
}
