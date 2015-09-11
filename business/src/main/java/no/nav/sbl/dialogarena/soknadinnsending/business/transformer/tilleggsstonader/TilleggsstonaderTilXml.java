package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Aktivitetsinformasjon;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Reiseutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Rettighetstype;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Tilleggsstoenadsskjema;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Tilsynsutgifter;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.collections15.Transformer;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.UUID;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTransformers.extractValue;

public class TilleggsstonaderTilXml implements Transformer<WebSoknad, AlternativRepresentasjon> {

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
        if (aktivBolk("flytting", webSoknad)) {
            rettighetstype.setFlytteutgifter(new FlytteutgifterTilXml().transform(webSoknad));
        }
        rettighetstype.setTilsynsutgifter(tilsynsutgifter(webSoknad));
        rettighetstype.setReiseutgifter(reiseutgifter(webSoknad));

        skjema.setRettighetstype(rettighetstype);
        skjema.setAktivitetsinformasjon(aktivitetsInformasjon(webSoknad));
        skjema.setPersonidentifikator(SubjectHandler.getSubjectHandler().getUid());
        return skjema;
    }

    private static Tilsynsutgifter tilsynsutgifter(WebSoknad webSoknad) {
        Tilsynsutgifter tilsynsutgifter = new Tilsynsutgifter();
        if (aktivBolk("familie", webSoknad)) {
            tilsynsutgifter.setTilsynsutgifterFamilie(new TilsynFamilieTilXml().transform(webSoknad));
        }
        if (aktivBolk("barnepass", webSoknad)) {
            tilsynsutgifter.setTilsynsutgifterBarn(new TilsynBarnepassTilXml().transform(webSoknad));
        }

        return tilsynsutgifter.getTilsynsutgifterBarn() == null && tilsynsutgifter.getTilsynsutgifterFamilie() == null ? null : tilsynsutgifter;
    }

    private static Aktivitetsinformasjon aktivitetsInformasjon(WebSoknad webSoknad) {
        Aktivitetsinformasjon result = new Aktivitetsinformasjon();
        String value = extractValue(webSoknad.getFaktumMedKey("aktivitet"), String.class, "id");
        if (Arrays.asList(null, "", "ikkeaktuelt", "arbeidssoking").contains(value)) {
            return null;
        }
        result.setAktivitetsId(value);
        return result;
    }

    private static boolean aktivBolk(String bolk, WebSoknad webSoknad) {
        Faktum bolkFaktum = webSoknad.getFaktumMedKey("informasjonsside.stonad." + bolk);
        return bolkFaktum != null && "true".equals(bolkFaktum.getValue());
    }

    private static Reiseutgifter reiseutgifter(WebSoknad webSoknad) {
        Reiseutgifter reiseutgifter = new Reiseutgifter();
        boolean satt = false;
        if (aktivBolk("reiseaktivitet", webSoknad)) {
            reiseutgifter.setDagligReise(new DagligReiseTilXml().transform(webSoknad));
            satt = true;
        }
        if (aktivBolk("reisearbeidssoker", webSoknad)) {
            reiseutgifter.setReisestoenadForArbeidssoeker(new ArbeidReiseTilXml().transform(webSoknad));
            satt = true;
        }
        if (aktivBolk("reisemidlertidig", webSoknad)) {
            reiseutgifter.setReiseVedOppstartOgAvsluttetAktivitet(new ReiseOppstartOgAvsluttetAktivitetTilXml().transform(webSoknad));
            satt = true;
        }
        if (aktivBolk("reisesamling", webSoknad)) {
            reiseutgifter.setReiseObligatoriskSamling(new SamlingReiseTilXml().transform(webSoknad));
            satt = true;
        }
        if (satt) {
            return reiseutgifter;
        }
        return null;
    }

    @Override
    public AlternativRepresentasjon transform(WebSoknad webSoknad) {
        Tilleggsstoenadsskjema tilleggsstoenadsskjema = tilTilleggsstoenadSkjema(webSoknad);
        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        JAXB.marshal(tilleggsstoenadsskjema, xml);
        return new AlternativRepresentasjon()
                .medMimetype("application/xml")
                .medFilnavn("Tilleggsstonader.xml")
                .medUuid(UUID.randomUUID().toString())
                .medContent(xml.toByteArray());
    }
}
