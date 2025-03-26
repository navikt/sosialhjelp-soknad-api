package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.okonomi.AnnenDokumentasjonType
import no.nav.sosialhjelp.soknad.v2.okonomi.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface DokumentasjonService {
    fun opprettDokumentasjon(
        soknadId: UUID,
        opplysningType: OpplysningType,
    )

    fun fjernForventetDokumentasjon(
        soknadId: UUID,
        opplysningType: OpplysningType,
    )

    fun resetForventetDokumentasjon(soknadId: UUID)

    fun findDokumentasjonForSoknad(soknadId: UUID): List<Dokumentasjon>

    fun hasDokumenterForType(
        soknadId: UUID,
        type: OpplysningType,
    ): Boolean

    fun updateDokumentasjon(dokumentasjon: Dokumentasjon)

    fun opprettObligatoriskDokumentasjon(
        soknadId: UUID,
        soknadType: SoknadType,
    )

    fun findDokumentasjonByType(
        soknadId: UUID,
        type: OpplysningType,
    ): Dokumentasjon?
}

interface DokumentRefService {
    fun getRef(
        soknadId: UUID,
        dokumentId: UUID,
    ): DokumentRef?

    fun addRef(
        soknadId: UUID,
        type: OpplysningType,
        fiksFilId: UUID,
        filnavn: String,
    )

    fun removeRef(
        soknadId: UUID,
        dokumentId: UUID,
    )
}

@Service
class DokumentasjonServiceImpl(
    private val dokumentasjonRepository: DokumentasjonRepository,
) : DokumentasjonService, DokumentRefService {
    @Transactional(readOnly = true)
    override fun getRef(
        soknadId: UUID,
        dokumentId: UUID,
    ): DokumentRef? =
        dokumentasjonRepository.findAllBySoknadId(soknadId)
            .flatMap { it.dokumenter }
            .find { it.dokumentId == dokumentId }

    @Transactional
    override fun opprettDokumentasjon(
        soknadId: UUID,
        opplysningType: OpplysningType,
    ) {
        dokumentasjonRepository.findAllBySoknadId(soknadId).find { it.type == opplysningType }
            ?: dokumentasjonRepository.save(Dokumentasjon(soknadId = soknadId, type = opplysningType))
    }

    @Transactional
    override fun fjernForventetDokumentasjon(
        soknadId: UUID,
        opplysningType: OpplysningType,
    ) {
        dokumentasjonRepository
            .findAllBySoknadId(soknadId)
            .find { it.type == opplysningType }
            ?.also { dokumentasjonRepository.deleteById(it.id) }
    }

    @Transactional
    override fun resetForventetDokumentasjon(soknadId: UUID) {
        dokumentasjonRepository.deleteAllBySoknadId(soknadId)
    }

    @Transactional(readOnly = true)
    override fun findDokumentasjonForSoknad(soknadId: UUID): List<Dokumentasjon> = dokumentasjonRepository.findAllBySoknadId(soknadId)

    @Transactional(readOnly = true)
    override fun hasDokumenterForType(
        soknadId: UUID,
        type: OpplysningType,
    ): Boolean = findDokumentasjonForSoknad(soknadId).find { it.type == type }?.dokumenter?.isNotEmpty() ?: false

    @Transactional
    override fun updateDokumentasjon(dokumentasjon: Dokumentasjon) {
        dokumentasjonRepository.findBySoknadIdAndType(dokumentasjon.soknadId, dokumentasjon.type)
            ?.also { dokumentasjonRepository.save(it) }
            ?: error("Dokumentasjon finnes ikke")
    }

    @Transactional
    override fun opprettObligatoriskDokumentasjon(
        soknadId: UUID,
        soknadType: SoknadType,
    ) {
        when (soknadType) {
            SoknadType.KORT -> {
                opprettDokumentasjon(soknadId, AnnenDokumentasjonType.BEHOV)
                opprettDokumentasjon(soknadId, FormueType.FORMUE_BRUKSKONTO)
                opprettDokumentasjon(soknadId, UtgiftType.UTGIFTER_ANDRE_UTGIFTER)
            }
            SoknadType.STANDARD -> {
                opprettDokumentasjon(soknadId, AnnenDokumentasjonType.SKATTEMELDING)
                opprettDokumentasjon(soknadId, UtgiftType.UTGIFTER_ANDRE_UTGIFTER)
            }
        }
    }

    override fun findDokumentasjonByType(
        soknadId: UUID,
        type: OpplysningType,
    ): Dokumentasjon? =
        dokumentasjonRepository.findBySoknadIdAndType(soknadId, type)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun addRef(
        soknadId: UUID,
        type: OpplysningType,
        fiksFilId: UUID,
        filnavn: String,
    ) {
        dokumentasjonRepository.findBySoknadIdAndType(soknadId, type)
            ?.run {
                copy(
                    status = DokumentasjonStatus.LASTET_OPP,
                    dokumenter = dokumenter.plus(DokumentRef(fiksFilId, filnavn)),
                )
            }
            ?.also { dokumentasjonRepository.save(it) }
            ?: throw IkkeFunnetException("Dokumentasjon for type ${type.name} finnes ikke")
    }

    @Transactional
    override fun removeRef(
        soknadId: UUID,
        dokumentId: UUID,
    ) {
        dokumentasjonRepository.removeDokumentFromDokumentasjon(soknadId, dokumentId)
            ?: logger.warn("Dokument($dokumentId) ble ikke funnet på noe Dokumentasjon. Slettet tidligere?")
    }

    companion object {
        private val logger by logger()
    }
}
