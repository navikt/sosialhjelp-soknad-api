package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class ArbeidOgUtdanningSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        var arbeid = jsonInternalSoknad.getSoknad().getData().getArbeid();
        var utdanning = jsonInternalSoknad.getSoknad().getData().getUtdanning();

        return new Steg.Builder()
                .withStegNr(3)
                .withTittel("arbeidbolk.tittel")
                .withAvsnitt(
                        List.of(
                                new Avsnitt.Builder()
                                        .withTittel("arbeidsforhold.sporsmal")
                                        .withSporsmal(arbeidsforholdSporsmal(arbeid))
                                        .build(),
                                new Avsnitt.Builder()
                                        .withTittel("arbeid.dinsituasjon.studerer.undertittel")
                                        .withSporsmal(utdanningSporsmal(utdanning))
                                        .build()
                        )
                )
                .build();
    }

    private List<Sporsmal> arbeidsforholdSporsmal(JsonArbeid arbeid) {
        var harArbeidsforhold = arbeid.getForhold() != null && !arbeid.getForhold().isEmpty();
        var harKommentarTilArbeidsforhold = arbeid.getKommentarTilArbeidsforhold() != null && arbeid.getKommentarTilArbeidsforhold().getVerdi() != null;

        var sporsmal = new ArrayList<Sporsmal>();
        sporsmal.add(
                new Sporsmal.Builder()
                        .withTittel(harArbeidsforhold ? "arbeidsforhold.infotekst" : "arbeidsforhold.ingen")
                        .withFelt(harArbeidsforhold ? arbeidsforholdFelter(arbeid.getForhold()) : null)
                        .withErUtfylt(true)
                        .build()
        );
        if (harKommentarTilArbeidsforhold) {
            sporsmal.add(
                    new Sporsmal.Builder()
                            .withTittel("Kommentar til arbeidsforhold")
                            .withFelt(kommentarFelter(arbeid.getKommentarTilArbeidsforhold()))
                            .withErUtfylt(true)
                            .build()
            );
        }
        return sporsmal;
    }

    private List<Felt> arbeidsforholdFelter(List<JsonArbeidsforhold> arbeidsforholdList) {
        // arbeidsgiver, startet i jobben, (sluttet i jobben), stillingsprosent
        return arbeidsforholdList.stream()
                .map(this::toFelter)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private ArrayList<Felt> toFelter(JsonArbeidsforhold arbeidsforhold) {
        var felter = new ArrayList<Felt>();
        if (arbeidsforhold.getArbeidsgivernavn() != null) {
            felter.add(new Felt.Builder()
                    .withLabel("arbeidsforhold.arbeidsgivernavn.label")
                    .withSvar(arbeidsforhold.getArbeidsgivernavn())
                    .withType(Type.SYSTEMDATA)
                    .build()
            );
        }
        if (arbeidsforhold.getFom() != null) {
            felter.add(new Felt.Builder()
                    .withLabel("arbeidsforhold.fom.label")
                    .withSvar(arbeidsforhold.getFom())
                    .withType(Type.SYSTEMDATA)
                    .build()
            );
        }
        if (arbeidsforhold.getTom() != null) {
            felter.add(new Felt.Builder()
                    .withLabel("arbeidsforhold.tom.label")
                    .withSvar(arbeidsforhold.getTom())
                    .withType(Type.SYSTEMDATA)
                    .build()
            );
        }
        if (arbeidsforhold.getStillingsprosent() != null) {
            felter.add(new Felt.Builder()
                    .withLabel("arbeidsforhold.stillingsprosent.label")
                    .withSvar(arbeidsforhold.getStillingsprosent().toString())
                    .withType(Type.SYSTEMDATA)
                    .build()
            );
        }
        return felter;
    }

    private List<Felt> kommentarFelter(JsonKommentarTilArbeidsforhold kommentar) {
        return singletonList(
                new Felt.Builder()
                        .withSvar(kommentar.getVerdi())
                        .withType(Type.TEKST)
                        .build()
        );
    }

    private List<Sporsmal> utdanningSporsmal(JsonUtdanning utdanning) {
        var erUtdanningUtfylt = utdanning != null && utdanning.getErStudent() != null;
        var erStudent = erUtdanningUtfylt && utdanning.getErStudent().equals(Boolean.TRUE);
        var erStudentgradUtfylt = erStudent && utdanning.getStudentgrad() != null;

        var sporsmal = new ArrayList<Sporsmal>();
        sporsmal.add(
                new Sporsmal.Builder()
                        .withTittel("dinsituasjon.studerer.sporsmal")
                        .withFelt(erUtdanningUtfylt ? erStudentFelt(erStudent) : null)
                        .withErUtfylt(erUtdanningUtfylt)
                        .build()
        );

        if (erStudent) {
            sporsmal.add(
                    new Sporsmal.Builder()
                            .withTittel("dinsituasjon.studerer.true.grad.sporsmal")
                            .withFelt(erStudentgradUtfylt ? studentgradFelt(utdanning.getStudentgrad()) : null)
                            .withErUtfylt(erStudentgradUtfylt)
                            .build()
            );
        }
        return sporsmal;
    }

    private List<Felt> erStudentFelt(boolean erStudent) {
        return singletonList(new Felt.Builder().withSvar(String.valueOf(erStudent)).withType(Type.CHECKBOX).build());
    }

    private List<Felt> studentgradFelt(JsonUtdanning.Studentgrad studentgrad) {
        return singletonList(new Felt.Builder().withSvar(studentgrad.value()).withType(Type.CHECKBOX).build());
    }
}
