package no.nav.sosialhjelp.soknad.service.opprettsoknad

import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidRepository
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.util.*
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils as eier

@Service
class OpprettJsonInternalSoknadService(
    private val ctx: ApplicationContext,
    private val soknadUndeArbeidRepository: SoknadUnderArbeidRepository
) {
    fun mergeJsonInternalSoknad(soknadId: UUID) {

        val soknadUA = soknadUndeArbeidRepository.hentSoknad(soknadId.toString(), eier.getUserIdFromToken())
        val jsonInternalSoknad =
            soknadUA.jsonInternalSoknad ?: throw IllegalStateException("JsonInternalSoknad finnes ikke")

        JsonInternalSoknadMappingManager(
            soknadId = soknadId,
            jsonInternalSoknad = jsonInternalSoknad,
            ctx = ctx
        ).also { it.mapDomainObjectsForSoknadId() }

        soknadUndeArbeidRepository.oppdaterSoknadsdata(soknadUA, eier.getUserIdFromToken())
    }
}