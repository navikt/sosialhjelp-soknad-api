package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;
import org.springframework.context.MessageSource;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class ForeldrepengerEngangsstonadTilXml implements Transformer<WebSoknad, AlternativRepresentasjon> {

    private final MessageSource messageSource;

    public ForeldrepengerEngangsstonadTilXml(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public AlternativRepresentasjon transform(WebSoknad webSoknad) {
        SoeknadsskjemaEngangsstoenad engangsstonad = tilSoeknadsskjemaEngangsstoenad(webSoknad, messageSource);
        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        JAXB.marshal(engangsstonad, xml);
        return new AlternativRepresentasjon()
                .medMimetype("application/xml")
                .medFilnavn("Engangsstonad.xml")
                .medUuid(UUID.randomUUID().toString())
                .medContent(xml.toByteArray());
    }

    private SoeknadsskjemaEngangsstoenad tilSoeknadsskjemaEngangsstoenad(WebSoknad webSoknad, MessageSource messageSource) {
        return new SoeknadsskjemaEngangsstoenad();
    }
}
