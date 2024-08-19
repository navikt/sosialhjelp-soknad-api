package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.innsending.SenderUtils
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiType
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.toSha512
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import org.springframework.stereotype.Service
import java.util.UUID

interface DokumentasjonService {
    fun opprettDokumentasjon(
        soknadId: UUID,
        okonomiType: OkonomiType,
    )

    fun fjernForventetVedlegg(
        soknadId: UUID,
        okonomiType: OkonomiType,
    )

    fun findDokumentasjonForSoknad(soknadId: UUID): List<Dokumentasjon>

    fun hasDokumenterForType(
        soknadId: UUID,
        type: OkonomiType,
    ): Boolean

    fun updateDokumentasjonStatus(
        soknadId: UUID,
        okonomiType: OkonomiType,
        status: DokumentasjonStatus,
    )
}

interface DokumentService {
    fun getDokument(
        soknadId: UUID,
        dokumentId: UUID,
    ): Pair<String, ByteArray>

    fun saveDokument(
        soknadId: UUID,
        type: OkonomiType,
        source: ByteArray,
        orginaltFilnavn: String,
    ): Dokument

    fun deleteDokument(
        soknadId: UUID,
        dokumentId: UUID,
    ): Dokumentasjon

    fun deleteAllDokumenter(soknadId: UUID)
}

// TODO Logging
// TODO Må se litt nøyere på transaksjonshåndtering i de forskjellige løpene
@Service
class DokumentasjonServiceImpl(
    private val dokumentasjonRepository: DokumentasjonRepository,
    private val mellomlagringClient: MellomlagringClient,
) : DokumentasjonService, DokumentService {
    override fun opprettDokumentasjon(
        soknadId: UUID,
        okonomiType: OkonomiType,
    ) {
        dokumentasjonRepository.findAllBySoknadId(soknadId).find { it.type == okonomiType }
            ?: dokumentasjonRepository.save(Dokumentasjon(soknadId = soknadId, type = okonomiType))
    }

    override fun fjernForventetVedlegg(
        soknadId: UUID,
        okonomiType: OkonomiType,
    ) {
        dokumentasjonRepository.findAllBySoknadId(soknadId).find { it.type == okonomiType }
            ?.let { dokumentasjonRepository.deleteById(it.id) }
    }

    override fun findDokumentasjonForSoknad(soknadId: UUID): List<Dokumentasjon> {
        return dokumentasjonRepository.findAllBySoknadId(soknadId)
    }

    override fun hasDokumenterForType(
        soknadId: UUID,
        type: OkonomiType,
    ): Boolean {
        return findDokumentasjonForSoknad(soknadId).any { it.type == type }
    }

    override fun updateDokumentasjonStatus(
        soknadId: UUID,
        okonomiType: OkonomiType,
        status: DokumentasjonStatus,
    ) {
        dokumentasjonRepository.findBySoknadIdAndType(soknadId, okonomiType)?.run {
            if (this.status != status) {
                copy(status = status).also { dokumentasjonRepository.save(it) }
            }
        } ?: error("Dokument finnes ikke")
    }

    override fun getDokument(
        soknadId: UUID,
        dokumentId: UUID,
    ): Pair<String, ByteArray> {
        val dokument =
            runCatching { dokumentasjonRepository.findDokumentOrThrow(soknadId, dokumentId) }
                .onFailure { mellomlagringClient.deleteDokument(soknadId, dokumentId) }
                .getOrThrow()

        val bytes =
            runCatching { mellomlagringClient.getDokument(soknadId, dokumentId) }
                .onFailure { dokumentasjonRepository.removeDokumentFromDokumentasjon(soknadId, dokumentId) }
                .getOrThrow()

        return Pair(dokument.filnavn, bytes)
    }

    override fun saveDokument(
        soknadId: UUID,
        type: OkonomiType,
        source: ByteArray,
        orginaltFilnavn: String,
    ): Dokument {
        val dokumentasjon =
            dokumentasjonRepository.findBySoknadIdAndType(soknadId, type)
                ?: throw IkkeFunnetException("Dokumentasjon for type ${type.name} finnes ikke")

        val (filnavn, data) = VedleggUtils.validerFilOgReturnerNyttFilnavn(orginaltFilnavn, source)

        val dokumentId = lastOppDokumentOgHentGenerertId(soknadId, filnavn, data)
        return Dokument(dokumentId = dokumentId, filnavn = filnavn, sha512 = data.toSha512())
            .also { dokumentasjon.addDokumentAndSave(it) }
    }

    override fun deleteDokument(
        soknadId: UUID,
        dokumentId: UUID,
    ): Dokumentasjon {
        return dokumentasjonRepository.removeDokumentFromDokumentasjon(soknadId, dokumentId)
            .run { if (dokumenter.isEmpty()) copy(status = DokumentasjonStatus.FORVENTET) else this }
            .also { dokumentasjonRepository.save(it) }
            .also {
                logger.info("Sletter Dokument($dokumentId) fra Dokumentasjon(type: ${it.type.name}")
                runCatching { mellomlagringClient.deleteDokument(soknadId, dokumentId) }
                    .onFailure { throw IllegalStateException("Feil ved sletting av Dokument($dokumentId) hos Fiks", it) }
            }
    }

    override fun deleteAllDokumenter(soknadId: UUID) {
        dokumentasjonRepository.findAllBySoknadId(soknadId)
            .map { dokumentasjon -> dokumentasjon.copy(dokumenter = emptySet(), status = dokumentasjon.updateStatus()) }
            .let { list -> dokumentasjonRepository.saveAll(list) }

        mellomlagringClient.deleteDokumenter(soknadId)
    }

    private fun Dokumentasjon.updateStatus(): DokumentasjonStatus {
        return DokumentasjonStatus.LEVERT_TIDLIGERE.let { if (status == it) it else null }
            ?: if (dokumenter.isEmpty()) DokumentasjonStatus.FORVENTET else DokumentasjonStatus.LASTET_OPP
    }

    private fun lastOppDokumentOgHentGenerertId(
        soknadId: UUID,
        filnavn: String,
        data: ByteArray,
    ): UUID {
        mellomlagringClient.postDokument(soknadId, filnavn, data)

        return mellomlagringClient.getDokumentMetadata(soknadId)?.mellomlagringMetadataList
            ?.find { dokumentInfo -> dokumentInfo.filnavn == filnavn }
            ?.let { dokumentInfo -> UUID.fromString(dokumentInfo.filId) }
            ?: error("Fant ikke Dokument hos Fiks etter opplasting")
    }

    private fun Dokumentasjon.addDokumentAndSave(dokument: Dokument) {
        runCatching {
            copy(status = DokumentasjonStatus.LASTET_OPP, dokumenter = dokumenter.plus(dokument))
                .also { dokumentasjonRepository.save(it) }
        }
            .onFailure { mellomlagringClient.deleteDokument(soknadId, dokument.dokumentId) }
    }

    companion object {
        private val logger by logger()
    }
}

private fun DokumentasjonRepository.findDokumentOrThrow(
    soknadId: UUID,
    dokumentId: UUID,
): Dokument {
    return findAllBySoknadId(soknadId).flatMap { it.dokumenter }.find { it.dokumentId == dokumentId }
        ?: throw IkkeFunnetException("Dokument eksisterer ikke på noe Dokumentasjon")
}

// TODO Denne må inn igjen før dette tar over - skulle gjerne løst det på en annen måte
private fun getNavEksternId(soknadId: UUID) =
    if (MiljoUtils.isNonProduction()) SenderUtils.createPrefixedBehandlingsId(soknadId.toString()) else soknadId.toString()
