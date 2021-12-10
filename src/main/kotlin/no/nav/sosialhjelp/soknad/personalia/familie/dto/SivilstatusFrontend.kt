package no.nav.sosialhjelp.soknad.personalia.familie.dto

import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.personalia.basispersonalia.dto.NavnFrontend

data class SivilstatusFrontend(
    val kildeErSystem: Boolean?,
    val sivilstatus: JsonSivilstatus.Status?,
    val ektefelle: EktefelleFrontend?,
    val harDiskresjonskode: Boolean?,
    val borSammenMed: Boolean?,
    val erFolkeregistrertSammen: Boolean?,
)

data class EktefelleFrontend(
    var navn: NavnFrontend?,
    var fodselsdato: String?,
    var personnummer: String?,
)
