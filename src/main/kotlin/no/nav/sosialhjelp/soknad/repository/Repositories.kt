package no.nav.sosialhjelp.soknad.repository

import no.nav.sosialhjelp.soknad.model.AdresseForSoknad
import no.nav.sosialhjelp.soknad.model.Arbeid
import no.nav.sosialhjelp.soknad.model.Bosituasjon
import no.nav.sosialhjelp.soknad.model.Fil
import no.nav.sosialhjelp.soknad.model.Soknad
import no.nav.sosialhjelp.soknad.model.Vedlegg
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SoknadRepository: UpsertRepository<Soknad>, ListCrudRepository<Soknad, UUID>

@NoRepositoryBean
interface DelAvSoknadRepository<T: DelAvSoknad> : ListCrudRepository<T, UUID> {
    @Query("SELECT * FROM soknad where id = :soknadId")
    fun findSoknad(soknadId: UUID): Soknad
}

//@Repository
//interface AdresseRepository: UpsertRepository<AdresseForSoknad>, ListCrudRepository<AdresseForSoknad, UUID>

@Repository
interface BosituasjonRepository : UpsertRepository<Bosituasjon>, DelAvSoknadRepository<Bosituasjon>

@Repository
interface ArbeidRepository : UpsertRepository<Arbeid>, DelAvSoknadRepository<Arbeid>

@Repository
interface VedleggRepository : ListCrudRepository<Vedlegg, Long> {
    fun findAllBySoknadId(soknadId: UUID): List<Vedlegg>
}
@Repository
interface FilRepository : ListCrudRepository<Fil, Long> {
    fun findAllByVedleggId(vedleggId: Long): List<Fil>
}

interface DelAvSoknad {
    val id: UUID
}
