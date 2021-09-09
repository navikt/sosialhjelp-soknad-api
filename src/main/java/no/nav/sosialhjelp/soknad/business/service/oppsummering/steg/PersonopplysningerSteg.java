package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class PersonopplysningerSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        var personalia = jsonInternalSoknad.getSoknad().getData().getPersonalia();

        return new Steg.Builder()
                .withStegNr(1)
                .withTittel("personaliabolk.tittel")
                .withAvsnitt(
                        List.of(
                                personaliaAvsnitt(personalia),
                                adresseOgNavKontorAvsnitt(personalia),
                                telefonnummerAvsnitt(personalia),
                                kontonummerAvsnitt(personalia)
                        )
                )
                .build();
    }

    private Avsnitt personaliaAvsnitt(JsonPersonalia personalia) {
        return new Avsnitt.Builder()
                .withTittel("kontakt.system.personalia.sporsmal")
                .withSporsmal(
                        singletonList(
                                new Sporsmal.Builder()
                                        .withTittel("kontakt.system.personalia.infotekst.tekst")
                                        .withErUtfylt(true)
                                        .withFelt(
                                                List.of(
                                                        new Felt.Builder()
                                                                .withLabel("kontakt.system.personalia.navn")
                                                                .withSvar(personalia.getNavn().getFornavn()) // todo helt navn
                                                                .withType(Type.SYSTEMDATA)
                                                                .build(),
                                                        new Felt.Builder()
                                                                .withLabel("kontakt.system.personalia.fnr")
                                                                .withSvar(personalia.getPersonIdentifikator().getVerdi())
                                                                .withType(Type.SYSTEMDATA)
                                                                .build(),
                                                        new Felt.Builder()
                                                                .withLabel("kontakt.system.personalia.statsborgerskap")
                                                                .withSvar(personalia.getStatsborgerskap().getVerdi())
                                                                .withType(Type.SYSTEMDATA)
                                                                .build()
                                                )
                                        )
                                        .build()
                        )
                ).build();
    }

    private Avsnitt adresseOgNavKontorAvsnitt(JsonPersonalia personalia) {
        var oppholdsadresse = personalia.getOppholdsadresse();
        var adressetype = oppholdsadresse.getType();
        // gatenavn, husnummer, husbokstav, postnummer, poststed
        // adressevalg -> folkeregistrert, midlertidig, soknad
        return new Avsnitt.Builder()
                .withTittel("soknadsmottaker.sporsmal")
                .withSporsmal(
                        List.of(
                                new Sporsmal.Builder()
                                        .withTittel("")
                                        .withErUtfylt(true)
                                        .withFelt(emptyList())
                                        .build()
                        )
                )
                .build();
    }

    private Avsnitt telefonnummerAvsnitt(JsonPersonalia personalia) {
        var telefonnummer = personalia.getTelefonnummer();
        var harUtfyltTelefonnummer = telefonnummer != null && !telefonnummer.getVerdi().isEmpty();

        return new Avsnitt.Builder()
                .withTittel("kontakt.system.telefoninfo.sporsmal") // skal variere ut fra kilde? systemdata eller bruker
                .withSporsmal(
                        List.of(
                                new Sporsmal.Builder()
                                        .withTittel("kontakt.system.telefoninfo.infotekst.tekst")
                                        .withErUtfylt(harUtfyltTelefonnummer)
                                        .withFelt(harUtfyltTelefonnummer ? telefonnummerFelt(telefonnummer) : null)
                                        .build()
                        )
                )
                .build();
    }

    private List<Felt> telefonnummerFelt(JsonTelefonnummer telefonnummer) {
        var erSystemdata = telefonnummer.getKilde().equals(JsonKilde.SYSTEM);
        return singletonList(
                new Felt.Builder()
                        .withLabel("kontakt.system.telefon.label")
                        .withSvar(telefonnummer.getVerdi())
                        .withType(erSystemdata ? Type.SYSTEMDATA : Type.TEKST)
                        .build()
        );
    }

    private Avsnitt kontonummerAvsnitt(JsonPersonalia personalia) {
        var kontonummer = personalia.getKontonummer();
        var harUtfyltKontonummer = kontonummer != null && (TRUE.equals(kontonummer.getHarIkkeKonto()) || !kontonummer.getVerdi().isEmpty());

        return new Avsnitt.Builder()
                .withTittel("kontakt.system.kontonummer.sporsmal") // skal variere ut fra kilde? systemdata eller bruker
                .withSporsmal(
                        List.of(
                                new Sporsmal.Builder()
                                        .withTittel("kontakt.system.kontonummer.label")
                                        .withErUtfylt(harUtfyltKontonummer)
                                        .withFelt(harUtfyltKontonummer ? kontonummerFelt(kontonummer) : null)
                                        .build()
                        )
                )
                .build();
    }

    private List<Felt> kontonummerFelt(JsonKontonummer kontonummer) {
        if (TRUE.equals(kontonummer.getHarIkkeKonto())) {
            return singletonList(
                    new Felt.Builder()
                            .withSvar("kontakt.kontonummer.harikke.true")
                            .withType(Type.CHECKBOX)
                            .build()
            );
        }
        var erSystemdata = kontonummer.getKilde().equals(JsonKilde.SYSTEM);

        return singletonList(
                new Felt.Builder()
                        .withLabel("kontakt.system.kontonummer.label")
                        .withSvar(kontonummer.getVerdi())
                        .withType(erSystemdata ? Type.SYSTEMDATA : Type.TEKST)
                        .build()
        );
    }
}
