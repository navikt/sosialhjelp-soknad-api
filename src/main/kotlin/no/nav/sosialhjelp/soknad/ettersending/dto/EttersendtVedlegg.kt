package no.nav.sosialhjelp.soknad.ettersending.dto

import no.nav.sosialhjelp.soknad.vedlegg.dto.FilFrontend

data class EttersendtVedlegg(
    val type: String? = null,
    val vedleggStatus: String? = null,
    val filer: List<FilFrontend>? = null
)
