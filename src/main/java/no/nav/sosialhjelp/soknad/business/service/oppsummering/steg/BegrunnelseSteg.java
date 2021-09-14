package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
<<<<<<< HEAD
<<<<<<< HEAD
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
=======
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
>>>>>>> 445b6b610d (bruk avsnitt builder)
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.List;

import static java.util.Collections.singletonList;
<<<<<<< HEAD
<<<<<<< HEAD
=======
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.createAvsnitt;
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
>>>>>>> 445b6b610d (bruk avsnitt builder)

public class BegrunnelseSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        var begrunnelse = jsonInternalSoknad.getSoknad().getData().getBegrunnelse();

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
        var harUtfyltHvaSokesOm = begrunnelse.getHvaSokesOm() != null && !begrunnelse.getHvaSokesOm().isEmpty();
        var harUtfyltHvorforSoke = begrunnelse.getHvorforSoke() != null && !begrunnelse.getHvorforSoke().isEmpty();

        return new Steg.Builder()
                .withStegNr(2)
                .withTittel("begrunnelsebolk.tittel")
                .withAvsnitt(
                        singletonList(
                                new Avsnitt.Builder()
                                        .withTittel("applikasjon.sidetittel.kortnavn")
                                        .withSporsmal(
                                                List.of(
                                                        new Sporsmal.Builder()
                                                                .withTittel("begrunnelse.hva.sporsmal")
                                                                .withErUtfylt(harUtfyltHvaSokesOm)
                                                                .withFelt(harUtfyltHvaSokesOm ? hvaSokerOmFelt(begrunnelse) : null)
                                                                .build(),
                                                        new Sporsmal.Builder()
                                                                .withTittel("begrunnelse.hvorfor.sporsmal")
                                                                .withErUtfylt(harUtfyltHvorforSoke)
                                                                .withFelt(harUtfyltHvorforSoke ? hvorforSokeFelt(begrunnelse) : null)
                                                                .build()
                                                )
                                        ).build()
                        )
                )
                .build();
    }

    private List<Felt> hvaSokerOmFelt(JsonBegrunnelse begrunnelse) {
        return singletonList(
                new Felt.Builder()
                        .withSvar(begrunnelse.getHvaSokesOm())
                        .withType(Type.TEKST)
                        .build());
    }

    private List<Felt> hvorforSokeFelt(JsonBegrunnelse begrunnelse) {
        return singletonList(
                new Felt.Builder()
                        .withSvar(begrunnelse.getHvorforSoke())
                        .withType(Type.TEKST)
                        .build());

    }
=======
=======
        var harUtfyltHvaSokesOm = !begrunnelse.getHvaSokesOm().isEmpty();
        var harUtfyltHvorforSoke = !begrunnelse.getHvorforSoke().isEmpty();
=======
        var harUtfyltHvaSokesOm = begrunnelse.getHvaSokesOm() != null && !begrunnelse.getHvaSokesOm().isEmpty();
        var harUtfyltHvorforSoke = begrunnelse.getHvorforSoke() != null && !begrunnelse.getHvorforSoke().isEmpty();
>>>>>>> b4e49cb130 (begrunnelseSteg test)

>>>>>>> 445b6b610d (bruk avsnitt builder)
        return new Steg.Builder()
                .withStegNr(2)
                .withTittel("begrunnelsebolk.tittel")
                .withAvsnitt(
                        singletonList(
                                new Avsnitt.Builder()
                                        .withTittel("applikasjon.sidetittel.kortnavn")
                                        .withSporsmal(
                                                List.of(
                                                        new Sporsmal.Builder()
                                                                .withTittel("begrunnelse.hva.sporsmal")
                                                                .withErUtfylt(harUtfyltHvaSokesOm)
                                                                .withFelt(harUtfyltHvaSokesOm ? hvaSokerOmFelt(begrunnelse) : null)
                                                                .build(),
                                                        new Sporsmal.Builder()
                                                                .withTittel("begrunnelse.hvorfor.sporsmal")
                                                                .withErUtfylt(harUtfyltHvorforSoke)
                                                                .withFelt(harUtfyltHvorforSoke ? hvorforSokeFelt(begrunnelse) : null)
                                                                .build()
                                                )
                                        ).build()
                        )
                )
                .build();
    }
<<<<<<< HEAD
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======

    private List<Felt> hvaSokerOmFelt(JsonBegrunnelse begrunnelse) {
        return singletonList(
                new Felt.Builder()
                        .withSvar(begrunnelse.getHvaSokesOm())
                        .withType(Type.TEKST)
                        .build());
    }

    private List<Felt> hvorforSokeFelt(JsonBegrunnelse begrunnelse) {
        return singletonList(
                new Felt.Builder()
                        .withSvar(begrunnelse.getHvorforSoke())
                        .withType(Type.TEKST)
                        .build());

    }
>>>>>>> 445b6b610d (bruk avsnitt builder)
}
