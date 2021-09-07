package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.createAvsnitt;

public class PersonopplysningerSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        var personalia = jsonInternalSoknad.getSoknad().getData().getPersonalia();

        return new Steg.Builder()
                .withStegNr(1)
                .withTittel("personaliabolk.tittel")
                .withAvsnitt(List.of(
                                createAvsnitt(
                                        "kontakt.system.personalia.sporsmal",
                                        singletonList(new Sporsmal(
                                                "kontakt.system.personalia.infotekst.tekst",
                                                List.of(
                                                        new Felt("kontakt.system.personalia.navn", personalia.getNavn().getFornavn(), Type.TEKST),
                                                        new Felt("kontakt.system.personalia.fnr", personalia.getPersonIdentifikator().getVerdi(), Type.TEKST),
                                                        new Felt("kontakt.system.personalia.statsborgerskap", personalia.getStatsborgerskap().getVerdi(), Type.TEKST)
                                                )
                                        ))
                                ),
                                createAvsnitt("soknadsmottaker.sporsmal", emptyList()),
                                createAvsnitt("kontakt.system.telefoninfo.sporsmal", emptyList()),
                                createAvsnitt("kontakt.system.kontonummer.sporsmal", emptyList())
                        )
                )
                .withErFerdigUtfylt(true)
                .build();
    }
}
