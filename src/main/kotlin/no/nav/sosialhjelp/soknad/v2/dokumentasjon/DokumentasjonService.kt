package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.okonomi.AnnenDokumentasjonType
import no.nav.sosialhjelp.soknad.v2.okonomi.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
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

    fun updateDokumentasjonStatus(
        soknadId: UUID,
        opplysningType: OpplysningType,
        status: DokumentasjonStatus,
    )

    fun opprettObligatoriskDokumentasjon(
        soknadId: UUID,
        soknadType: SoknadType,
    )
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
    override fun updateDokumentasjonStatus(
        soknadId: UUID,
        opplysningType: OpplysningType,
        status: DokumentasjonStatus,
    ) {
        dokumentasjonRepository.findBySoknadIdAndType(soknadId, opplysningType)?.run {
            if (this.status != status) {
                copy(status = status).also { dokumentasjonRepository.save(it) }
            }
        } ?: error("Dokument finnes ikke")
    }

    @Transactional
    override fun opprettObligatoriskDokumentasjon(
        soknadId: UUID,
        soknadType: SoknadType,
    ) {
        when (soknadType) {
            SoknadType.KORT -> {
                obligatoriskeDokumentasjonsTyperForKortSoknad.forEach { opprettDokumentasjon(soknadId, it) }
            }
            SoknadType.STANDARD -> {
                opprettDokumentasjon(soknadId, AnnenDokumentasjonType.SKATTEMELDING)
                opprettDokumentasjon(soknadId, UtgiftType.UTGIFTER_ANDRE_UTGIFTER)
            }
        }
    }

    private val obligatoriskeDokumentasjonsTyperForKortSoknad: List<OpplysningType> =
        listOf(
            FormueType.FORMUE_BRUKSKONTO, // kontooversikt|brukskonto
            AnnenDokumentasjonType.BEHOV, // kort|behov
            UtgiftType.UTGIFTER_ANDRE_UTGIFTER, // annet|annet
            UtgiftType.UTGIFTER_BARNEHAGE, // faktura|barnhage
            UtgiftType.UTGIFTER_SFO, // faktura|sfo
            InntektType.UTBETALING_HUSBANKEN, // husbanken|vedtak
            AnnenDokumentasjonType.HUSLEIEKONTRAKT, // husleiekontrakt|husleiekontrakt
            FormueType.FORMUE_ANNET, // kontooversikt|annet
            UtgiftType.UTGIFTER_STROM, // faktura|strom
            InntektType.JOBB, // lonnslipp|arbeid
            InntektType.STUDIELAN_INNTEKT, // student|vedtak
            InntektType.BARNEBIDRAG_MOTTAR,
            UtgiftType.BARNEBIDRAG_BETALER,
        )

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
