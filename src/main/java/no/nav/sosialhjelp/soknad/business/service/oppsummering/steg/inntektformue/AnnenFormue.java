package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_VERDI;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_ANNET;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_BOLIG;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_CAMPINGVOGN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_FRITIDSEIENDOM;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_KJORETOY;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.InntektFormueUtils.addFormueIfPresent;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelse;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelseTrue;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.InntektFormueUtils.harValgtFormueType;

public class AnnenFormue {

    public Avsnitt getAvsnitt(JsonOkonomi okonomi) {
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
}
