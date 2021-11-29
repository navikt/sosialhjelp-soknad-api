package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.Steg
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.fulltnavn
import org.slf4j.LoggerFactory
import java.util.Optional

class PersonopplysningerSteg {
    fun get(jsonInternalSoknad: JsonInternalSoknad): Steg {
        val personalia = jsonInternalSoknad.soknad.data.personalia
        return Steg(
            stegNr = 1,
            tittel = "personaliabolk.tittel",
            avsnitt = listOf(
                personaliaAvsnitt(personalia),
                adresseOgNavKontorAvsnitt(personalia),
                telefonnummerAvsnitt(personalia),
                kontonummerAvsnitt(personalia)
            )
        )
    }

    private fun personaliaAvsnitt(personalia: JsonPersonalia): Avsnitt {
        return Avsnitt(
            tittel = "kontakt.system.personalia.sporsmal",
            sporsmal = listOf(
                Sporsmal(
                    tittel = "kontakt.system.personalia.infotekst.tekst",
                    erUtfylt = true,
                    felt = listOf(
                        Felt(
                            type = Type.SYSTEMDATA,
                            label = "kontakt.system.personalia.navn",
                            svar = createSvar(fulltnavn(personalia.navn), SvarType.TEKST)
                        ),
                        Felt(
                            type = Type.SYSTEMDATA,
                            label = "kontakt.system.personalia.fnr",
                            svar = createSvar(personalia.personIdentifikator.verdi, SvarType.TEKST)
                        ),
                        Felt(
                            type = Type.SYSTEMDATA,
                            label = "kontakt.system.personalia.statsborgerskap",
                            svar = personalia.statsborgerskap?.let { createSvar(it.verdi, SvarType.TEKST) }
                        )
                    )
                )
            )
        )
    }

    private fun adresseOgNavKontorAvsnitt(personalia: JsonPersonalia): Avsnitt {
        val oppholdsadresse = personalia.oppholdsadresse
        return Avsnitt(
            tittel = "soknadsmottaker.sporsmal",
            sporsmal = listOf(
                Sporsmal(
                    tittel = "soknadsmottaker.infotekst.tekst",
                    erUtfylt = true,
                    felt = listOf(
                        Felt(
                            type = if (JsonAdresseValg.SOKNAD == oppholdsadresse.adresseValg) Type.TEKST else Type.SYSTEMDATA,
                            label = adresseLabel(oppholdsadresse.adresseValg),
                            svar = createSvar(adresseSvar(oppholdsadresse), SvarType.TEKST)
                        )
                    )
                )
            )
        )
    }

    private fun adresseLabel(adresseValg: JsonAdresseValg): String {
        return when (adresseValg) {
            JsonAdresseValg.FOLKEREGISTRERT -> "kontakt.system.oppholdsadresse.folkeregistrertAdresse"
            JsonAdresseValg.MIDLERTIDIG -> "kontakt.system.oppholdsadresse.midlertidigAdresse"
            else -> "kontakt.system.oppholdsadresse.valg.soknad"
        }
    }

    private fun adresseSvar(oppholdsadresse: JsonAdresse): String {
        if (oppholdsadresse.type == JsonAdresse.Type.GATEADRESSE) {
            return gateadresseString(oppholdsadresse as JsonGateAdresse)
        }
        if (oppholdsadresse.type == JsonAdresse.Type.MATRIKKELADRESSE && oppholdsadresse is JsonMatrikkelAdresse) {
            return matrikkeladresseString(oppholdsadresse)
        }
        log.warn("Oppholdsadresse er verken GateAdresse eller MatrikkelAdresse. Burde ikke være mulig - må undersøkes nærmere")
        return ""
    }

