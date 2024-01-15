package no.nav.sosialhjelp.soknad.personalia.familie.dto

import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag

data class ForsorgerpliktFrontend(
    val harForsorgerplikt: Boolean?,
    val barnebidrag: JsonBarnebidrag.Verdi?,
    val ansvar: List<AnsvarFrontend> = emptyList(),
)

data class AnsvarFrontend(
    val barn: BarnFrontend?,
    val borSammenMed: Boolean?,
    val erFolkeregistrertSammen: Boolean?,
    val harDeltBosted: Boolean?,
    val samvarsgrad: Int?,
)

data class BarnFrontend(
    val navn: NavnFrontend?,
    val fodselsdato: String?,
    val personnummer: String?,
    val fodselsnummer: String?,
)
