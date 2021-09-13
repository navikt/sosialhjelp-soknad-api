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

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
import static java.util.Collections.emptyList;
=======
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
>>>>>>> ae6b7e6c66 (arbeid og utdanning steg. wip)

=======
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
import static java.util.Collections.emptyList;

>>>>>>> 75f48c35dc (utkast til de ulike stegene)
public class ArbeidOgUtdanningSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
<<<<<<< HEAD
        // todo implement
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 75f48c35dc (utkast til de ulike stegene)
        return new Steg.Builder()
                .withStegNr(3)
                .withTittel("arbeidbolk.tittel")
                .withAvsnitt(emptyList())
<<<<<<< HEAD
<<<<<<< HEAD
                .build();
=======
        return null;
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
                .withErFerdigUtfylt(true)
=======
>>>>>>> d2f2735ecc (`erUtfylt` pr sporsmal, ikke pr steg)
=======
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
>>>>>>> ae6b7e6c66 (arbeid og utdanning steg. wip)
                .build();
>>>>>>> 75f48c35dc (utkast til de ulike stegene)
    }

    private List<Sporsmal> arbeidsforholdSporsmal(JsonArbeid arbeid) {
        var harArbeidsforhold = arbeid.getForhold() != null && !arbeid.getForhold().isEmpty();
        var harKommentarTilArbeidsforhold = arbeid.getKommentarTilArbeidsforhold() != null && arbeid.getKommentarTilArbeidsforhold().getVerdi() != null;

        return List.of(
                new Sporsmal.Builder()
                        .withTittel(harArbeidsforhold ? "arbeidsforhold.infotekst" : "arbeidsforhold.ingen")
                        .withFelt(harArbeidsforhold ? arbeidsforholdFelter(arbeid.getForhold()) : null)
                        .withErUtfylt(true)
                        .build(),
                new Sporsmal.Builder()
                        .withTittel("Kommentar til arbeidsforhold")
                        .withFelt(harKommentarTilArbeidsforhold ? kommentarFelter(arbeid.getKommentarTilArbeidsforhold()) : null)
                        .withErUtfylt(true)
                        .build()
        );
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
        // todo

        return null;
    }
}
