package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Svar;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.createSvar;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.InntektFormueUtils.getBekreftelse;
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
                            .withErUtfylt(true)
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
            sporsmal.add(bekreftelsesTidspunktSporsmal(getBekreftelse(opplysninger, UTBETALING_SKATTEETATEN_SAMTYKKE)));

            sporsmal.add(new Sporsmal.Builder()
                    .withTittel("utbetalinger.inntekt.skattbar.inntekt.tittel")
                    .withFelt(skattbarInntektFelter(opplysninger.getUtbetaling()))
                    .withErUtfylt(true)
                    .build()
            );
        }
        return sporsmal;
    }

    private Sporsmal bekreftelsesTidspunktSporsmal(JsonOkonomibekreftelse skatteetatenBekreftelse) {
        return new Sporsmal.Builder()
                .withTittel("utbetalinger.inntekt.skattbar.har_gitt_samtykke")
                .withFelt(singletonList(
                        new Felt.Builder()
                                .withType(Type.TEKST)
                                .withSvar(createSvar(skatteetatenBekreftelse.getBekreftelsesDato(), SvarType.TIDSPUNKT))
                                .build()
                ))
                .withErUtfylt(true)
                .build();
    }

    private List<Felt> skattbarInntektFelter(List<JsonOkonomiOpplysningUtbetaling> utbetalinger) {
        var harSkattbareInntekter = utbetalinger != null && utbetalinger.stream().anyMatch(utbetaling -> UTBETALING_SKATTEETATEN.equals(utbetaling.getType()));

        var feltListe = new ArrayList<Felt>();

        if (!harSkattbareInntekter) {
            feltListe.add(
                    new Felt.Builder()
                            .withType(Type.TEKST)
                            .withSvar(createSvar("utbetalinger.inntekt.skattbar.ingen", SvarType.LOCALE_TEKST))
                            .build()
            );
        } else {
            // todo: gruppere pr mnd og organisasjon? summere inntekter og skattetrekk
            utbetalinger.stream()
                    .filter(utbetaling -> UTBETALING_SKATTEETATEN.equals(utbetaling.getType()))
                    .forEach(utbetaling -> {
                        var map = new LinkedHashMap<String, Svar>();
                        if (utbetaling.getOrganisasjon() == null) {
                            map.put("utbetalinger.utbetaling.arbeidsgivernavn.label", createSvar("Uten organisasjonsnummer", SvarType.TEKST));
                        } else {
                            map.put("utbetalinger.utbetaling.arbeidsgivernavn.label", createSvar(utbetaling.getOrganisasjon().getNavn(), SvarType.TEKST));
                        }
                        map.put("utbetalinger.utbetaling.periodeFom.label", createSvar(utbetaling.getPeriodeFom(), SvarType.DATO));
                        map.put("utbetalinger.utbetaling.periodeTom.label", createSvar(utbetaling.getPeriodeTom(), SvarType.DATO));
                        map.put("utbetalinger.utbetaling.brutto.label", createSvar(utbetaling.getBrutto().toString(), SvarType.TEKST));
                        map.put("utbetalinger.utbetaling.skattetrekk.label", createSvar(utbetaling.getSkattetrekk().toString(), SvarType.TEKST));

                        feltListe.add(
                                new Felt.Builder()
                                        .withType(Type.SYSTEMDATA_MAP)
                                        .withLabelSvarMap(map)
                                        .build());
                    });
        }

        return feltListe;
    }
}