    private fun gateadresseString(gateAdresse: JsonGateAdresse): String {
        // gatenavn husnummer+husbokstav, postnummer poststed
        val optionalGateNavn = Optional.ofNullable(gateAdresse.gatenavn)
        val optionalHusnummer = Optional.ofNullable(gateAdresse.husnummer)
        val optionalHusbokstav = Optional.ofNullable(gateAdresse.husbokstav)
        val optionalPostnummer = Optional.ofNullable(gateAdresse.postnummer)
        val optionalPoststed = Optional.ofNullable(gateAdresse.poststed)
        val gatedel =
            optionalGateNavn.map { "$it " }.orElse("") + optionalHusnummer.orElse("") + optionalHusbokstav.orElse("")
        val postdel = optionalPostnummer.map { "$it " }.orElse("") + optionalPoststed.orElse("")
        return "$gatedel, $postdel"
    }

    private fun matrikkeladresseString(matrikkelAdresse: JsonMatrikkelAdresse): String {
        // bruksenhetsnummer, kommunenummer // mer?
        val optionalBruksenhetsnummer = Optional.ofNullable(matrikkelAdresse.bruksnummer)
        val optionalKommunenummer = Optional.ofNullable(matrikkelAdresse.kommunenummer)
        return optionalBruksenhetsnummer.map { s: String -> "$s, " }.orElse("") + optionalKommunenummer.orElse("")
    }

    private fun telefonnummerAvsnitt(personalia: JsonPersonalia): Avsnitt {
        val telefonnummer = personalia.telefonnummer
        val harUtfyltTelefonnummer =
            telefonnummer != null && telefonnummer.verdi != null && !telefonnummer.verdi.isEmpty()
        return Avsnitt(
            tittel = "kontakt.system.telefoninfo.sporsmal",
            sporsmal = listOf(
                Sporsmal(
                    tittel = "kontakt.system.telefoninfo.infotekst.tekst", // skal variere ut fra kilde? systemdata eller bruker
                    erUtfylt = harUtfyltTelefonnummer,
                    felt = if (harUtfyltTelefonnummer) telefonnummerFelt(telefonnummer) else null
                )
            )
        )
    }

    private fun telefonnummerFelt(telefonnummer: JsonTelefonnummer?): List<Felt> {
        val erSystemdata = telefonnummer!!.kilde == JsonKilde.SYSTEM
        return listOf(
            Felt(
                type = if (erSystemdata) Type.SYSTEMDATA else Type.TEKST,
                label = "kontakt.system.telefon.label",
                svar = createSvar(telefonnummer.verdi, SvarType.TEKST)
            )
        )
    }

    private fun kontonummerAvsnitt(personalia: JsonPersonalia): Avsnitt {
        val kontonummer = personalia.kontonummer
        val harValgtHarIkkeKonto = kontonummer != null && java.lang.Boolean.TRUE == kontonummer.harIkkeKonto
        val harUtfyltKontonummer =
            kontonummer != null && (kontonummer.verdi != null && !kontonummer.verdi.isEmpty() || harValgtHarIkkeKonto)
        return Avsnitt(
            tittel = "kontakt.system.kontonummer.sporsmal",
            sporsmal = listOf(
                Sporsmal(
                    tittel = "kontakt.system.kontonummer.label",
                    erUtfylt = harUtfyltKontonummer,
                    felt = if (harUtfyltKontonummer) kontonummerFelt(kontonummer) else null
                )
            )
        )
    }

    private fun kontonummerFelt(kontonummer: JsonKontonummer?): List<Felt> {
        if (java.lang.Boolean.TRUE == kontonummer!!.harIkkeKonto) {
            return listOf(
                Felt(
                    type = Type.CHECKBOX,
                    svar = createSvar("kontakt.kontonummer.harikke.true", SvarType.LOCALE_TEKST)
                )
            )
        }
        val erSystemdata = kontonummer.kilde == JsonKilde.SYSTEM
        return listOf(
            Felt(
                type = if (erSystemdata) Type.SYSTEMDATA else Type.TEKST,
                label = "kontakt.system.kontonummer.label",
                svar = createSvar(kontonummer.verdi, SvarType.TEKST)
            )
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(PersonopplysningerSteg::class.java)
    }
}
