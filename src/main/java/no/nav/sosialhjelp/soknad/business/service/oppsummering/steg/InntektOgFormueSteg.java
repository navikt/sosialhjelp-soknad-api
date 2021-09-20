package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_SPARING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_UTBETALING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_VERDI;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_ANNET;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BRUKSKONTO;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BSU;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_LIVSFORSIKRING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_SPAREKONTO;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_VERDIPAPIRER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.STUDIELAN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_ANNET;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_FORSIKRING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_NAVYTELSE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SALG;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_UTBYTTE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_ANNET;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_BOLIG;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_CAMPINGVOGN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_FRITIDSEIENDOM;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_KJORETOY;

public class InntektOgFormueSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        var okonomi = jsonInternalSoknad.getSoknad().getData().getOkonomi();
        var opplysninger = okonomi.getOpplysninger();
        var driftsinformasjon = jsonInternalSoknad.getSoknad().getDriftsinformasjon();

        var avsnitt = new ArrayList<Avsnitt>();
        avsnitt.add(skattbarInntektAvsnitt(okonomi, driftsinformasjon));
        avsnitt.add(navUtbetalingerAvsnitt(opplysninger, driftsinformasjon));
        avsnitt.add(bostotteHusbankenAvsnitt(opplysninger, driftsinformasjon));
        if (erStudent(jsonInternalSoknad.getSoknad().getData().getUtdanning())) {
            avsnitt.add(studielanAvsnitt(opplysninger));
        }
        avsnitt.add(andreInntekterAvsnitt(opplysninger));
        avsnitt.add(bankAvsnitt(okonomi));
        avsnitt.add(annenFormueAvsnitt(okonomi));

        return new Steg.Builder()
                .withStegNr(6)
                .withTittel("inntektbolk.tittel")
                .withAvsnitt(avsnitt)
                .build();
    }

    private Avsnitt skattbarInntektAvsnitt(JsonOkonomi okonomi, JsonDriftsinformasjon driftsinformasjon) {
        var fikkFeilMotSkatteetaten = Boolean.TRUE.equals(driftsinformasjon.getInntektFraSkatteetatenFeilet());

        return new Avsnitt.Builder()
                .withTittel("utbetalinger.inntekt.skattbar.tittel")
                .withSporsmal(skattbarInntektSporsmal(okonomi, fikkFeilMotSkatteetaten))
                .build();
    }

    private ArrayList<Sporsmal> skattbarInntektSporsmal(JsonOkonomi okonomi, boolean fikkFeilMotSkatteetaten) {
        var opplysninger = okonomi.getOpplysninger();
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
                            .withTittel("Vi fikk ikke hentet opplysninger fra Skatteetaten") // Vi fikk ikke hentet opplysninger fra Skatteetaten
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

    private Avsnitt navUtbetalingerAvsnitt(JsonOkonomiopplysninger opplysninger, JsonDriftsinformasjon driftsinformasjon) {
        var utbetalingerFraNavFeilet = Boolean.TRUE.equals(driftsinformasjon.getUtbetalingerFraNavFeilet());

        return new Avsnitt.Builder()
                .withTittel("navytelser.sporsmal")
                .withSporsmal(navUtbetalingerSporsmal(opplysninger, utbetalingerFraNavFeilet))
                .build();
    }

    private List<Sporsmal> navUtbetalingerSporsmal(JsonOkonomiopplysninger opplysninger, boolean utbetalingerFraNavFeilet) {
        if (utbetalingerFraNavFeilet) {
            // På grunn av systemfeil klarte vi ikke å hente ned informasjon om ytelser fra NAV.
            return singletonList(
                    new Sporsmal.Builder()
                            .withTittel("utbetalinger.kontaktproblemer")
                            .withErUtfylt(true)
                            .build()
            );
        }
        var harNavUtbetalinger = opplysninger.getUtbetaling() != null && opplysninger.getUtbetaling().stream().anyMatch(utbetaling -> UTBETALING_NAVYTELSE.equals(utbetaling.getType()));
        if (!harNavUtbetalinger) {
            // Vi har ingen registrerte utbetalinger på deg fra NAV den siste måneden.
            return singletonList(
                    new Sporsmal.Builder()
                            .withTittel("utbetalinger.ingen.true")
                            .withErUtfylt(true)
                            .build()
            );
        }

        // 1 eller flere utbetalinger
        return opplysninger.getUtbetaling().stream()
                .filter(utbetaling -> UTBETALING_NAVYTELSE.equals(utbetaling.getType()))
                .map(utbetaling -> {
                    var map = new LinkedHashMap<String, String>();
                    // type, beløp (netto), beløp (brutto), utbetaltdato
                    map.put("utbetalinger.utbetaling.type.label", utbetaling.getTittel());
                    map.put("utbetalinger.utbetaling.netto.label", utbetaling.getNetto().toString());
                    map.put("utbetalinger.utbetaling.brutto.label", utbetaling.getBrutto().toString());
                    map.put("utbetalinger.utbetaling.utbetalingsdato.label", utbetaling.getUtbetalingsdato());

                    return new Sporsmal.Builder()
                            .withTittel("utbetalinger.utbetaling.sporsmal")
                            .withErUtfylt(true)
                            .withFelt(
                                    singletonList(
                                            new Felt.Builder()
                                                    .withType(Type.SYSTEMDATA_MAP)
                                                    .withLabelSvarMap(map)
                                                    .build()
                                    )
                            )
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Avsnitt bostotteHusbankenAvsnitt(JsonOkonomiopplysninger opplysninger, JsonDriftsinformasjon driftsinformasjon) {
        return new Avsnitt.Builder()
                .withTittel("inntekt.bostotte.husbanken.tittel")
                .withSporsmal(bostotteSporsmal(opplysninger, driftsinformasjon))
                .build();
    }

    private ArrayList<Sporsmal> bostotteSporsmal(JsonOkonomiopplysninger opplysninger, JsonDriftsinformasjon driftsinformasjon) {
        var harUtfyltBostotteSporsmal = harBekreftelse(opplysninger, BOSTOTTE);
        var harSvartJaBostotte = harUtfyltBostotteSporsmal && harBekreftelseTrue(opplysninger, BOSTOTTE);

        var harBostotteSamtykke = harUtfyltBostotteSporsmal && harBekreftelseTrue(opplysninger, BOSTOTTE_SAMTYKKE);
        var fikkFeilMotHusbanken = Boolean.TRUE.equals(driftsinformasjon.getStotteFraHusbankenFeilet());

        var sporsmal = new ArrayList<Sporsmal>();
        sporsmal.add(
                new Sporsmal.Builder()
                        .withTittel("inntekt.bostotte.sporsmal.sporsmal")
                        .withErUtfylt(harUtfyltBostotteSporsmal)
                        .withFelt(harUtfyltBostotteSporsmal ?
                                singletonList(
                                        new Felt.Builder()
                                                .withSvar(bostotteSvar(harSvartJaBostotte))
                                                .withType(Type.CHECKBOX)
                                                .build()
                                ) :
                                null
                        )
                        .build()
        );

        if (harSvartJaBostotte && fikkFeilMotHusbanken) {
            sporsmal.add(
                    new Sporsmal.Builder()
                            .withTittel("Vi fikk ikke hentet opplysninger fra Husbanken") // Vi fikk ikke hentet opplysninger fra Husbanken
                            .build()
            );
        }

        if (harSvartJaBostotte && !fikkFeilMotHusbanken)
            sporsmal.add(
                    new Sporsmal.Builder()
                            .withTittel("")
                            .withErUtfylt(true)
                            .withFelt(harBostotteSamtykke ? bostotteFelter(opplysninger) : ikkeHentetBostotte())
                            .build()
            );
        return sporsmal;
    }

    private String bostotteSvar(boolean harSoktEllerMottattBostotte) {
        return harSoktEllerMottattBostotte ? "inntekt.bostotte.sporsmal.true" : "inntekt.bostotte.sporsmal.false";
    }

    private List<Felt> bostotteFelter(JsonOkonomiopplysninger opplysninger) {
        var felter = new ArrayList<Felt>();
        opplysninger.getUtbetaling().stream()
                .filter(utbetaling -> UTBETALING_HUSBANKEN.equals(utbetaling.getType()))
                .forEach(utbetaling -> {
                            var map = new LinkedHashMap<String, String>();
                            map.put("inntekt.bostotte.utbetaling.mottaker", utbetaling.getMottaker().value());
                            map.put("inntekt.bostotte.utbetaling.utbetalingsdato", utbetaling.getUtbetalingsdato());
                            map.put("inntekt.bostotte.utbetaling.belop", utbetaling.getBelop().toString());

                            felter.add(
                                    new Felt.Builder()
                                            .withLabelSvarMap(map)
                                            .withType(Type.SYSTEMDATA_MAP)
                                            .build()
                            );
                        }
                );
        opplysninger.getBostotte().getSaker()
                .forEach(sak -> {
                            var map = new LinkedHashMap<String, String>();
                            map.put("inntekt.bostotte.sak.dato", sak.getDato());
                            map.put("inntekt.bostotte.sak.status", sak.getStatus());

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

    private List<Felt> ikkeHentetBostotte() {
        return singletonList(
                new Felt.Builder()
                        .withSvar("inntekt.bostotte.mangler_samtykke")
                        .build()
        );
    }

    private Avsnitt studielanAvsnitt(JsonOkonomiopplysninger opplysninger) {
        return new Avsnitt.Builder()
                .withTittel("inntekt.studielan.titel")
                .withSporsmal(studielanSporsmal(opplysninger))
                .build();
    }

    private List<Sporsmal> studielanSporsmal(JsonOkonomiopplysninger opplysninger) {
        var harUtfyltStudielanSporsmal = harBekreftelse(opplysninger, STUDIELAN);
        var harSvartJaStudielan = harUtfyltStudielanSporsmal && harBekreftelseTrue(opplysninger, STUDIELAN);

        return singletonList(
                new Sporsmal.Builder()
                        .withTittel("inntekt.studielan.sporsmal")
                        .withErUtfylt(harUtfyltStudielanSporsmal)
                        .withFelt(harUtfyltStudielanSporsmal ? studielanFelt(harSvartJaStudielan) : null)
                        .build()
        );
    }

    private List<Felt> studielanFelt(boolean harStudielan) {
        return singletonList(
                new Felt.Builder()
                        .withType(Type.CHECKBOX)
                        .withSvar(harStudielan ? "inntekt.studielan.true" : "inntekt.studielan.false")
                        .build()
        );
    }

    private Avsnitt andreInntekterAvsnitt(JsonOkonomiopplysninger opplysninger) {
        return new Avsnitt.Builder()
                .withTittel("inntekt.inntekter.titel")
                .withSporsmal(andreInntekterSporsmal(opplysninger))
                .build();
    }

    private List<Sporsmal> andreInntekterSporsmal(JsonOkonomiopplysninger opplysninger) {
        var harUtfyltAndreInntekterSporsmal = harBekreftelse(opplysninger, BEKREFTELSE_UTBETALING);
        var harSvartJaAndreInntekter = harBekreftelseTrue(opplysninger, BEKREFTELSE_UTBETALING);
        var utbetalingTyper = Arrays.asList(UTBETALING_UTBYTTE, UTBETALING_SALG, UTBETALING_FORSIKRING, UTBETALING_ANNET);
        var sporsmal = new ArrayList<Sporsmal>();
        sporsmal.add(
                new Sporsmal.Builder()
                        .withTittel("inntekt.inntekter.sporsmal")
                        .withErUtfylt(harUtfyltAndreInntekterSporsmal)
                        .withFelt(harUtfyltAndreInntekterSporsmal ? andreInntekterFelt(harSvartJaAndreInntekter) : null)
                        .build()
        );

        if (harSvartJaAndreInntekter) {
            var harSvartHvaHarDuMottattSporsmal = opplysninger.getUtbetaling().stream().noneMatch(utbetaling -> utbetalingTyper.contains(utbetaling.getType()));
            sporsmal.add(
                    new Sporsmal.Builder()
                            .withTittel("inntekt.inntekter.true.type.sporsmal")
                            .withErUtfylt(harSvartHvaHarDuMottattSporsmal)
                            .withFelt(harSvartHvaHarDuMottattSporsmal ? andreinntekterFelter(opplysninger) : null)
                            .build()
            );

            if (sporsmal.get(1).containsFeltWithSvar("json.okonomi.opplysninger.inntekt.inntekter.annet")) {
                var beskrivelseAvAnnet = opplysninger.getBeskrivelseAvAnnet();
                var harUtfyltAnnetFelt = beskrivelseAvAnnet != null && beskrivelseAvAnnet.getUtbetaling() != null && !beskrivelseAvAnnet.getUtbetaling().isBlank();
                sporsmal.add(
                        new Sporsmal.Builder()
                                .withTittel("inntekt.inntekter.true.type.annet")
                                .withErUtfylt(harUtfyltAnnetFelt)
                                .withFelt(harUtfyltAnnetFelt ?
                                        singletonList(new Felt.Builder().withType(Type.TEKST).withSvar(beskrivelseAvAnnet.getUtbetaling()).build()) :
                                        null
                                )
                                .build()
                );
            }
        }
        return sporsmal;
    }

    private List<Felt> andreInntekterFelt(boolean harSvartJaAndreInntekter) {
        return singletonList(
                new Felt.Builder()
                        .withType(Type.CHECKBOX)
                        .withSvar(harSvartJaAndreInntekter ? "inntekt.inntekter.true" : "inntekt.inntekter.false")
                        .build()
        );
    }

    private List<Felt> andreinntekterFelter(JsonOkonomiopplysninger opplysninger) {
        var felter = new ArrayList<Felt>();
        addUtbetalingIfPresent(opplysninger, felter, UTBETALING_UTBYTTE, "json.okonomi.opplysninger.inntekt.inntekter.utbytte");
        addUtbetalingIfPresent(opplysninger, felter, UTBETALING_SALG, "json.okonomi.opplysninger.inntekt.inntekter.salg");
        addUtbetalingIfPresent(opplysninger, felter, UTBETALING_FORSIKRING, "json.okonomi.opplysninger.inntekt.inntekter.forsikringsutbetalinger");
        addUtbetalingIfPresent(opplysninger, felter, UTBETALING_ANNET, "json.okonomi.opplysninger.inntekt.inntekter.annet");
        return felter;
    }

    private void addUtbetalingIfPresent(JsonOkonomiopplysninger opplysninger, ArrayList<Felt> felter, String type, String key) {
        opplysninger.getUtbetaling().stream()
                .filter(utbetaling -> type.equals(utbetaling.getType()))
                .findFirst()
                .ifPresent(utbetaling -> felter.add(
                        new Felt.Builder()
                                .withType(Type.CHECKBOX)
                                .withSvar(key)
                                .build()
                ));
    }

    private Avsnitt bankAvsnitt(JsonOkonomi okonomi) {
        var oversikt = okonomi.getOversikt();
        var opplysninger = okonomi.getOpplysninger();

        return new Avsnitt.Builder()
                .withTittel("opplysninger.formue.bank.undertittel")
                .withSporsmal(bankSporsmal(oversikt, opplysninger))
                .build();
    }

    private List<Sporsmal> bankSporsmal(JsonOkonomioversikt oversikt, JsonOkonomiopplysninger opplysninger) {
        var harUtfyltBankSporsmal = harBekreftelseTrue(opplysninger, BEKREFTELSE_SPARING);

        var sporsmal = new ArrayList<Sporsmal>();

        sporsmal.add(
                new Sporsmal.Builder()
                        .withTittel("inntekt.bankinnskudd.true.type.sporsmal")
                        .withErUtfylt(true) // alltid true, eller nullable?
                        .withFelt(harUtfyltBankSporsmal ? formueFelter(oversikt) : null)
                        .build()
        );

        if (harValgtFormueType(oversikt, FORMUE_ANNET)) {
            var beskrivelseAvAnnet = opplysninger.getBeskrivelseAvAnnet();
            var harUtfyltAnnetFelt = beskrivelseAvAnnet != null && beskrivelseAvAnnet.getSparing() != null && !beskrivelseAvAnnet.getSparing().isBlank();

            sporsmal.add(
                    new Sporsmal.Builder()
                            .withTittel("inntekt.bankinnskudd.true.type.annet.true.beskrivelse.label")
                            .withErUtfylt(harUtfyltAnnetFelt)
                            .withFelt(harUtfyltAnnetFelt ?
                                    singletonList(new Felt.Builder().withType(Type.TEKST).withSvar(beskrivelseAvAnnet.getSparing()).build()) :
                                    null)
                            .build()
            );
        }

        return sporsmal;
    }

    private List<Felt> formueFelter(JsonOkonomioversikt oversikt) {
        var felter = new ArrayList<Felt>();
        addFormueIfPresent(oversikt, felter, FORMUE_BRUKSKONTO, "inntekt.bankinnskudd.true.type.brukskonto");
        addFormueIfPresent(oversikt, felter, FORMUE_BSU, "inntekt.bankinnskudd.true.type.bsu");
        addFormueIfPresent(oversikt, felter, FORMUE_LIVSFORSIKRING, "inntekt.bankinnskudd.true.type.livsforsikringssparedel");
        addFormueIfPresent(oversikt, felter, FORMUE_SPAREKONTO, "inntekt.bankinnskudd.true.type.sparekonto");
        addFormueIfPresent(oversikt, felter, FORMUE_VERDIPAPIRER, "inntekt.bankinnskudd.true.type.verdipapirer");
        addFormueIfPresent(oversikt, felter, FORMUE_ANNET, "inntekt.bankinnskudd.true.type.annet");
        return felter;
    }

    private void addFormueIfPresent(JsonOkonomioversikt oversikt, ArrayList<Felt> felter, String type, String key) {
        oversikt.getFormue().stream()
                .filter(formue -> type.equals(formue.getType()))
                .findFirst()
                .ifPresent(formue -> felter.add(
                        new Felt.Builder()
                                .withType(Type.CHECKBOX)
                                .withSvar(key)
                                .build()
                ));
    }

    private boolean harValgtFormueType(JsonOkonomioversikt oversikt, String type) {
        return oversikt.getFormue().stream()
                .anyMatch(formue -> type.equals(formue.getType()));
    }

    private Avsnitt annenFormueAvsnitt(JsonOkonomi okonomi) {
        var oversikt = okonomi.getOversikt();
        var opplysninger = okonomi.getOpplysninger();

        return new Avsnitt.Builder()
                .withTittel("opplysninger.formue.annen.undertittel")
                .withSporsmal(annenFormueSporsmal(oversikt, opplysninger))
                .build();
    }

    private List<Sporsmal> annenFormueSporsmal(JsonOkonomioversikt oversikt, JsonOkonomiopplysninger opplysninger) {
        var harUtfyltAnnenFormueSporsmal = harBekreftelse(opplysninger, BEKREFTELSE_VERDI);
        var harSvartJaAnnenFormue = harBekreftelseTrue(opplysninger, BEKREFTELSE_VERDI);
        var utbetalingTyper = Arrays.asList(VERDI_BOLIG, VERDI_CAMPINGVOGN, VERDI_KJORETOY, VERDI_FRITIDSEIENDOM, VERDI_ANNET);

        var sporsmal = new ArrayList<Sporsmal>();
        sporsmal.add(
                new Sporsmal.Builder()
                        .withTittel("inntekt.eierandeler.sporsmal")
                        .withErUtfylt(harUtfyltAnnenFormueSporsmal)
                        .withFelt(harUtfyltAnnenFormueSporsmal ? annenFormueFelt(harSvartJaAnnenFormue) : null)
                        .build()
        );

        if (harSvartJaAnnenFormue) {
            var harSvartHvaEierDuSporsmal = opplysninger.getUtbetaling().stream().noneMatch(utbetaling -> utbetalingTyper.contains(utbetaling.getType()));
            sporsmal.add(
                    new Sporsmal.Builder()
                            .withTittel("inntekt.eierandeler.true.type.sporsmal")
                            .withErUtfylt(harSvartHvaEierDuSporsmal)
                            .withFelt(harSvartHvaEierDuSporsmal ? annenFormueFelter(oversikt) : null)
                            .build()
            );

            if (harValgtFormueType(oversikt, VERDI_ANNET)) {
                var beskrivelseAvAnnet = opplysninger.getBeskrivelseAvAnnet();
                var harUtfyltAnnetFelt = beskrivelseAvAnnet != null && beskrivelseAvAnnet.getVerdi() != null && !beskrivelseAvAnnet.getVerdi().isBlank();
                sporsmal.add(
                        new Sporsmal.Builder()
                                .withTittel("inntekt.eierandeler.true.type.annet.true.beskrivelse.label")
                                .withErUtfylt(harUtfyltAnnetFelt)
                                .withFelt(harUtfyltAnnetFelt ?
                                        singletonList(new Felt.Builder().withType(Type.TEKST).withSvar(beskrivelseAvAnnet.getVerdi()).build()) :
                                        null
                                )
                                .build()
                );
            }
        }
        return sporsmal;
    }

    private List<Felt> annenFormueFelt(boolean harSvartJa) {
        return singletonList(
                new Felt.Builder()
                        .withType(Type.CHECKBOX)
                        .withSvar(harSvartJa ? "inntekt.eierandeler.true" : "inntekt.eierandeler.false")
                        .build()
        );
    }

    private List<Felt> annenFormueFelter(JsonOkonomioversikt oversikt) {
        var felter = new ArrayList<Felt>();
        addFormueIfPresent(oversikt, felter, VERDI_BOLIG, "inntekt.eierandeler.true.type.bolig");
        addFormueIfPresent(oversikt, felter, VERDI_CAMPINGVOGN, "inntekt.eierandeler.true.type.campingvogn");
        addFormueIfPresent(oversikt, felter, VERDI_KJORETOY, "inntekt.eierandeler.true.type.kjoretoy");
        addFormueIfPresent(oversikt, felter, VERDI_FRITIDSEIENDOM, "inntekt.eierandeler.true.type.fritidseiendom");
        addFormueIfPresent(oversikt, felter, VERDI_ANNET, "inntekt.eierandeler.true.type.annet");
        return felter;
    }

    private boolean erStudent(JsonUtdanning utdanning) {
        return utdanning != null && utdanning.getErStudent() != null && utdanning.getErStudent().equals(Boolean.TRUE);
    }

    private boolean harBekreftelse(JsonOkonomiopplysninger opplysninger, String type) {
        return opplysninger.getBekreftelse() != null && opplysninger.getBekreftelse().stream().anyMatch(bekreftelse -> type.equals(bekreftelse.getType()));
    }

    private boolean harBekreftelseTrue(JsonOkonomiopplysninger opplysninger, String type) {
        return opplysninger.getBekreftelse().stream().anyMatch(bekreftelse -> type.equals(bekreftelse.getType()) && Boolean.TRUE.equals(bekreftelse.getVerdi()));
    }
}
