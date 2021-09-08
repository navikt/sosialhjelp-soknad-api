package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.createAvsnitt;

public class BegrunnelseSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        var begrunnelse = jsonInternalSoknad.getSoknad().getData().getBegrunnelse();

        return new Steg.Builder()
                .withStegNr(2)
                .withTittel("begrunnelsebolk.tittel")
                .withAvsnitt(List.of(
                                createAvsnitt(
                                        "applikasjon.sidetittel.kortnavn",
                                        List.of(
                                                new Sporsmal.Builder()
                                                        .withTittel("begrunnelse.hva.sporsmal")
                                                        .withFelt(singletonList(
                                                                new Felt.Builder()
                                                                        .withSvar(begrunnelse.getHvaSokesOm())
                                                                        .withType(Type.TEKST)
                                                                        .build()))
                                                        .build(),
                                                new Sporsmal.Builder()
                                                        .withTittel("begrunnelse.hvorfor.sporsmal")
                                                        .withFelt(singletonList(
                                                                new Felt.Builder()
                                                                        .withSvar(begrunnelse.getHvorforSoke())
                                                                        .withType(Type.TEKST)
                                                                        .build()))
                                                        .build()
                                                )
                                        )
                                )
                        )
                .build();
    }
}
