package no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.repository

import no.nav.sosialhjelp.soknad.nymodell.domene.UpsertRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Utdanning
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

//@Repository
//interface ArbeidRepository : UpsertRepository<Arbeid>, ListCrudRepository<Arbeid, UUID>

@Repository
interface BosituasjonRepository : UpsertRepository<Bosituasjon>, ListCrudRepository<Bosituasjon, UUID>

@Repository
interface UtdanningRepository: UpsertRepository<Utdanning>, ListCrudRepository<Utdanning, UUID>