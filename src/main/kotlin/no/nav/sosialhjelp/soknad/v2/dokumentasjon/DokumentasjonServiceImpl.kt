package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

interface DokumentasjonStatusService {
    fun updateDokumentasjonStatus(
        soknadId: UUID,
        okonomiType: OkonomiType,
        status: DokumentasjonStatus,
    )
}

@Service
@Transactional
class DokumentasjonServiceImpl(
    private val dokumentasjonRepository: DokumentasjonRepository,
) : DokumentasjonService, DokumentasjonStatusService {
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

//    override fun updateDokumentasjonStatus(
//        soknadId: UUID,
//        okonomiType: OkonomiType,
//        levertTidligere: Boolean,
//    ) {
//        // TODO DokumentasjonStatus settes backend når Dokumentasjon opprettes.
//        // TODO Lastes det opp en fil - vil den settes til levert.
//        // TODO Huker bruker av "levert tidligere" får Dokumentasjon dette flagget - MEN:
//        // TODO Hvis det finnes Dokument(er) på denne Dokumentasjonen, men bruker oppdaterer levertTidligere = true, skal vi:
//        // TODO 1. Ta vare på vedleggene men forholde oss til de som "inaktive" sålenge flagget er true?
//        // TODO 2. Slette alle records om Dokumentene både hos oss og FIKS Mellomlager?
//    }
}
