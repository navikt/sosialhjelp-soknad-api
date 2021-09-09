package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class PersonopplysningerSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        var personalia = jsonInternalSoknad.getSoknad().getData().getPersonalia();

        return new Steg.Builder()
                .withStegNr(1)
                .withTittel("personaliabolk.tittel")
                .withAvsnitt(List.of(
                                new Avsnitt.Builder()
                                        .withTittel("kontakt.system.personalia.sporsmal")
                                        .withSporsmal(
                                                singletonList(
                                                        new Sporsmal.Builder()
                                                                .withTittel("kontakt.system.personalia.infotekst.tekst")
                                                                .withFelt(List.of(
                                                                        new Felt("kontakt.system.personalia.navn", personalia.getNavn().getFornavn(), Type.TEKST),
                                                                        new Felt("kontakt.system.personalia.fnr", personalia.getPersonIdentifikator().getVerdi(), Type.TEKST),
                                                                        new Felt("kontakt.system.personalia.statsborgerskap", personalia.getStatsborgerskap().getVerdi(), Type.TEKST)
                                                                ))
                                                                .withErUtfylt(true)
                                                                .build()
                                                )
                                        ).build(),
                                new Avsnitt.Builder()
                                        .withTittel("soknadsmottaker.sporsmal")
                                        .withSporsmal(emptyList())
                                        .build(),
                                new Avsnitt.Builder()
                                        .withTittel("kontakt.system.telefoninfo.sporsmal")
                                        .withSporsmal(emptyList())
                                        .build(),
                                new Avsnitt.Builder()
                                        .withTittel("kontakt.system.kontonummer.sporsmal")
                                        .withSporsmal(emptyList())
                                        .build()
                        )
                )
                .build();
    }
}
