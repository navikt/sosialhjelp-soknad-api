package no.nav.sosialhjelp.soknad.v2.familie.service

import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.familie.Barn
import no.nav.sosialhjelp.soknad.v2.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.v2.familie.Ektefelle
import no.nav.sosialhjelp.soknad.v2.familie.Familie
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus

interface ForsorgerService {
    fun findForsorger(soknadId: UUID): Forsorger?
    fun updateForsorger(soknadId: UUID, barnebidrag: Barnebidrag?, updated: List<Barn>): Forsorger
}

interface SivilstandService {
    fun findSivilstand(soknadId: UUID): Sivilstand?
    fun updateSivilstand(soknadId: UUID, sivilstatus: Sivilstatus?, ektefelle: Ektefelle?): Sivilstand
}

interface FamilieRegisterService {
    fun updateSivilstatusFraRegister(soknadId: UUID, sivilstatus: Sivilstatus, ektefelle: Ektefelle)
    fun updateForsorgerpliktRegister(soknadId: UUID, harForsorgerplikt: Boolean, barn: List<Barn>)
}
// wrapper-klasse (midlertidig sålenge vi har et såpass fragmentert controller-lag?)
data class Forsorger(
    val harForsorgerplikt: Boolean? = null,
    val barnebidrag: Barnebidrag? = null,
    val ansvar: Map<UUID, Barn> = emptyMap()
)

fun Familie.toForsorger() = Forsorger(
    harForsorgerplikt = harForsorgerplikt,
    barnebidrag = barnebidrag,
    ansvar = ansvar
)

// wrapper-klasse (midlertidig sålenge vi har et såpass fragmentert controller-lag?)
data class Sivilstand(
    val sivilstatus: Sivilstatus? = null,
    val ektefelle: Ektefelle? = null,
)

internal fun Familie.toSivilstand() = Sivilstand(
    sivilstatus = sivilstatus,
    ektefelle = ektefelle
)
