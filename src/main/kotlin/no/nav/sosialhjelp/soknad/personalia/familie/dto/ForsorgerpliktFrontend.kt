package no.nav.sosialhjelp.soknad.personalia.familie.dto

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sosialhjelp.soknad.personalia.familie.PersonMapper.fulltNavn
import no.nav.sosialhjelp.soknad.personalia.familie.PersonMapper.getPersonnummerFromFnr

data class ForsorgerpliktFrontend(
    val harForsorgerplikt: Boolean?,
    val barnebidrag: JsonBarnebidrag.Verdi?,
    val ansvar: List<AnsvarFrontend> = emptyList(),
) {
    companion object {
        fun fromJson(forsorgerplikt: JsonForsorgerplikt): ForsorgerpliktFrontend =
            with(forsorgerplikt) {
                ForsorgerpliktFrontend(
                    harForsorgerplikt = harForsorgerplikt?.verdi,
                    barnebidrag = barnebidrag?.verdi,
                    ansvar = ansvar.orEmpty().filter { it.barn.kilde == JsonKilde.SYSTEM }.map { AnsvarFrontend.fromJson(it) },
                )
            }
    }
}

data class AnsvarFrontend(
    val barn: BarnFrontend?,
    val borSammenMed: Boolean?,
    val erFolkeregistrertSammen: Boolean?,
    val harDeltBosted: Boolean?,
    val samvarsgrad: Int?,
) {
    companion object {
        fun fromJson(jsonAnsvar: JsonAnsvar): AnsvarFrontend =
            with(jsonAnsvar) {
                AnsvarFrontend(
                    barn = barn?.let { BarnFrontend.fromJson(it) },
                    borSammenMed = borSammenMed?.verdi,
                    erFolkeregistrertSammen = erFolkeregistrertSammen?.verdi,
                    harDeltBosted = harDeltBosted?.verdi,
                    samvarsgrad = samvarsgrad?.verdi,
                )
            }
    }
}

data class BarnFrontend(
    val navn: NavnFrontend?,
    val fodselsdato: String?,
    val personnummer: String?,
    val fodselsnummer: String?,
) {
    companion object {
        fun fromJson(barn: JsonBarn): BarnFrontend =
            with(barn) {
                BarnFrontend(
                    navn = NavnFrontend(navn.fornavn, navn.mellomnavn, navn.etternavn, fulltNavn(navn)),
                    fodselsdato = fodselsdato,
                    personnummer = getPersonnummerFromFnr(personIdentifikator),
                    fodselsnummer = personIdentifikator,
                )
            }
    }
}
