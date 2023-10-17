package no.nav.sosialhjelp.soknad.service.opprettsoknad

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.repository.BosituasjonRepository
import no.nav.sosialhjelp.soknad.repository.DelAvSoknad
import no.nav.sosialhjelp.soknad.repository.SoknadRepository
import no.nav.sosialhjelp.soknad.service.opprettsoknad.JsonInternalSoknadMappers.map
import org.springframework.context.ApplicationContext
import org.springframework.data.repository.ListCrudRepository
import java.util.*
import kotlin.reflect.KClass

class JsonInternalSoknadMappingManager(
    private val soknadId: UUID,
    private val ctx: ApplicationContext,
    private val jsonInternalSoknad: JsonInternalSoknad
) {

    // Henter ut domeneobjektet fra Repository og mapper
    fun mapDomainObjectsForSoknadId() {
        with (jsonInternalSoknad) {
            map(getDomainObject(SoknadRepository::class))
            map(getDomainObject(BosituasjonRepository::class))
        }
    }

    // Henter ut domeneobjekt for SoknadId fra Repository
    private fun <Entity: DelAvSoknad, Repo: ListCrudRepository<Entity, UUID>>getDomainObject(repositoryClazz: KClass<Repo>): Entity {
        val repository = ctx.getBean(repositoryClazz.java)
        return repository.findById(soknadId).get()
    }
}
