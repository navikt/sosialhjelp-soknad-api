package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.opprettKontakt
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@ActiveProfiles("test", "test-container")
class SoknadMetadataRepositoryTest {
    @Autowired
    private lateinit var metadataRepository: SoknadMetadataRepository

    @Autowired
    private lateinit var soknadRepository: SoknadRepository

    @Autowired
    private lateinit var kontaktRepository: KontaktRepository

    @Test
    fun `Lagre metadata`() {
        metadataRepository.save(opprettSoknadMetadata())
            .soknadId
            .let { metadataRepository.findByIdOrNull(it) }
            .also { assertThat(it).isNotNull }
    }

    @Test
    fun `Endre metadata`() {
        val metadata = metadataRepository.save(opprettSoknadMetadata())

        metadataRepository.findByIdOrNull(metadata.soknadId)!!
            .run {
                copy(
                    status = SoknadStatus.SENDT,
                    tidspunkt = tidspunkt.copy(sendtInn = nowWithMillis()),
                )
            }
            .also { metadataRepository.save(it) }

        metadataRepository.findByIdOrNull(metadata.soknadId)!!
            .also { updatedMetadata ->
                assertThat(updatedMetadata.status).isEqualTo(SoknadStatus.SENDT)
                assertThat(updatedMetadata.status).isNotEqualTo(metadata.status)
            }
    }

    @Test
    fun `Slette metadata`() {
        val metadata = metadataRepository.save(opprettSoknadMetadata())

        metadataRepository.findByIdOrNull(metadata.soknadId)!!
            .also { metadataRepository.deleteById(metadata.soknadId) }

        metadataRepository.findByIdOrNull(metadata.soknadId)
            .also { assertThat(it).isNull() }
    }

    @Test
    fun `Slette metadata skal slette Soknad og andre entiteter`() {
        val metadata = metadataRepository.save(opprettSoknadMetadata())
        soknadRepository.save(opprettSoknad(id = metadata.soknadId))
        kontaktRepository.save(opprettKontakt(soknadId = metadata.soknadId))

        assertThat(soknadRepository.findByIdOrNull(metadata.soknadId)).isNotNull()
        assertThat(kontaktRepository.findByIdOrNull(metadata.soknadId)).isNotNull()

        metadataRepository.deleteById(metadata.soknadId)

        assertThat(soknadRepository.findByIdOrNull(metadata.soknadId)).isNull()
        assertThat(kontaktRepository.findByIdOrNull(metadata.soknadId)).isNull()
    }
}
