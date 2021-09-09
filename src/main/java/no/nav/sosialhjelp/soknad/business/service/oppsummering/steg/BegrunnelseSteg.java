package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.List;

import static java.util.Collections.singletonList;

public class BegrunnelseSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        var begrunnelse = jsonInternalSoknad.getSoknad().getData().getBegrunnelse();

        var harUtfyltHvaSokesOm = !begrunnelse.getHvaSokesOm().isEmpty();
        var harUtfyltHvorforSoke = !begrunnelse.getHvorforSoke().isEmpty();

        return new Steg.Builder()
                .withStegNr(2)
                .withTittel("begrunnelsebolk.tittel")
                .withAvsnitt(
                        List.of(
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
                        .withSvar(begrunnelse.getHvaSokesOm())
                        .withType(Type.TEKST)
                        .build());

    }
}
