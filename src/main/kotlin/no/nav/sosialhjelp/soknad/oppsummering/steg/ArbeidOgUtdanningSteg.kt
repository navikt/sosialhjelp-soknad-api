package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning.Studentgrad
import no.nav.sosialhjelp.soknad.oppsummering.dto.Avsnitt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Felt
import no.nav.sosialhjelp.soknad.oppsummering.dto.Sporsmal
import no.nav.sosialhjelp.soknad.oppsummering.dto.Steg
import no.nav.sosialhjelp.soknad.oppsummering.dto.Svar
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.booleanVerdiFelt
import no.nav.sosialhjelp.soknad.oppsummering.steg.StegUtils.createSvar

class ArbeidOgUtdanningSteg {

    fun get(jsonInternalSoknad: JsonInternalSoknad): Steg {
        val arbeid = jsonInternalSoknad.soknad.data.arbeid
        val utdanning = jsonInternalSoknad.soknad.data.utdanning
        return Steg(
            stegNr = 3,
            tittel = "arbeidbolk.tittel",
            avsnitt = listOf(
                Avsnitt(
                    tittel = "arbeidsforhold.sporsmal",
                    sporsmal = arbeidsforholdSporsmal(arbeid)
                ),
                Avsnitt(
                    tittel = "arbeid.dinsituasjon.studerer.undertittel",
                    sporsmal = utdanningSporsmal(utdanning)
                )
            )
        )
    }

    private fun arbeidsforholdSporsmal(arbeid: JsonArbeid): List<Sporsmal> {
        val harArbeidsforhold = arbeid.forhold != null && !arbeid.forhold.isEmpty()
        val harKommentarTilArbeidsforhold =
            arbeid.kommentarTilArbeidsforhold != null && arbeid.kommentarTilArbeidsforhold.verdi != null
        val sporsmal = ArrayList<Sporsmal>()
        sporsmal.add(
            Sporsmal(
                tittel = if (harArbeidsforhold) "arbeidsforhold.infotekst" else "arbeidsforhold.ingen",
                erUtfylt = true,
                felt = if (harArbeidsforhold) arbeidsforholdFelter(arbeid.forhold) else null
            )
        )
        if (harKommentarTilArbeidsforhold) {
            sporsmal.add(
                Sporsmal(
                    tittel = "opplysninger.arbeidsituasjon.kommentarer.label",
                    erUtfylt = true,
                    felt = kommentarFelter(arbeid.kommentarTilArbeidsforhold)
                )
            )
        }
        return sporsmal
    }

    private fun arbeidsforholdFelter(arbeidsforholdList: List<JsonArbeidsforhold>): List<Felt> {
        return arbeidsforholdList
            .map { arbeidsforhold: JsonArbeidsforhold ->
                toFelt(
                    arbeidsforhold
                )
            }
    }

    private fun toFelt(arbeidsforhold: JsonArbeidsforhold): Felt {
        // arbeidsgiver, startet i jobben, (sluttet i jobben), stillingsprosent
        val labelSvarMap = LinkedHashMap<String, Svar>()
        if (arbeidsforhold.arbeidsgivernavn != null) {
            labelSvarMap["arbeidsforhold.arbeidsgivernavn.label"] =
                createSvar(arbeidsforhold.arbeidsgivernavn, SvarType.TEKST)
        }
        if (arbeidsforhold.fom != null) {
            labelSvarMap["arbeidsforhold.fom.label"] = createSvar(arbeidsforhold.fom, SvarType.DATO)
        }
        if (arbeidsforhold.tom != null) {
            labelSvarMap["arbeidsforhold.tom.label"] = createSvar(arbeidsforhold.tom, SvarType.DATO)
        }
        if (arbeidsforhold.stillingsprosent != null) {
            labelSvarMap["arbeidsforhold.stillingsprosent.label"] =
                createSvar(arbeidsforhold.stillingsprosent.toString(), SvarType.TEKST)
        }
        return Felt(
            type = Type.SYSTEMDATA_MAP,
            labelSvarMap = labelSvarMap
        )
    }

    private fun kommentarFelter(kommentar: JsonKommentarTilArbeidsforhold): List<Felt> {
        return listOf(
            Felt(
                type = Type.TEKST,
                svar = createSvar(kommentar.verdi, SvarType.TEKST)
            )
        )
    }

    private fun utdanningSporsmal(utdanning: JsonUtdanning?): List<Sporsmal> {
        val erUtdanningUtfylt = utdanning != null && utdanning.erStudent != null
        val erStudent = erUtdanningUtfylt && utdanning!!.erStudent == java.lang.Boolean.TRUE
        val erStudentgradUtfylt = erStudent && utdanning!!.studentgrad != null
        val sporsmal = ArrayList<Sporsmal>()
        sporsmal.add(
            Sporsmal(
                tittel = "dinsituasjon.studerer.sporsmal",
                erUtfylt = erUtdanningUtfylt,
                felt = if (erUtdanningUtfylt) booleanVerdiFelt(
                    erStudent,
                    "dinsituasjon.studerer.true",
                    "dinsituasjon.studerer.false"
                ) else null
            )
        )
        if (erStudent) {
            sporsmal.add(
                Sporsmal(
                    tittel = "dinsituasjon.studerer.true.grad.sporsmal",
                    erUtfylt = erStudentgradUtfylt,
                    felt = if (erStudentgradUtfylt) booleanVerdiFelt(
                        Studentgrad.HELTID == utdanning!!.studentgrad,
                        "dinsituasjon.studerer.true.grad.heltid",
                        "dinsituasjon.studerer.true.grad.deltid"
                    ) else null
                )
            )
        }
        return sporsmal
    }
}
