package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue.*;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue.XMLBankinnskudd.XMLBankinnskuddliste;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue.XMLBostotte.XMLBostotteliste;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue.XMLUtbetalinger.XMLUtbetalingerliste;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLInntektFormue.XMLVerdier.XMLVerdierliste;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKildeString;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLUtbetaling;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLVerdi;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLBankinnskudd.*;
import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLBankinnskudd.ANNET;
import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLBostotte.HUSBANKEN;
import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLBostotte.KOMMUNAL;
import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLUtbetaling.FORSIKRINGSUTBETALING;
import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLUtbetaling.SALG;
import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLUtbetaling.UTBYTTE;
import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLVerdi.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilBoolean;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilString;


public class InntektFormueTilXml implements Function<WebSoknad, XMLInntektFormue> {

    private static final Map<String, String> BOSTOTTE_MAP = lagBostotteMap();
    private static final Map<String, String> VERDI_MAP = lagVerdiMap();
    private static final Map<String, String> BANKINNSKUDD_MAP = lagBankinnskuddMap();
    private static final Map<String, String> UTBETALINGER_MAP = lagUtbetalingerMap();

    private static Map<String, String> lagBostotteMap() {
        Map<String, String> map = new HashMap<>();
        map.put("inntekt.bostotte.true.type.husbanken", HUSBANKEN.value());
        map.put("inntekt.bostotte.true.type.kommunal", KOMMUNAL.value());
        return map;
    }

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

    private List<XMLKildeString> lagListeFraFakta(WebSoknad webSoknad, Map<String, String> keyTilEnum) {
        return keyTilEnum.entrySet().stream()
                .map((e) -> webSoknad.getValueForFaktum(e.getKey()).equals("true") ? e.getValue() : null)
                .filter(Objects::nonNull)
                .map(SoknadSosialhjelpUtils::tilString)
                .collect(Collectors.toList());
    }

    @Override
    public XMLInntektFormue apply(WebSoknad webSoknad) {
        return new XMLInntektFormue()
                .withNavYtelser(new XMLNavYtelser()
                        .withMottarYtelser(tilBoolean(webSoknad, "inntekt.bostotte"))
                        .withIkkeFerdigbehandledeYtelser(tilBoolean(webSoknad, "inntekt.soktytelser")))
                .withBostotte(new XMLBostotte()
                        // TODO sette true/false siden listen ikke er påkrevd?
                        .withBostotteliste(new XMLBostotteliste(lagListeFraFakta(webSoknad, BOSTOTTE_MAP))))
                .withVerdier(new XMLVerdier()
                        .withVerdierliste(new XMLVerdierliste(lagListeFraFakta(webSoknad, VERDI_MAP)))
                        .withAnnetBeskrivelse(tilString(webSoknad, "inntekt.eierandeler.true.type.annet.true.beskrivelse")))
                .withBankinnskudd(new XMLBankinnskudd()
                        .withBankinnskuddliste(new XMLBankinnskuddliste(lagListeFraFakta(webSoknad, BANKINNSKUDD_MAP)))
                        .withAnnetBeskrivelse(tilString(webSoknad, "inntekt.bankinnskudd.true.type.annet.true.beskrivelse")))
                .withUtbetalinger(new XMLUtbetalinger()
                        .withUtbetalingerliste(new XMLUtbetalingerliste(lagListeFraFakta(webSoknad, UTBETALINGER_MAP)))
                        .withAnnetBeskrivelse(tilString(webSoknad, "inntekt.inntekter.true.type.annet.true.beskrivelse")));
    }
}