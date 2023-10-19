package no.nav.sosialhjelp.soknad.repository

import no.nav.sosialhjelp.soknad.model.arbeid.Arbeid
import no.nav.sosialhjelp.soknad.model.familie.Familie
import no.nav.sosialhjelp.soknad.model.soknad.Bosituasjon
import no.nav.sosialhjelp.soknad.model.soknad.Fil
import no.nav.sosialhjelp.soknad.model.soknad.KeyErSoknadId
import no.nav.sosialhjelp.soknad.model.soknad.Soknad
import no.nav.sosialhjelp.soknad.model.soknad.Vedlegg
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SoknadRepository: UpsertRepository<Soknad>, ListCrudRepository<Soknad, UUID>

@NoRepositoryBean
interface DelAvSoknadRepository<T: KeyErSoknadId> : ListCrudRepository<T, UUID> {
    @Modifying
    @Query("SELECT * FROM soknad where id = :soknadId")
    fun findSoknad(soknadId: UUID): Soknad
}

@Repository
interface BosituasjonRepository : UpsertRepository<Bosituasjon>, DelAvSoknadRepository<Bosituasjon>

@Repository
interface ArbeidRepository : UpsertRepository<Arbeid>, DelAvSoknadRepository<Arbeid>

@Repository
interface FamilieRepository : UpsertRepository<Familie>, DelAvSoknadRepository<Familie>

@Repository
interface VedleggRepository : ListCrudRepository<Vedlegg, Long> {
    fun findAllBySoknadId(soknadId: UUID): List<Vedlegg>
}
@Repository
interface FilRepository : ListCrudRepository<Fil, Long> {
    fun findAllByVedleggId(vedleggId: Long): List<Fil>
}
