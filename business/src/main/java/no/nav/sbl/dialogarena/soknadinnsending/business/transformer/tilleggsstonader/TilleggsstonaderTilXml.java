package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Rettighetstype;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Tilleggsstoenadsskjema;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;

import javax.xml.bind.JAXB;
import java.io.StringWriter;
import java.util.UUID;

public class TilleggsstonaderTilXml implements Transformer<WebSoknad, AlternativRepresentasjon> {

    @Override
    public AlternativRepresentasjon transform(WebSoknad webSoknad) {
        Tilleggsstoenadsskjema tilleggsstoenadsskjema = tilTilleggsstoenadSkjema(webSoknad);
        StringWriter xml = new StringWriter();
        JAXB.marshal(tilleggsstoenadsskjema, xml);

        return new AlternativRepresentasjon()
                .medMimetype("application/xml")
                .medFilnavn("Tilleggsstonader.xml")
                .medUuid(UUID.randomUUID().toString())
                .medContent(xml.toString().getBytes());
    }

    private static Tilleggsstoenadsskjema tilTilleggsstoenadSkjema(WebSoknad webSoknad) {
        Tilleggsstoenadsskjema skjema = new Tilleggsstoenadsskjema();
        skjema.setMaalgruppeinformasjon(new MaalgruppeTilXml().transform(webSoknad.getFaktumMedKey("maalgruppe")));
        Rettighetstype rettighetstype = new Rettighetstype();
        rettighetstype.setBoutgifter(new BoutgifterTilXml().transform(webSoknad));
        skjema.setRettighetstype(rettighetstype);
        return skjema;
    }
}
