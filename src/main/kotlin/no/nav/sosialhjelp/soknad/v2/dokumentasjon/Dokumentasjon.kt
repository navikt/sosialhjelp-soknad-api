package no.nav.sosialhjelp.soknad.v2.dokumentasjon

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
) : DomainRoot {
    override fun getDbId() = id
}

enum class DokumentasjonStatus {
    FORVENTET,
    LEVERT_TIDLIGERE,
}
