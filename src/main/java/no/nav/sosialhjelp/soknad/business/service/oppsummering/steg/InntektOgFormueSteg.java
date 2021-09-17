package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;

public class InntektOgFormueSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        var okonomi = jsonInternalSoknad.getSoknad().getData().getOkonomi();
        var driftsinformasjon = jsonInternalSoknad.getSoknad().getDriftsinformasjon();

        var avsnitt = new ArrayList<Avsnitt>();
        avsnitt.add(skattbarInntektAvsnitt(okonomi, driftsinformasjon));
        avsnitt.add(navUtbetalingerAvsnitt());
        avsnitt.add(bostotteHusbankenAvsnitt());
        if (erStudent(jsonInternalSoknad.getSoknad().getData().getUtdanning())) {
            avsnitt.add(studielanAvsnitt());
        }
        avsnitt.add(andreInntekterAvsnitt());
        avsnitt.add(bankAvsnitt());
        avsnitt.add(annenFormueAvsnitt());

        return new Steg.Builder()
                .withStegNr(6)
                .withTittel("inntektbolk.tittel")
                .withAvsnitt(avsnitt)
                .build();
    }

    private Avsnitt skattbarInntektAvsnitt(JsonOkonomi okonomi, JsonDriftsinformasjon driftsinformasjon) {
        var opplysninger = okonomi.getOpplysninger();

        var harSkatteetatenSamtykke = opplysninger != null && opplysninger.getBekreftelse() != null && opplysninger.getBekreftelse().stream().anyMatch(bekreftelse -> UTBETALING_SKATTEETATEN_SAMTYKKE.equals(bekreftelse.getType()) && Boolean.TRUE.equals(bekreftelse.getVerdi()));
        var fikkFeilMotSkatteetaten = harSkatteetatenSamtykke && Boolean.TRUE.equals(driftsinformasjon.getInntektFraSkatteetatenFeilet());

        var sporsmal = new ArrayList<Sporsmal>();

        if (!harSkatteetatenSamtykke) {
            // du har ikke hentet informasjon fra skatteetaten
            sporsmal.add(
                    new Sporsmal.Builder()
                            .withTittel("utbetalinger.inntekt.skattbar.mangler_samtykke")
                            .withErUtfylt(true)
                            .build()
            );
        }

        if (harSkatteetatenSamtykke && fikkFeilMotSkatteetaten) {
            // bruke fikkFeilMotSkatteetaten til "noe"
        }

        if (harSkatteetatenSamtykke && !fikkFeilMotSkatteetaten) {
            sporsmal.add(
                    new Sporsmal.Builder()
                            .withTittel("utbetalinger.inntekt.skattbar.har_gitt_samtykke")
                            // undertittel? Tidspunkt for henting av inntekt fra Skatteetaten
                            .withErUtfylt(true)
                            .withFelt(skattbarInntektFelter(opplysninger.getUtbetaling()))
                            .build()
            );
        }

        return new Avsnitt.Builder()
                .withTittel("utbetalinger.inntekt.skattbar.tittel")
                .withSporsmal(sporsmal)
                .build();
    }

    private List<Felt> skattbarInntektFelter(List<JsonOkonomiOpplysningUtbetaling> utbetalinger) {
        var harSkattbareInntekter = utbetalinger != null && utbetalinger.stream().anyMatch(utbetaling -> UTBETALING_SKATTEETATEN.equals(utbetaling.getType()));

        if (!harSkattbareInntekter) {
            // mer info her?
            return Collections.emptyList();
        }

        // gruppere pr mnd og organisasjon?
        //  summere inntekter og skattetrekk pr org

        return utbetalinger.stream()
                .filter(utbetaling -> UTBETALING_SKATTEETATEN.equals(utbetaling.getType()))
                //.collect(Collectors.groupingBy()) ??
                .map(utbetaling -> {
                    var map = new LinkedHashMap<String, String>();
                    // inntekt fra organisasjon
                    map.put(utbetaling.getOrganisasjon().getNavn(), null);
                    // periode (fom - tom)
                    map.put(String.format("Fra %s til %s", utbetaling.getPeriodeFom(), utbetaling.getPeriodeTom()), null);
                    // bruttoinntekt
                    map.put("utbetalinger.utbetaling.brutto.label", utbetaling.getBrutto().toString());
                    // forskuddstrekk
                    map.put("utbetalinger.utbetaling.skattetrekk.label", utbetaling.getSkattetrekk().toString());

                    return new Felt.Builder()
                            .withType(Type.SYSTEMDATA_MAP)
                            .withLabelSvarMap(map)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Avsnitt navUtbetalingerAvsnitt() {
        return new Avsnitt.Builder()
                .withTittel("navytelser.sporsmal")
                .withSporsmal()
                .build();
    }

    private Avsnitt bostotteHusbankenAvsnitt() {
        return new Avsnitt.Builder()
                .withTittel("inntekt.bostotte.husbanken.tittel")
                .withSporsmal()
                .build();
    }

    private Avsnitt studielanAvsnitt() {
        return new Avsnitt.Builder()
                .withTittel("inntekt.studielan.titel")
                .withSporsmal()
                .build();
    }

    private Avsnitt andreInntekterAvsnitt(JsonOkonomi okonomi) {
        var harUtfyltAndreInntekterSporsmal = true;
        var harAnnenUtbetalingMedBeskrivelse = false;

        /*
        "json.okonomi.opplysninger.inntekt.inntekter.utbytte"
        "json.okonomi.opplysninger.inntekt.inntekter.salg"
        "json.okonomi.opplysninger.inntekt.inntekter.forsikringsutbetalinger"
        "json.okonomi.opplysninger.inntekt.inntekter.annet"
         */


        return new Avsnitt.Builder()
                .withTittel("inntekt.inntekter.titel")
                .withSporsmal()
                .build();
    }

    private Avsnitt bankAvsnitt() {
        return new Avsnitt.Builder()
                .withTittel("opplysninger.formue.bank.undertittel")
                .withSporsmal()
                .build();
    }

    private Avsnitt annenFormueAvsnitt() {
        return new Avsnitt.Builder()
                .withTittel("opplysninger.formue.annen.undertittel")
                .withSporsmal()
                .build();
    }

    private boolean erStudent(JsonUtdanning utdanning) {
        return utdanning != null && utdanning.getErStudent() != null && utdanning.getErStudent().equals(Boolean.TRUE);
    }
}
