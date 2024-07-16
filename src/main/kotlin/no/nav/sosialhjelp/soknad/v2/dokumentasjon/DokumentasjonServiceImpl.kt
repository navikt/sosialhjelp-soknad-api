package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
import no.nav.sosialhjelp.soknad.v2.okonomi.BruttoNetto
import no.nav.sosialhjelp.soknad.v2.okonomi.DokumentDto
import no.nav.sosialhjelp.soknad.v2.okonomi.DokumentasjonDto
import no.nav.sosialhjelp.soknad.v2.okonomi.ForventetDokumentasjonDto
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetalj
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiskeDetaljerDto
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.UtbetalingMedKomponent
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface ForventetDokumentasjonService {
    fun getForventetDokumentasjon(soknadId: UUID): ForventetDokumentasjonDto

    fun updateDokumentasjonStatus(
        soknadId: UUID,
        okonomiType: OkonomiType,
        levertTidligere: Boolean,
    )
}

interface DokumentasjonService {
    fun opprettForventetVedlegg(
        soknadId: UUID,
        okonomiType: OkonomiType,
    )

    fun fjernForventetVedlegg(
        soknadId: UUID,
        okonomiType: OkonomiType,
    )
}

interface DokumentasjonStatusService {
    fun updateDokumentStatus(
        soknadId: UUID,
        okonomiType: OkonomiType,
        status: DokumentasjonStatus,
    )
}

@Service
@Transactional
class DokumentasjonServiceImpl(
    private val okonomiService: OkonomiService,
    private val dokumentasjonRepository: DokumentasjonRepository,
) : DokumentasjonService, DokumentasjonStatusService, ForventetDokumentasjonService {
    override fun opprettForventetVedlegg(
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

    override fun updateDokumentStatus(
        soknadId: UUID,
        okonomiType: OkonomiType,
        status: DokumentasjonStatus,
    ) {
        dokumentasjonRepository.findBySoknadIdAndType(soknadId, okonomiType)?.run {
            if (this.status != status) {
                copy(status = status)
                    .also { dokumentasjonRepository.save(it) }
            }
        } ?: error("Dokument finnes ikke")
    }

    override fun getForventetDokumentasjon(soknadId: UUID): ForventetDokumentasjonDto {
        return dokumentasjonRepository.findAllBySoknadId(soknadId)
            .map { dok ->
                DokumentasjonDto(
                    type = dok.type,
                    dokumentasjonStatus = dok.status,
                    detaljer = getDetaljerForDokumentasjon(soknadId, dok.type),
                    dokumenter = dok.dokumenter.map { it.toDokumentDto() },
                )
            }
            .let { dtos -> ForventetDokumentasjonDto(forventetDokumentasjon = dtos) }
    }

    override fun updateDokumentasjonStatus(
        soknadId: UUID,
        okonomiType: OkonomiType,
        levertTidligere: Boolean,
    ) {
        // TODO DokumentasjonStatus settes backend når Dokumentasjon opprettes.
        // TODO Lastes det opp en fil - vil den settes til levert.
        // TODO Huker bruker av "levert tidligere" får Dokumentasjon dette flagget - MEN:
        // TODO Hvis det finnes Dokument(er) på denne Dokumentasjonen, men bruker oppdaterer levertTidligere = true, skal vi:
        // TODO 1. Ta vare på vedleggene men forholde oss til de som "inaktive" sålenge flagget er true?
        // TODO 2. Slette alle records om Dokumentene både hos oss og FIKS Mellomlager?
    }

    private fun getDetaljerForDokumentasjon(
        soknadId: UUID,
        type: OkonomiType,
    ): List<OkonomiskeDetaljerDto>? {
        //
        if (!typesWithOkonomiElement.contains(type.javaClass)) return null

        return okonomiService.findDetaljerOrNull(soknadId, type)
            ?.map { detalj -> detalj.mapToOkonomiskeDetaljerDto() }
    }

    companion object {
        // kan finnes forventet dokumentasjon som ikke har okonomi-element (skattemelding, annet, etc.)
        val typesWithOkonomiElement = listOf(InntektType::class.java, UtgiftType::class.java, FormueType::class.java)
    }
}

private fun Dokument.toDokumentDto() =
    DokumentDto(
        uuid = sha512,
        filnavn = filnavn,
    )

private fun OkonomiDetalj.mapToOkonomiskeDetaljerDto(): OkonomiskeDetaljerDto {
    return when (this) {
        is Belop -> OkonomiskeDetaljerDto(belop = belop)
        is BruttoNetto -> OkonomiskeDetaljerDto(brutto = brutto, netto = netto)
        is Utbetaling -> OkonomiskeDetaljerDto(belop = belop)
        is UtbetalingMedKomponent -> OkonomiskeDetaljerDto(belop = utbetaling.belop)
        else -> error("Ukjent type for OkonomiskeDetaljerDto")
    }
}

private fun OkonomiType.isNotRenterOrAvdrag(): Boolean {
    return this != UtgiftType.UTGIFTER_BOLIGLAN_RENTER && this != UtgiftType.UTGIFTER_BOLIGLAN_AVDRAG
}
