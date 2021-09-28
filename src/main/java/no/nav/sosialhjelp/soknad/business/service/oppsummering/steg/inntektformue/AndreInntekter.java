package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_UTBETALING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_ANNET;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_FORSIKRING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SALG;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_UTBYTTE;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.booleanVerdiFelt;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelse;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelseTrue;

public class AndreInntekter {

    public Avsnitt getAvsnitt(JsonOkonomiopplysninger opplysninger) {
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
                        .withFelt(harUtfyltAndreInntekterSporsmal ?
                                booleanVerdiFelt(harSvartJaAndreInntekter, "inntekt.inntekter.true", "inntekt.inntekter.false") :
                                null
                        )
                        .build()
        );

        if (harSvartJaAndreInntekter) {
            var harSvartHvaHarDuMottattSporsmal = opplysninger.getUtbetaling().stream().anyMatch(utbetaling -> utbetalingTyper.contains(utbetaling.getType()));
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
}
