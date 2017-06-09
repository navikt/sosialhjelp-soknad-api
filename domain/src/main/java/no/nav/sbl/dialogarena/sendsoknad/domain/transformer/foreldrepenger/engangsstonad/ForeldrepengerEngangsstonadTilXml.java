package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
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
        SoeknadsskjemaEngangsstoenad engangsstonad = tilSoeknadsskjemaEngangsstoenad(webSoknad);
        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        JAXB.marshal(engangsstonad, xml);
        return new AlternativRepresentasjon()
                .medRepresentasjonsType(getRepresentasjonsType())
                .medMimetype("application/xml")
                .medFilnavn("Engangsstonad.xml")
                .medUuid(UUID.randomUUID().toString())
                .medContent(xml.toByteArray());
    }

    private SoeknadsskjemaEngangsstoenad tilSoeknadsskjemaEngangsstoenad(WebSoknad webSoknad) {
        return new SoeknadsskjemaEngangsstoenad()
                .withBruker(new AktoerTilXml().apply(webSoknad))
                .withRettigheter(new RettigheterTilXml().apply(webSoknad))
                .withTilknytningNorge(new TilknytningTilXml().apply(webSoknad))
                .withOpplysningerOmMor(new OpplysningerOmMorTilXml().apply(webSoknad))
                .withOpplysningerOmFar(new OpplysningerOmFarTilXml().apply(webSoknad))
                .withOpplysningerOmBarn(new OpplysningerOmBarnTilXml().apply(webSoknad))
                .withVedleggListes(new VedleggTilXml().apply(webSoknad));
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
