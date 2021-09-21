package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_SPARING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_ANNET;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BRUKSKONTO;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BSU;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_LIVSFORSIKRING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_SPAREKONTO;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_VERDIPAPIRER;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.InntektFormueUtils.addFormueIfPresent;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.InntektFormueUtils.harBekreftelseTrue;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue.InntektFormueUtils.harValgtFormueType;

public class Bank {

    public Avsnitt getAvsnitt(JsonOkonomi okonomi) {
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

        if (harUtfyltBankSporsmal && harValgtFormueType(oversikt, FORMUE_ANNET)) {
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

}
