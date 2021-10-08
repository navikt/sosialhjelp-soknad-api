package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_BARNEUTGIFTER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BARN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BO;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARNEHAGE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_HUSLEIE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_OPPVARMING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_SFO;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_STROM;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.booleanVerdiFelt;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.createSvar;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.harBarnMedKilde;

public class UtgifterOgGjeldSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        var okonomi = jsonInternalSoknad.getSoknad().getData().getOkonomi();
        var forsorgerplikt = jsonInternalSoknad.getSoknad().getData().getFamilie().getForsorgerplikt();

        var boutgifterSporsmal = boutgifter(okonomi);
        var barneutgifterSporsmal = barneutgifter(okonomi);

        var alleSporsmal = new ArrayList<>(boutgifterSporsmal);
        if (harBarn(forsorgerplikt)) {
            alleSporsmal.addAll(barneutgifterSporsmal);
        }

        return new Steg.Builder()
                .withStegNr(7)
                .withTittel("utgifterbolk.tittel")
                .withAvsnitt(
                        singletonList(
                                new Avsnitt.Builder()
                                        .withTittel("utgifter.tittel")
                                        .withSporsmal(alleSporsmal)
                                        .build()
                        )
                )
                .build();
    }

    private List<Sporsmal> boutgifter(JsonOkonomi okonomi) {
        var boutgiftBekreftelser = okonomi.getOpplysninger().getBekreftelse().stream().filter(b -> BEKREFTELSE_BOUTGIFTER.equals(b.getType())).collect(Collectors.toList());
        var erBoutgifterUtfylt = !boutgiftBekreftelser.isEmpty() && boutgiftBekreftelser.get(0).getVerdi() != null;
        var harBoutgifter = erBoutgifterUtfylt && boutgiftBekreftelser.get(0).getVerdi().equals(TRUE);

        var sporsmalList = new ArrayList<Sporsmal>();

        sporsmalList.add(
                new Sporsmal.Builder()
                        .withTittel("utgifter.boutgift.sporsmal")
                        .withErUtfylt(erBoutgifterUtfylt)
                        .withFelt(erBoutgifterUtfylt ?
                                booleanVerdiFelt(harBoutgifter, "utgifter.boutgift.true", "utgifter.boutgift.false") :
                                null
                        )
                        .build()
        );

        if (erBoutgifterUtfylt && harBoutgifter) {
            var utgifter = okonomi.getOpplysninger().getUtgift();
            var oversiktUtgift = okonomi.getOversikt().getUtgift();

            var felter = new ArrayList<Felt>();
            addOversiktUtgiftIfPresent(felter, oversiktUtgift, UTGIFTER_HUSLEIE, "utgifter.boutgift.true.type.husleie");
            addOpplysningUtgiftIfPresent(felter, utgifter, UTGIFTER_STROM, "utgifter.boutgift.true.type.strom");
            addOpplysningUtgiftIfPresent(felter, utgifter, UTGIFTER_KOMMUNAL_AVGIFT, "utgifter.boutgift.true.type.kommunalAvgift");
            addOpplysningUtgiftIfPresent(felter, utgifter, UTGIFTER_OPPVARMING, "utgifter.boutgift.true.type.oppvarming");
            addOversiktUtgiftIfPresent(felter, oversiktUtgift, UTGIFTER_BOLIGLAN_AVDRAG, "utgifter.boutgift.true.type.boliglanAvdrag");
            addOpplysningUtgiftIfPresent(felter, utgifter, UTGIFTER_ANNET_BO, "utgifter.boutgift.true.type.annenBoutgift");

            sporsmalList.add(
                    new Sporsmal.Builder()
                            .withTittel("utgifter.boutgift.true.type.sporsmal")
                            .withFelt(felter)
                            .withErUtfylt(true)
                            .build()
            );
        }

        return sporsmalList;
    }

    private boolean harBarn(JsonForsorgerplikt forsorgerplikt) {
        return harBarnMedKilde(forsorgerplikt, JsonKilde.SYSTEM) || harBarnMedKilde(forsorgerplikt, JsonKilde.BRUKER);
    }

    private List<Sporsmal> barneutgifter(JsonOkonomi okonomi) {
        var barneutgiftBekreftelser = okonomi.getOpplysninger().getBekreftelse().stream().filter(b -> BEKREFTELSE_BARNEUTGIFTER.equals(b.getType())).collect(Collectors.toList());
        var erBarneutgifterUtfylt = !barneutgiftBekreftelser.isEmpty() && barneutgiftBekreftelser.get(0).getVerdi() != null;
        var harBarneutgifter = erBarneutgifterUtfylt && barneutgiftBekreftelser.get(0).getVerdi().equals(TRUE);

        var sporsmalList = new ArrayList<Sporsmal>();
        sporsmalList.add(
                new Sporsmal.Builder()
                        .withTittel("utgifter.barn.sporsmal")
                        .withErUtfylt(erBarneutgifterUtfylt)
                        .withFelt(erBarneutgifterUtfylt ?
                                booleanVerdiFelt(harBarneutgifter, "utgifter.barn.true", "utgifter.barn.false") :
                                null
                        )
                        .build()
        );

        if (erBarneutgifterUtfylt && harBarneutgifter) {
            var utgifter = okonomi.getOpplysninger().getUtgift();
            var oversiktUtgifter = okonomi.getOversikt().getUtgift();

            var felter = new ArrayList<Felt>();
            addOpplysningUtgiftIfPresent(felter, utgifter, UTGIFTER_BARN_FRITIDSAKTIVITETER, "utgifter.barn.true.utgifter.barnFritidsaktiviteter");
            addOversiktUtgiftIfPresent(felter, oversiktUtgifter, UTGIFTER_BARNEHAGE, "utgifter.barn.true.utgifter.barnehage");
            addOversiktUtgiftIfPresent(felter, oversiktUtgifter, UTGIFTER_SFO, "utgifter.barn.true.utgifter.sfo");
            addOpplysningUtgiftIfPresent(felter, utgifter, UTGIFTER_BARN_TANNREGULERING, "utgifter.barn.true.utgifter.barnTannregulering");
            addOpplysningUtgiftIfPresent(felter, utgifter, UTGIFTER_ANNET_BARN, "utgifter.barn.true.utgifter.annenBarneutgift");

            sporsmalList.add(
                    new Sporsmal.Builder()
                            .withTittel("utgifter.barn.true.utgifter.sporsmal")
                            .withFelt(felter)
                            .withErUtfylt(true)
                            .build()
            );
        }

        return sporsmalList;
    }

    private void addOpplysningUtgiftIfPresent(List<Felt> felter, List<JsonOkonomiOpplysningUtgift> utgifter, String type, String key) {
        // "strom", "kommunalAvgift", "oppvarming", "annenBoutgift", "barnFritidsaktiviteter", "barnTannregulering", “annenBarneutgift” og "annen"
        utgifter.stream()
                .filter(utgift -> type.equals(utgift.getType()))
                .findFirst()
                .ifPresent(utgift -> felter.add(
                        new Felt.Builder()
                                .withSvar(createSvar(key, SvarType.LOCALE_TEKST))
                                .withType(Type.CHECKBOX)
                                .build()
                ));
    }

    private void addOversiktUtgiftIfPresent(List<Felt> felter, List<JsonOkonomioversiktUtgift> utgifter, String type, String key) {
        // "barnebidrag", "husleie", "boliglanAvdrag", "boliglanRenter", “barnehage” og "sfo"
        utgifter.stream()
                .filter(utgift -> type.equals(utgift.getType()))
                .findFirst()
                .ifPresent(utgift -> felter.add(
                        new Felt.Builder()
                                .withSvar(createSvar(key, SvarType.LOCALE_TEKST))
                                .withType(Type.CHECKBOX)
                                .build()
                ));
    }
}
