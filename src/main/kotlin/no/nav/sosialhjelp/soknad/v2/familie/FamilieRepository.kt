package no.nav.sosialhjelp.soknad.v2.familie

import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.repository.ListCrudRepository
import java.util.UUID

interface FamilieRepository : UpsertRepository<Familie>, ListCrudRepository<Familie, UUID>
