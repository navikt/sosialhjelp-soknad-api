package no.nav.sosialhjelp.soknad.migration.dto

data class SjekksumDto(
    val soknadMetadataSum: Int,
    val soknadUnderArbeidSum: Int,
    val opplastetVedleggSum: Int,
    val oppgaveSum: Int,
)
