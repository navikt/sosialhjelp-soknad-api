package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue.XMLBankinnskudd;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue.XMLBankinnskudd.XMLBankinnskuddliste;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue.XMLBostotte;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue.XMLUtbetalinger;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue.XMLUtbetalinger.XMLUtbetalingerliste;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue.XMLVerdier;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue.XMLVerdier.XMLVerdierliste;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLUtbetaling;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLVerdi;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLBankinnskudd.*;
import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLBankinnskudd.ANNET;
import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLUtbetaling.*;
import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLVerdi.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.lagListeFraFakta;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilString;


public class InntektFormueTilXml implements Function<WebSoknad, XMLInntektFormue> {

    private static final Map<String, String> VERDI_MAP = lagVerdiMap();
    private static final Map<String, String> BANKINNSKUDD_MAP = lagBankinnskuddMap();
    private static final Map<String, String> UTBETALINGER_MAP = lagUtbetalingerMap();

    private static Map<String, String> lagVerdiMap() {
        Map<String, String> map = new HashMap<>();
        map.put("inntekt.eierandeler.true.type.bolig", BOLIG.value());
        map.put("inntekt.eierandeler.true.type.kjoretoy", KJORETOY.value());
        map.put("inntekt.eierandeler.true.type.campingvogn", CAMPINGVOGN.value());
        map.put("inntekt.eierandeler.true.type.fritidseiendom", FRITIDSEIENDOM.value());
        map.put("inntekt.eierandeler.true.type.annet", XMLVerdi.ANNET.value());
        return map;
    }

    private static Map<String, String> lagBankinnskuddMap() {
        Map<String, String> map = new HashMap<>();
        map.put("inntekt.bankinnskudd.true.type.sparekonto", SPAREKONTO.value());
        map.put("inntekt.bankinnskudd.true.type.brukskonto", BRUKSKONTO.value());
        map.put("inntekt.bankinnskudd.true.type.livsforsikring", LIVSFORSIKRING.value());
        map.put("inntekt.bankinnskudd.true.type.aksjer", AKSJER.value());
        map.put("inntekt.bankinnskudd.true.type.annet", ANNET.value());
        return map;
    }

    private static Map<String, String> lagUtbetalingerMap() {
        Map<String, String> map = new HashMap<>();
        map.put("inntekt.inntekter.true.type.utbytte", UTBYTTE.value());
        map.put("inntekt.inntekter.true.type.salg", SALG.value());
        map.put("inntekt.inntekter.true.type.forsikringsutbetalinger", FORSIKRINGSUTBETALING.value());
        map.put("inntekt.inntekter.true.type.annet", XMLUtbetaling.ANNET.value());
        return map;
    }

    @Override
    public XMLInntektFormue apply(WebSoknad webSoknad) {
        return new XMLInntektFormue()
                .withBostotte(new XMLBostotte()
                        .withMottarBostotte(tilString(webSoknad, "inntekt.bostotte")))
                .withVerdier(new XMLVerdier()
                        .withHarVerdier(tilString(webSoknad, "inntekt.eierandeler"))
                        .withVerdierliste(new XMLVerdierliste(lagListeFraFakta(webSoknad, VERDI_MAP)))
                        .withAnnetBeskrivelse(tilString(webSoknad, "inntekt.eierandeler.true.type.annet.true.beskrivelse")))
                .withBankinnskudd(new XMLBankinnskudd()
                        .withHarBankinnskudd(tilString(webSoknad, "inntekt.bankinnskudd"))
                        .withBankinnskuddliste(new XMLBankinnskuddliste(lagListeFraFakta(webSoknad, BANKINNSKUDD_MAP)))
                        .withAnnetBeskrivelse(tilString(webSoknad, "inntekt.bankinnskudd.true.type.annet.true.beskrivelse")))
                .withUtbetalinger(new XMLUtbetalinger()
                        .withHarUtbetalinger(tilString(webSoknad, "inntekt.inntekter"))
                        .withUtbetalingerliste(new XMLUtbetalingerliste(lagListeFraFakta(webSoknad, UTBETALINGER_MAP)))
                        .withAnnetBeskrivelse(tilString(webSoknad, "inntekt.inntekter.true.type.annet.true.beskrivelse")));
    }
}