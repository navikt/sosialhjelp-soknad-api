package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> d8cdea3cd5 (utgifterOgGjeld steg 7)
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
<<<<<<< HEAD

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
=======
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
>>>>>>> d8cdea3cd5 (utgifterOgGjeld steg 7)

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;

public class UtgifterOgGjeldSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> d8cdea3cd5 (utgifterOgGjeld steg 7)
        var okonomi = jsonInternalSoknad.getSoknad().getData().getOkonomi();

        var boutgifterSporsmal = boutgifter(okonomi);
        var barneutgifterSporsmal = barneutgifter(okonomi);

        var alleSporsmal = new ArrayList<Sporsmal>();
        alleSporsmal.addAll(boutgifterSporsmal);
        alleSporsmal.addAll(barneutgifterSporsmal);
<<<<<<< HEAD

        return new Steg.Builder()
                .withStegNr(7)
                .withTittel("utgifterbolk.tittel") // Utgifter og gjeld
                .withAvsnitt(
                        singletonList(
                                new Avsnitt.Builder()
                                        .withTittel("utgifterbolk.tittel") // Utgifter og gjeld
                                        .withSporsmal(alleSporsmal)
                                        .build()
                        )
                )
                .build();
    }

    private List<Sporsmal> boutgifter(JsonOkonomi okonomi) {
        var boutgiftBekreftelser = okonomi.getOpplysninger().getBekreftelse().stream().filter(b -> "boutgifter".equals(b.getType())).collect(Collectors.toList());
        var erBoutgifterUtfylt = !boutgiftBekreftelser.isEmpty() && boutgiftBekreftelser.get(0).getVerdi() != null;
        var harBoutgifter = erBoutgifterUtfylt && boutgiftBekreftelser.get(0).getVerdi().equals(TRUE);

        var sporsmalList = new ArrayList<Sporsmal>();

        sporsmalList.add(
                new Sporsmal.Builder()
                        .withTittel("utgifter.boutgift.sporsmal")
                        .withErUtfylt(erBoutgifterUtfylt)
                        .withFelt(erBoutgifterUtfylt ? harBoutgifterFelt(harBoutgifter) : null)
                        .build()
        );

        if (erBoutgifterUtfylt && harBoutgifter) {
            var utgifter = okonomi.getOpplysninger().getUtgift();
            var oversiktUtgift = okonomi.getOversikt().getUtgift();

            var felter = new ArrayList<Felt>();
            addOversiktUtgiftIfPresent(felter, oversiktUtgift, "husleie", "utgifter.boutgift.true.type.husleie");
            addOpplysningUtgiftIfPresent(felter, utgifter, "strom", "utgifter.boutgift.true.type.strom");
            addOpplysningUtgiftIfPresent(felter, utgifter, "kommunalAvgift", "utgifter.boutgift.true.type.kommunalAvgift");
            addOpplysningUtgiftIfPresent(felter, utgifter, "oppvarming", "utgifter.boutgift.true.type.oppvarming");
            addOversiktUtgiftIfPresent(felter, oversiktUtgift, "boliglanAvdrag", "utgifter.boutgift.true.type.boliglanAvdrag");
            addOpplysningUtgiftIfPresent(felter, utgifter, "annenBoutgift", "utgifter.boutgift.true.type.annenBoutgift");

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

    private List<Sporsmal> barneutgifter(JsonOkonomi okonomi) {
        var barneutgiftBekreftelser = okonomi.getOpplysninger().getBekreftelse().stream().filter(b -> "barneutgifter".equals(b.getType())).collect(Collectors.toList());
        var erBarneutgifterUtfylt = !barneutgiftBekreftelser.isEmpty() && barneutgiftBekreftelser.get(0).getVerdi() != null;
        var harBarneutgifter = erBarneutgifterUtfylt && barneutgiftBekreftelser.get(0).getVerdi().equals(TRUE);

        var sporsmalList = new ArrayList<Sporsmal>();
        sporsmalList.add(
                new Sporsmal.Builder()
                        .withTittel("utgifter.barn.sporsmal")
                        .withErUtfylt(erBarneutgifterUtfylt)
                        .withFelt(erBarneutgifterUtfylt ? harBarneutgifterFelt(harBarneutgifter) : null)
                        .build()
        );

        if (erBarneutgifterUtfylt && harBarneutgifter) {
            var utgifter = okonomi.getOpplysninger().getUtgift();
            var oversiktUtgifter = okonomi.getOversikt().getUtgift();

            var felter = new ArrayList<Felt>();
            addOpplysningUtgiftIfPresent(felter, utgifter, "barnFritidsaktiviteter", "utgifter.barn.true.utgifter.barnFritidsaktiviteter");
            addOversiktUtgiftIfPresent(felter, oversiktUtgifter, "barnehage", "utgifter.barn.true.utgifter.barnehage");
            addOversiktUtgiftIfPresent(felter, oversiktUtgifter, "sfo", "utgifter.barn.true.utgifter.sfo");
            addOpplysningUtgiftIfPresent(felter, utgifter, "barnTannregulering", "utgifter.barn.true.utgifter.barnTannregulering");
            addOpplysningUtgiftIfPresent(felter, utgifter, "annenBarneutgift", "utgifter.barn.true.utgifter.annenBarneutgift");

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

    private List<Felt> harBoutgifterFelt(boolean harBoutgifter) {
        return singletonList(
                new Felt.Builder()
                        .withSvar(harBoutgifter ? "utgifter.boutgift.true" : "utgifter.boutgift.false")
                        .withType(Type.CHECKBOX)
                        .build()
        );
    }

    private List<Felt> harBarneutgifterFelt(boolean harBarneutgifter) {
        return singletonList(
                new Felt.Builder()
                        .withSvar(harBarneutgifter ? "utgifter.barn.true" : "utgifter.barn.false")
                        .withType(Type.CHECKBOX)
                        .build()
        );
    }

    private void addOpplysningUtgiftIfPresent(List<Felt> felter, List<JsonOkonomiOpplysningUtgift> utgifter, String type, String key) {
        // "strom", "kommunalAvgift", "oppvarming", "annenBoutgift", "barnFritidsaktiviteter", "barnTannregulering", “annenBarneutgift” og "annen"
        utgifter.stream()
                .filter(utgift -> type.equals(utgift.getType()))
                .findFirst()
                .ifPresent(utgift -> felter.add(
                        new Felt.Builder()
                                .withSvar(key)
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
                                .withSvar(key)
                                .withType(Type.CHECKBOX)
                                .build()
                ));
    }
=======
        // todo implement
        return null;
=======
        var utgifter = jsonInternalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtgift();
=======
>>>>>>> d8cdea3cd5 (utgifterOgGjeld steg 7)

        return new Steg.Builder()
                .withStegNr(7)
                .withTittel("utgifterbolk.tittel") // Utgifter og gjeld
                .withAvsnitt(
                        singletonList(
                                new Avsnitt.Builder()
                                        .withTittel("utgifterbolk.tittel") // Utgifter og gjeld
                                        .withSporsmal(alleSporsmal)
                                        .build()
                        )
                )
                .build();
>>>>>>> 75f48c35dc (utkast til de ulike stegene)
    }

<<<<<<< HEAD
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
    private List<Sporsmal> boutgifter(JsonOkonomi okonomi) {
        var boutgiftBekreftelser = okonomi.getOpplysninger().getBekreftelse().stream().filter(b -> "boutgifter".equals(b.getType())).collect(Collectors.toList());
        var erBoutgifterUtfylt = !boutgiftBekreftelser.isEmpty() && boutgiftBekreftelser.get(0).getVerdi() != null;
        var harBoutgifter = erBoutgifterUtfylt ? boutgiftBekreftelser.get(0).getVerdi() : null;

        var sporsmalList = new ArrayList<Sporsmal>();

        sporsmalList.add(
                new Sporsmal.Builder()
                        .withTittel("utgifter.boutgift.sporsmal")
                        .withErUtfylt(erBoutgifterUtfylt)
                        .withFelt(erBoutgifterUtfylt
                                ? singletonList(new Felt.Builder().withSvar(harBoutgifter.toString()).withType(Type.CHECKBOX).build())
                                : null)
                        .build()
        );

        if (erBoutgifterUtfylt && TRUE.equals(harBoutgifter)) {
            var utgifter = okonomi.getOpplysninger().getUtgift();
            var oversiktUtgift = okonomi.getOversikt().getUtgift();

            var felter = new ArrayList<Felt>();
            addOversiktUtgiftIfPresent(felter, oversiktUtgift, "husleie", "utgifter.boutgift.true.type.husleie");
            addOpplysningUtgiftIfPresent(felter, utgifter, "strom", "utgifter.boutgift.true.type.strom");
            addOpplysningUtgiftIfPresent(felter, utgifter, "kommunalAvgift", "utgifter.boutgift.true.type.kommunalAvgift");
            addOpplysningUtgiftIfPresent(felter, utgifter, "oppvarming", "utgifter.boutgift.true.type.oppvarming");
            addOversiktUtgiftIfPresent(felter, oversiktUtgift, "boliglanAvdrag", "utgifter.boutgift.true.type.boliglanAvdrag");
            addOpplysningUtgiftIfPresent(felter, utgifter, "annenBoutgift", "utgifter.boutgift.true.type.annenBoutgift");

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

    private List<Sporsmal> barneutgifter(JsonOkonomi okonomi) {
        var barneutgiftBekreftelser = okonomi.getOpplysninger().getBekreftelse().stream().filter(b -> "barneutgifter".equals(b.getType())).collect(Collectors.toList());
        var erBarneutgifterUtfylt = !barneutgiftBekreftelser.isEmpty() && barneutgiftBekreftelser.get(0).getVerdi() != null;
        var harBarneutgifter = erBarneutgifterUtfylt ? barneutgiftBekreftelser.get(0).getVerdi() : null;

        var sporsmalList = new ArrayList<Sporsmal>();
        sporsmalList.add(
                new Sporsmal.Builder()
                        .withTittel("utgifter.barn.sporsmal")
                        .withErUtfylt(erBarneutgifterUtfylt)
                        .withFelt(erBarneutgifterUtfylt
                                ? singletonList(new Felt.Builder().withSvar(harBarneutgifter.toString()).withType(Type.CHECKBOX).build())
                                : null)
                        .build()
        );

        if (erBarneutgifterUtfylt && TRUE.equals(harBarneutgifter)) {
            var utgifter = okonomi.getOpplysninger().getUtgift();
            var oversiktUtgifter = okonomi.getOversikt().getUtgift();

            var felter = new ArrayList<Felt>();
            addOpplysningUtgiftIfPresent(felter, utgifter, "barnFritidsaktiviteter", "utgifter.barn.true.utgifter.barnFritidsaktiviteter");
            addOversiktUtgiftIfPresent(felter, oversiktUtgifter, "barnehage", "utgifter.barn.true.utgifter.barnehage");
            addOversiktUtgiftIfPresent(felter, oversiktUtgifter, "sfo", "utgifter.barn.true.utgifter.sfo");
            addOpplysningUtgiftIfPresent(felter, utgifter, "barnTannregulering", "utgifter.barn.true.utgifter.barnTannregulering");
            addOpplysningUtgiftIfPresent(felter, utgifter, "annenBarneutgift", "utgifter.barn.true.utgifter.annenBarneutgift");

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
                                .withSvar(key)
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
                                .withSvar(key)
                                .withType(Type.CHECKBOX)
                                .build()
                ));
    }
>>>>>>> d8cdea3cd5 (utgifterOgGjeld steg 7)
}
