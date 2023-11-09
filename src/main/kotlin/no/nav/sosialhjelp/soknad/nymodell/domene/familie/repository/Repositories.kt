package no.nav.sosialhjelp.soknad.nymodell.domene.familie.repository

import no.nav.sosialhjelp.soknad.nymodell.domene.UpsertRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.Forsorger
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.Sivilstand
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SivilstandRepository : UpsertRepository<Sivilstand>, ListCrudRepository<Sivilstand, UUID>

@Repository
interface ForsorgerRepository : UpsertRepository<Forsorger>, ListCrudRepository<Forsorger, UUID>
