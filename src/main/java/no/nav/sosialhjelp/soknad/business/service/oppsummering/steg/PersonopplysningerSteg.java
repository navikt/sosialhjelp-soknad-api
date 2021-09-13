package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;

public class PersonopplysningerSteg {

    private static final Logger log = getLogger(PersonopplysningerSteg.class);

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
                                                                .withSvar(fulltnavn(personalia.getNavn()))
                                                                .withType(Type.SYSTEMDATA)
                                                                .build(),
                                                        new Felt.Builder()
                                                                .withLabel("kontakt.system.personalia.fnr")
                                                                .withSvar(personalia.getPersonIdentifikator().getVerdi())
                                                                .withType(Type.SYSTEMDATA)
                                                                .build(),
                                                        new Felt.Builder()
                                                                .withLabel("kontakt.system.personalia.statsborgerskap")
                                                                .withSvar(Optional.ofNullable(personalia.getStatsborgerskap()).map(JsonStatsborgerskap::getVerdi).orElse(null))
                                                                .withType(Type.SYSTEMDATA)
                                                                .build()
                                                )
                                        )
                                        .build()
                        )
                ).build();
    }

    private String fulltnavn(JsonNavn navn) {
        if (navn == null) {
            log.warn("Personalia.getNavn er null?");
            return "";
        }

        var optionalFornavn = Optional.ofNullable(navn.getFornavn());
        var optionalMellomnavn = Optional.ofNullable(navn.getMellomnavn());
        var optionalEtternavn = Optional.ofNullable(navn.getEtternavn());

        return Stream.of(optionalFornavn, optionalMellomnavn, optionalEtternavn)
                .map(opt -> opt.orElse(""))
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));
    }

    private Avsnitt adresseOgNavKontorAvsnitt(JsonPersonalia personalia) {
        var oppholdsadresse = personalia.getOppholdsadresse();

        return new Avsnitt.Builder()
                .withTittel("soknadsmottaker.sporsmal")
                .withSporsmal(
                        singletonList(
                                new Sporsmal.Builder()
                                        .withTittel("soknadsmottaker.infotekst.tekst")
                                        .withErUtfylt(true)
                                        .withFelt(
                                                singletonList(
                                                        new Felt.Builder()
                                                                .withLabel(adresseLabel(oppholdsadresse.getAdresseValg()))
                                                                .withSvar(adresseSvar(oppholdsadresse))
                                                                .withType(Type.CHECKBOX)
                                                                .build()
                                                )
                                        )
                                        .build()
                        )
                )
                .build();
    }

    private String adresseLabel(JsonAdresseValg adresseValg) {
        if (adresseValg.equals(JsonAdresseValg.FOLKEREGISTRERT)) {
            return "kontakt.system.oppholdsadresse.folkeregistrertAdresse";
        } else if (adresseValg.equals(JsonAdresseValg.MIDLERTIDIG)) {
            return "kontakt.system.oppholdsadresse.midlertidigAdresse";
        } else {
            return "kontakt.system.oppholdsadresse.valg.soknad";
        }
    }

    private String adresseSvar(JsonAdresse oppholdsadresse) {
        if (oppholdsadresse.getType().equals(JsonAdresse.Type.GATEADRESSE)) {
            return gateadresseString((JsonGateAdresse) oppholdsadresse);
        }
        if (oppholdsadresse.getType().equals(JsonAdresse.Type.MATRIKKELADRESSE) && oppholdsadresse instanceof JsonMatrikkelAdresse) {
            return matrikkeladresseString((JsonMatrikkelAdresse) oppholdsadresse);
        }
        log.warn("Oppholdsadresse er verken GateAdresse eller MatrikkelAdresse. Burde ikke være mulig - må undersøkes nærmere");
        return "";
    }

    private String gateadresseString(JsonGateAdresse gateAdresse) {
        // gatenavn husnummer+husbokstav, postnummer poststed
        var optionalGateNavn = Optional.ofNullable(gateAdresse.getGatenavn());
        var optionalHusnummer = Optional.ofNullable(gateAdresse.getHusnummer());
        var optionalHusbokstav = Optional.ofNullable(gateAdresse.getHusbokstav());
        var optionalPostnummer = Optional.ofNullable(gateAdresse.getPostnummer());
        var optionalPoststed = Optional.ofNullable(gateAdresse.getPoststed());

        var gatedel = optionalGateNavn.map(s -> s + " ").orElse("") + optionalHusnummer.orElse("") + optionalHusbokstav.orElse("");
        var postdel = optionalPostnummer.map(s -> s + " ").orElse("") + optionalPoststed.orElse("");

        return gatedel + ", " + postdel;
    }

    private String matrikkeladresseString(JsonMatrikkelAdresse matrikkelAdresse) {
        // bruksenhetsnummer, kommunenummer // mer?

        var optionalBruksenhetsnummer = Optional.ofNullable(matrikkelAdresse.getBruksnummer());
        var optionalKommunenummer = Optional.ofNullable(matrikkelAdresse.getKommunenummer());

        return optionalBruksenhetsnummer.map(s -> s + ", ").orElse("") + optionalKommunenummer.orElse("");
    }

    private Avsnitt telefonnummerAvsnitt(JsonPersonalia personalia) {
        var telefonnummer = personalia.getTelefonnummer();
        var harUtfyltTelefonnummer = telefonnummer != null && !telefonnummer.getVerdi().isEmpty();

        return new Avsnitt.Builder()
                .withTittel("kontakt.system.telefoninfo.sporsmal") // skal variere ut fra kilde? systemdata eller bruker
                .withSporsmal(
                        singletonList(
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
                .withTittel("kontakt.system.kontonummer.sporsmal")
                .withSporsmal(
                        singletonList(
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
