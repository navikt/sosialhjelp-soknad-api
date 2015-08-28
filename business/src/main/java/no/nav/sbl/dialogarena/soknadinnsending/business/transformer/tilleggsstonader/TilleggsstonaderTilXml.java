package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Aktivitetsinformasjon;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Reiseutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Rettighetstype;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Tilleggsstoenadsskjema;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;
import org.slf4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.extractValue;
import static org.slf4j.LoggerFactory.getLogger;

public class TilleggsstonaderTilXml implements Transformer<WebSoknad, AlternativRepresentasjon> {

    private static final Logger logger = getLogger(TilleggsstonaderTilXml.class);

    @Override
    public AlternativRepresentasjon transform(WebSoknad webSoknad) {
        Tilleggsstoenadsskjema tilleggsstoenadsskjema = tilTilleggsstoenadSkjema(webSoknad);
        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        try {
            JAXBElement tilleggsstonadsskjemaElement = new JAXBElement(new QName("tilleggsstonadsskjema"), Tilleggsstoenadsskjema.class, tilleggsstoenadsskjema);
            JAXBContext.newInstance(Tilleggsstoenadsskjema.class, Rettighetstype.class).createMarshaller().marshal(tilleggsstonadsskjemaElement, xml);
        } catch (JAXBException e) {
            logger.error("Klarte ikke konvertere tilleggsstonadsskjema til xml", e);
            throw new RuntimeException("Klarte ikke konvertere tilleggsstonadsskjema til xml", e);
        }

        return new AlternativRepresentasjon()
                .medMimetype("application/xml")
                .medFilnavn("Tilleggsstonader.xml")
                .medUuid(UUID.randomUUID().toString())
                .medContent(xml.toByteArray());
    }

    private static Tilleggsstoenadsskjema tilTilleggsstoenadSkjema(WebSoknad webSoknad) {
        Tilleggsstoenadsskjema skjema = new Tilleggsstoenadsskjema();
        skjema.setMaalgruppeinformasjon(new MaalgruppeTilXml().transform(webSoknad.getFaktumMedKey("maalgruppe")));
        Rettighetstype rettighetstype = new Rettighetstype();
        if (aktivBolk("bostotte", webSoknad)) {
            rettighetstype.setBoutgifter(new BoutgifterTilXml().transform(webSoknad));
        }
        if (aktivBolk("laermidler", webSoknad)) {
            rettighetstype.setLaeremiddelutgifter(new LaeremidlerTilXml().transform(webSoknad));
        }

        rettighetstype.setReiseutgifter(reiseutgifter(webSoknad));
        skjema.setRettighetstype(rettighetstype);
        skjema.setAktivitetsinformasjon(aktivitetsInformasjon(webSoknad));
        return skjema;
    }

    private static Aktivitetsinformasjon aktivitetsInformasjon(WebSoknad webSoknad) {
        Aktivitetsinformasjon result = new Aktivitetsinformasjon();
        result.setAktivitetsId(extractValue(webSoknad.getFaktumMedKey("aktivitet"), String.class, "id"));
        return result;
    }

    private static boolean aktivBolk(String bolk, WebSoknad webSoknad) {
        Faktum bolkFaktum = webSoknad.getFaktumMedKey("informasjonsside.stonad." + bolk);
        return bolkFaktum != null && "true".equals(bolkFaktum.getValue());
    }

    private static Reiseutgifter reiseutgifter(WebSoknad webSoknad) {
        Reiseutgifter reiseutgifter = new Reiseutgifter();
        if (aktivBolk("reiseaktivitet", webSoknad)) {
            reiseutgifter.setDagligReise(new DagligReiseTilXml().transform(webSoknad));
        }
        if (aktivBolk("reisearbeidssoker", webSoknad)) {
            reiseutgifter.setReisestoenadForArbeidssoeker(new ArbeidReiseTilXml().transform(webSoknad));
        }
        if (aktivBolk("reisemidlertidig", webSoknad)) {
            reiseutgifter.setReiseVedOppstartOgAvsluttetAktivitet(new ReiseOppstartOgAvsluttetAktivitetTilXml().transform(webSoknad));
        }
        if(aktivBolk("reisesamling", webSoknad) ) {
            reiseutgifter.setReiseObligatoriskSamling(new SamlingReiseTilXml().transform(webSoknad));
        }
        return reiseutgifter;
    }
}
