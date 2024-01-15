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

class PersonopplysningerSteg {
    fun get(jsonInternalSoknad: JsonInternalSoknad): Steg {
        val personalia = jsonInternalSoknad.soknad.data.personalia
        val telefonnummer: JsonTelefonnummer? = personalia.telefonnummer
        val kontonummer: JsonKontonummer? = personalia.kontonummer
        return Steg(
            stegNr = 1,
            tittel = "personaliabolk.tittel",
            avsnitt = listOf(
                personaliaAvsnitt(personalia),
                adresseOgNavKontorAvsnitt(personalia),
                telefonnummerAvsnitt(telefonnummer),
                kontonummerAvsnitt(kontonummer),
            ),
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
                            svar = createSvar(fulltnavn(personalia.navn), SvarType.TEKST),
                        ),
                        Felt(
                            type = Type.SYSTEMDATA,
                            label = "kontakt.system.personalia.fnr",
                            svar = createSvar(personalia.personIdentifikator.verdi, SvarType.TEKST),
                        ),
                        Felt(
                            type = Type.SYSTEMDATA,
                            label = "kontakt.system.personalia.statsborgerskap",
                            svar = createSvar(personalia.statsborgerskap?.verdi, SvarType.TEKST),
                        ),
                    ),
                ),
            ),
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
                            svar = createSvar(adresseSvar(oppholdsadresse), SvarType.TEKST),
                        ),
                    ),
                ),
            ),
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
        val gateNavn = gateAdresse.gatenavn?.let { "$it " } ?: ""
        val husnummer = gateAdresse.husnummer ?: ""
        val husbokstav = gateAdresse.husbokstav ?: ""
        val postnummer = gateAdresse.postnummer?.let { "$it " } ?: ""
        val poststed = gateAdresse.poststed ?: ""

        return "$gateNavn$husnummer$husbokstav, $postnummer$poststed"
    }

    private fun matrikkeladresseString(matrikkelAdresse: JsonMatrikkelAdresse): String {
        // bruksenhetsnummer, kommunenummer // mer?
        val bruksenhetsnummer = matrikkelAdresse.bruksnummer?.let { "$it, " } ?: ""
        val kommunenummer = matrikkelAdresse.kommunenummer ?: ""
        return bruksenhetsnummer + kommunenummer
    }

    private fun telefonnummerAvsnitt(telefonnummer: JsonTelefonnummer?): Avsnitt {
        val harUtfyltTelefonnummer =
            telefonnummer != null && telefonnummer.verdi != null && telefonnummer.verdi.isNotEmpty()
        return Avsnitt(
            tittel = "kontakt.system.telefoninfo.sporsmal",
            sporsmal = listOf(
                Sporsmal(
                    tittel = "kontakt.system.telefoninfo.infotekst.tekst", // skal variere ut fra kilde? systemdata eller bruker
                    erUtfylt = harUtfyltTelefonnummer,
                    felt = telefonnummer?.let { if (harUtfyltTelefonnummer) telefonnummerFelt(it) else null },
                ),
            ),
        )
    }

    private fun telefonnummerFelt(telefonnummer: JsonTelefonnummer): List<Felt> {
        val erSystemdata = telefonnummer.kilde == JsonKilde.SYSTEM
        return listOf(
            Felt(
                type = if (erSystemdata) Type.SYSTEMDATA else Type.TEKST,
                label = "kontakt.system.telefon.label",
                svar = createSvar(telefonnummer.verdi, SvarType.TEKST),
            ),
        )
    }

    private fun kontonummerAvsnitt(kontonummer: JsonKontonummer?): Avsnitt {
        val harValgtHarIkkeKonto = kontonummer != null && java.lang.Boolean.TRUE == kontonummer.harIkkeKonto
        val harUtfyltKontonummer =
            kontonummer != null && (kontonummer.verdi != null && kontonummer.verdi.isNotEmpty() || harValgtHarIkkeKonto)
        return Avsnitt(
            tittel = "kontakt.system.kontonummer.sporsmal",
            sporsmal = listOf(
                Sporsmal(
                    tittel = "kontakt.system.kontonummer.label",
                    erUtfylt = harUtfyltKontonummer,
                    felt = kontonummer?.let { if (harUtfyltKontonummer) kontonummerFelt(it) else null },
                ),
            ),
        )
    }

    private fun kontonummerFelt(kontonummer: JsonKontonummer): List<Felt> {
        if (java.lang.Boolean.TRUE == kontonummer.harIkkeKonto) {
            return listOf(
                Felt(
                    type = Type.CHECKBOX,
                    svar = createSvar("kontakt.kontonummer.harikke.true", SvarType.LOCALE_TEKST),
                ),
            )
        }
        val erSystemdata = kontonummer.kilde == JsonKilde.SYSTEM
        return listOf(
            Felt(
                type = if (erSystemdata) Type.SYSTEMDATA else Type.TEKST,
                label = "kontakt.system.kontonummer.label",
                svar = createSvar(kontonummer.verdi, SvarType.TEKST),
            ),
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(PersonopplysningerSteg::class.java)
    }
}
