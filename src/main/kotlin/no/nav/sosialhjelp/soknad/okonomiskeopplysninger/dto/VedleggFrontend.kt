package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto

import no.nav.sosialhjelp.soknad.vedlegg.dto.FilFrontend

data class VedleggFrontend(
    var type: String?,
    var gruppe: String?,
    var rader: List<VedleggRadFrontend>? = null,
    var vedleggStatus: String? = null,
    var filer: List<FilFrontend>?,
)

data class VedleggRadFrontend(
    var beskrivelse: String? = null,
    var belop: Int? = null,
    var brutto: Int? = null,
    var netto: Int? = null,
    var avdrag: Int? = null,
    var renter: Int? = null
)
