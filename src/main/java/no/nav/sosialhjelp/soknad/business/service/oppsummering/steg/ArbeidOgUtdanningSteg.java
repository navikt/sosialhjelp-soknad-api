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
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Svar;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning.Studentgrad.HELTID;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.booleanVerdiFelt;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.createSvar;

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
                            .withTittel("opplysninger.arbeidsituasjon.kommentarer.label")
                            .withFelt(kommentarFelter(arbeid.getKommentarTilArbeidsforhold()))
                            .withErUtfylt(true)
                            .build()
            );
        }
        return sporsmal;
    }

    private List<Felt> arbeidsforholdFelter(List<JsonArbeidsforhold> arbeidsforholdList) {

        return arbeidsforholdList.stream()
                .map(this::toFelt)
                .collect(Collectors.toList());
    }

    private Felt toFelt(JsonArbeidsforhold arbeidsforhold) {
        // arbeidsgiver, startet i jobben, (sluttet i jobben), stillingsprosent
        var labelSvarMap = new LinkedHashMap<String, Svar>();
        if (arbeidsforhold.getArbeidsgivernavn() != null) {
            labelSvarMap.put("arbeidsforhold.arbeidsgivernavn.label", createSvar(arbeidsforhold.getArbeidsgivernavn(), SvarType.TEKST));
        }
        if (arbeidsforhold.getFom() != null) {
            labelSvarMap.put("arbeidsforhold.fom.label", createSvar(arbeidsforhold.getFom(), SvarType.DATO));
        }
        if (arbeidsforhold.getTom() != null) {
            labelSvarMap.put("arbeidsforhold.tom.label", createSvar(arbeidsforhold.getTom(), SvarType.DATO));
        }
        if (arbeidsforhold.getStillingsprosent() != null) {
            labelSvarMap.put("arbeidsforhold.stillingsprosent.label", createSvar(arbeidsforhold.getStillingsprosent().toString(), SvarType.TEKST));
        }
        return new Felt.Builder()
                .withType(Type.SYSTEMDATA_MAP)
                .withLabelSvarMap(labelSvarMap)
                .build();
    }

    private List<Felt> kommentarFelter(JsonKommentarTilArbeidsforhold kommentar) {
        return singletonList(
                new Felt.Builder()
                        .withSvar(createSvar(kommentar.getVerdi(), SvarType.TEKST))
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
                        .withFelt(erUtdanningUtfylt ?
                                booleanVerdiFelt(erStudent, "dinsituasjon.studerer.true", "dinsituasjon.studerer.false") :
                                null
                        )
                        .withErUtfylt(erUtdanningUtfylt)
                        .build()
        );

        if (erStudent) {
            sporsmal.add(
                    new Sporsmal.Builder()
                            .withTittel("dinsituasjon.studerer.true.grad.sporsmal")
                            .withFelt(erStudentgradUtfylt ?
                                    booleanVerdiFelt(HELTID.equals(utdanning.getStudentgrad()), "dinsituasjon.studerer.true.grad.heltid", "dinsituasjon.studerer.true.grad.deltid") :
                                    null
                            )
                            .withErUtfylt(erStudentgradUtfylt)
                            .build()
            );
        }
        return sporsmal;
    }
}
