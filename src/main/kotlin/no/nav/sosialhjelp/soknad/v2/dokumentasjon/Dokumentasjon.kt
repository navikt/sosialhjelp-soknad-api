package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggGruppe
import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table
data class Dokumentasjon(
    @Id val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    val type: OpplysningType,
    val status: DokumentasjonStatus = DokumentasjonStatus.FORVENTET,
    val dokumenter: Set<DokumentRef> = emptySet(),
) : DomainRoot {
    override fun getDbId() = id
}

@Table
data class DokumentRef(
    val dokumentId: UUID,
    val filnavn: String,
)

enum class DokumentasjonStatus {
    LASTET_OPP,
    FORVENTET,
    LEVERT_TIDLIGERE,
}

enum class AnnenDokumentasjonType(
    override val dokumentasjonForventet: Boolean?,
) : OpplysningType {
    SKATTEMELDING(dokumentasjonForventet = true),
    SAMVARSAVTALE(dokumentasjonForventet = true),
    OPPHOLDSTILLATELSE(dokumentasjonForventet = true),
    HUSLEIEKONTRAKT(dokumentasjonForventet = true),
    HUSLEIEKONTRAKT_KOMMUNAL(dokumentasjonForventet = true),
    BEHOV(dokumentasjonForventet = true),
    ;

    override val group: VedleggGruppe get() = VedleggGruppe.GenerelleVedlegg
}
