package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;
import org.springframework.context.MessageSource;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class ForeldrepengerEngangsstonadTilXml implements AlternativRepresentasjonTransformer {

    private final MessageSource messageSource;

    public ForeldrepengerEngangsstonadTilXml(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public AlternativRepresentasjon transform(WebSoknad webSoknad) {
        SoeknadsskjemaEngangsstoenad engangsstonad = tilSoeknadsskjemaEngangsstoenad(webSoknad, messageSource);
        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        JAXB.marshal(engangsstonad, xml);
        return new AlternativRepresentasjon()
                .medRepresentasjonsType(getRepresentasjonsType())
                .medMimetype("application/xml")
                .medFilnavn("Engangsstonad.xml")
                .medUuid(UUID.randomUUID().toString())
                .medContent(xml.toByteArray());
    }

    private SoeknadsskjemaEngangsstoenad tilSoeknadsskjemaEngangsstoenad(WebSoknad webSoknad, MessageSource messageSource) {
        return new SoeknadsskjemaEngangsstoenad();
    }

    @Override
    public AlternativRepresentasjonType getRepresentasjonsType() {
        return AlternativRepresentasjonType.XML;
    }

    @Override
    public AlternativRepresentasjon apply(WebSoknad webSoknad) {
        return transform(webSoknad);
    }


}
