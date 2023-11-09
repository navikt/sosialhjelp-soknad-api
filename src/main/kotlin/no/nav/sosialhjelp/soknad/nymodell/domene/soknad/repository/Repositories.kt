package no.nav.sosialhjelp.soknad.nymodell.domene.soknad.repository

import no.nav.sosialhjelp.soknad.nymodell.domene.UpsertRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.Soknad
import org.springframework.data.repository.ListCrudRepository
import java.util.*

@org.springframework.stereotype.Repository
interface SoknadRepository : UpsertRepository<Soknad>, ListCrudRepository<Soknad, UUID>
