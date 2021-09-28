package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelseTrue;

public class SkattbarInntekt {

    public Avsnitt getAvsnitt(JsonOkonomi okonomi, JsonDriftsinformasjon driftsinformasjon) {
        var opplysninger = okonomi.getOpplysninger();
        var fikkFeilMotSkatteetaten = Boolean.TRUE.equals(driftsinformasjon.getInntektFraSkatteetatenFeilet());

        return new Avsnitt.Builder()
                .withTittel("utbetalinger.inntekt.skattbar.tittel")
                .withSporsmal(skattbarInntektSporsmal(opplysninger, fikkFeilMotSkatteetaten))
                .build();
    }

    private ArrayList<Sporsmal> skattbarInntektSporsmal(JsonOkonomiopplysninger opplysninger, boolean fikkFeilMotSkatteetaten) {
        var harSkatteetatenSamtykke = harBekreftelseTrue(opplysninger, UTBETALING_SKATTEETATEN_SAMTYKKE);

        var sporsmal = new ArrayList<Sporsmal>();
        if (!harSkatteetatenSamtykke) {
            sporsmal.add(
                    new Sporsmal.Builder()
                            .withTittel("utbetalinger.inntekt.skattbar.mangler_samtykke")
                            .withErUtfylt(true) // alltid true (eller null)
                            .build()
            );
        }

        if (harSkatteetatenSamtykke && fikkFeilMotSkatteetaten) {
            sporsmal.add(
                    new Sporsmal.Builder()
                            .withTittel("utbetalinger.skattbar.kontaktproblemer.oppsummering")
                            .withErUtfylt(true)
                            .build()
            );
        }

        if (harSkatteetatenSamtykke && !fikkFeilMotSkatteetaten) {
            sporsmal.add(
                    new Sporsmal.Builder()
                            .withTittel("utbetalinger.inntekt.skattbar.har_gitt_samtykke")
                            // undertittel? Tidspunkt for henting av inntekt fra Skatteetaten // bekreftelse.getBekreftelsesDato
                            .withErUtfylt(true)
                            .withFelt(skattbarInntektFelter(opplysninger.getUtbetaling()))
                            .build()
            );
        }
        return sporsmal;
    }

    private List<Felt> skattbarInntektFelter(List<JsonOkonomiOpplysningUtbetaling> utbetalinger) {
        var harSkattbareInntekter = utbetalinger != null && utbetalinger.stream().anyMatch(utbetaling -> UTBETALING_SKATTEETATEN.equals(utbetaling.getType()));

        if (!harSkattbareInntekter) {
            // mer info her?
            return Collections.emptyList();
        }

        // todo: gruppere pr mnd og organisasjon? summere inntekter og skattetrekk

        return utbetalinger.stream()
                .filter(utbetaling -> UTBETALING_SKATTEETATEN.equals(utbetaling.getType()))
                //.collect(Collectors.groupingBy()) ??
                .map(utbetaling -> {
                    // arbeidsgivernavn, fom, tom, brutto, forskuddstrekk
                    var map = new LinkedHashMap<String, String>();
                    map.put("utbetalinger.utbetaling.arbeidsgivernavn.label", utbetaling.getOrganisasjon().getNavn());
                    map.put("utbetalinger.utbetaling.periodeFom.label", utbetaling.getPeriodeFom());
                    map.put("utbetalinger.utbetaling.periodeTom.label", utbetaling.getPeriodeTom());
                    map.put("utbetalinger.utbetaling.brutto.label", utbetaling.getBrutto().toString());
                    map.put("utbetalinger.utbetaling.skattetrekk.label", utbetaling.getSkattetrekk().toString());

                    return new Felt.Builder()
                            .withType(Type.SYSTEMDATA_MAP)
                            .withLabelSvarMap(map)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
