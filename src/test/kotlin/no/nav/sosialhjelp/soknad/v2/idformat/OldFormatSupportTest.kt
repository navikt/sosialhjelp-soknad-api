package no.nav.sosialhjelp.soknad.v2.idformat

import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.BatchSoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataType
import no.nav.sosialhjelp.soknad.v2.soknad.OldIdFormatSupportHandler
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("no-redis", "test", "test-container")
class OldFormatSupportTest {

    companion object {
        // Dette var langtlevende Oracle-db sin neste sequence
        const val oracleDbNextSequenceValue = 4360796L
    }

    @Autowired
    private lateinit var soknadMetadataRepository: SoknadMetadataRepository

    @Autowired
    private lateinit var batchRepository: BatchSoknadMetadataRepository

    @Autowired
    private lateinit var handler: OldIdFormatSupportHandler

    @Test
    fun `GetNextVal skal gi tall lik eller storre enn 4360796`() {
        assertThat(handler.getNextId()).isGreaterThanOrEqualTo(oracleDbNextSequenceValue)
    }

    @Test
    fun `IdFormatMap kan lagres til DB`() {
        val soknadMetadata = soknadMetadata().also {
            soknadMetadataRepository.opprett(it)
        }
        val idFormatMap = handler.createAndMap(soknadMetadata.behandlingsId)

        assertThat(soknadMetadata.behandlingsId).isEqualTo(idFormatMap.soknadId.toString())
        assertThat((idFormatMap.idOldFormat)).isNotNull()
    }

    @Test
    fun `IdFormatMap gir feil hvis SoknadMetadata ikke finnes`() {
        assertThatThrownBy { handler.createAndMap(UUID.randomUUID().toString()) }
            .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `Slette SoknadMetadata sletter IdFormatMap`() {
        val soknadMetadata = soknadMetadata().also {
            soknadMetadataRepository.opprett(it)
        }
        handler.createAndMap(soknadMetadata.behandlingsId)
        batchRepository.slettSoknadMetaDataer(listOf(soknadMetadata.behandlingsId))

        assertThat(handler.findByUUID(soknadMetadata.behandlingsId)).isNull()
    }

    private fun soknadMetadata(
        behandlingsId: String = UUID.randomUUID().toString(),
        status: SoknadMetadataInnsendingStatus = SoknadMetadataInnsendingStatus.UNDER_ARBEID,
        dagerSiden: Int = 5
    ): SoknadMetadata {
        return SoknadMetadata(
            id = 0,
            behandlingsId = behandlingsId,
            fnr = "12345612345",
            type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL,
            skjema = "",
            status = status,
            innsendtDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            opprettetDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            sistEndretDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            lest = false
        )
    }
}
