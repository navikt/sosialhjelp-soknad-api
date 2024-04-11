package no.nav.sosialhjelp.soknad.ettersending.innsendtsoknad

import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus

data class BehandlingsKjede(
    val originalSoknad: InnsendtSoknad?,
    val ettersendelser: List<InnsendtSoknad>?,
)

data class InnsendtSoknad(
    val behandlingsId: String?,
    val innsendtDato: String?,
    val innsendtTidspunkt: String?,
    val soknadsalderIMinutter: Long?,
    val innsendteVedlegg: List<Vedlegg>?,
    val ikkeInnsendteVedlegg: List<Vedlegg>?,
    val navenhet: String?,
    val orgnummer: String?,
)

data class Vedlegg(
    val skjemaNummer: String?,
    val skjemanummerTillegg: String?,
    val innsendingsvalg: Vedleggstatus?,
)
