package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
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
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.booleanVerdiFelt;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.createSvar;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.InntektFormueUtils.getBekreftelse;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelse;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelseTrue;

public class BostotteHusbanken {

    public Avsnitt getAvsnitt(JsonOkonomiopplysninger opplysninger, JsonDriftsinformasjon driftsinformasjon) {
        return new Avsnitt.Builder()
                .withTittel("inntekt.bostotte.husbanken.tittel")
                .withSporsmal(bostotteSporsmal(opplysninger, driftsinformasjon))
                .build();
    }

    private ArrayList<Sporsmal> bostotteSporsmal(JsonOkonomiopplysninger opplysninger, JsonDriftsinformasjon driftsinformasjon) {
        var harUtfyltBostotteSporsmal = harBekreftelse(opplysninger, BOSTOTTE);
        var harSvartJaBostotte = harUtfyltBostotteSporsmal && harBekreftelseTrue(opplysninger, BOSTOTTE);

        var harBostotteSamtykke = harSvartJaBostotte && harBekreftelseTrue(opplysninger, BOSTOTTE_SAMTYKKE);
        var fikkFeilMotHusbanken = Boolean.TRUE.equals(driftsinformasjon.getStotteFraHusbankenFeilet());

        var sporsmal = new ArrayList<Sporsmal>();
        sporsmal.add(
                new Sporsmal.Builder()
                        .withTittel("inntekt.bostotte.sporsmal.sporsmal")
                        .withErUtfylt(harUtfyltBostotteSporsmal)
                        .withFelt(harUtfyltBostotteSporsmal ?
                                booleanVerdiFelt(harSvartJaBostotte, "inntekt.bostotte.sporsmal.true", "inntekt.bostotte.sporsmal.false") :
                                null
                        )
                        .build()
        );

        if (harSvartJaBostotte && fikkFeilMotHusbanken) {
            sporsmal.add(
                    new Sporsmal.Builder()
                            .withTittel("inntekt.bostotte.kontaktproblemer")
                            .withErUtfylt(true)
                            .build()
            );
        }

        if (harSvartJaBostotte && !fikkFeilMotHusbanken)
            sporsmal.add(
                    new Sporsmal.Builder()
                            .withTittel("")
                            .withErUtfylt(true)
                            .withFelt(harBostotteSamtykke ? bostotteFelter(opplysninger) : manglerSamtykkeFelt())
                            .build()
            );
        return sporsmal;
    }

    private List<Felt> bostotteFelter(JsonOkonomiopplysninger opplysninger) {
        var felter = new ArrayList<Felt>();

        var bostotteBekreftelse = getBekreftelse(opplysninger, BOSTOTTE_SAMTYKKE);

        // info om tidspunkt for henting av data fra Husbanken
        felter.add(
                new Felt.Builder()
                        .withType(Type.TEKST)
                        .withSvar(createSvar(bostotteBekreftelse.getBekreftelsesDato(), SvarType.DATO))
                        .build()
        );

        var harUtbetalinger = opplysninger.getUtbetaling().stream().anyMatch(utbetaling -> UTBETALING_HUSBANKEN.equals(utbetaling.getType()));
        var harSaker = !opplysninger.getBostotte().getSaker().isEmpty();

        // hvis ingen utbetalinger eller saker:
        if (!harUtbetalinger && !harSaker) {
            felter.add(
                    new Felt.Builder()
                            .withType(Type.TEKST)
                            .withSvar(createSvar("inntekt.bostotte.ikkefunnet", SvarType.LOCALE_TEKST))
                            .build()
            );
        }

        // hvis ingen utbetalinger, men har saker
        if (!harUtbetalinger && harSaker) {
            felter.add(
                    new Felt.Builder()
                            .withType(Type.TEKST)
                            .withSvar(createSvar("inntekt.bostotte.utbetalingerIkkefunnet", SvarType.LOCALE_TEKST))
                            .build()
            );
        }

        opplysninger.getUtbetaling().stream()
                .filter(utbetaling -> UTBETALING_HUSBANKEN.equals(utbetaling.getType()))
                .forEach(utbetaling -> {
                            var map = new LinkedHashMap<String, Svar>();
                            map.put("inntekt.bostotte.utbetaling.mottaker", createSvar(utbetaling.getMottaker().value(), SvarType.TEKST));
                            map.put("inntekt.bostotte.utbetaling.utbetalingsdato", createSvar(utbetaling.getUtbetalingsdato(), SvarType.DATO));
                            map.put("inntekt.bostotte.utbetaling.belop", createSvar(utbetaling.getNetto().toString(), SvarType.TEKST));

                            felter.add(
                                    new Felt.Builder()
                                            .withLabelSvarMap(map)
                                            .withType(Type.SYSTEMDATA_MAP)
                                            .build()
                            );
                        }
                );

        // hvis ingen saker, men har utbetalinger
        if (harUtbetalinger && !harSaker) {
            felter.add(
                    new Felt.Builder()
                            .withType(Type.TEKST)
                            .withSvar(createSvar("inntekt.bostotte.sakerIkkefunnet", SvarType.LOCALE_TEKST))
                            .build()
            );
        }
        opplysninger.getBostotte().getSaker()
                .forEach(sak -> {
                            var map = new LinkedHashMap<String, Svar>();
                            map.put("inntekt.bostotte.sak.dato", createSvar(sak.getDato(), SvarType.DATO));
                            map.put("inntekt.bostotte.sak.status", createSvar(bostotteSakStatus(sak), SvarType.TEKST));

                            felter.add(
                                    new Felt.Builder()
                                            .withLabelSvarMap(map)
                                            .withType(Type.SYSTEMDATA_MAP)
                                            .build()
                            );
                        }
                );
        return felter;
    }

    private String bostotteSakStatus(JsonBostotteSak sak) {
        var status = sak.getVedtaksstatus() != null ? sak.getVedtaksstatus().value() : sak.getStatus();
        if (sak.getBeskrivelse() != null && !sak.getBeskrivelse().isBlank()) {
            status += String.format(": %s", sak.getBeskrivelse());
        }
        return status;
    }

    private List<Felt> manglerSamtykkeFelt() {
        return singletonList(
                new Felt.Builder()
                        .withSvar(createSvar("inntekt.bostotte.mangler_samtykke", SvarType.LOCALE_TEKST))
                        .build()
        );
    }
}
