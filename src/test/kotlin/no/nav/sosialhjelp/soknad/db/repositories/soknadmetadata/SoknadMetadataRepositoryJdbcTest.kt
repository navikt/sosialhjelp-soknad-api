package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

import no.nav.sosialhjelp.soknad.business.db.RepositoryTestSupport
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.config.DbTestConfig
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import javax.inject.Inject

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DbTestConfig::class])
@ActiveProfiles("test")
internal class SoknadMetadataRepositoryJdbcTest {

    private val dagerGammelSoknad = 20
    private val behandlingsId = "1100AAAAA"

    @Inject
    private val soknadMetadataRepository: SoknadMetadataRepository? = null

    @Inject
    private val support: RepositoryTestSupport? = null
    @AfterEach
    fun teardown() {
        support!!.jdbcTemplate.update("DELETE FROM soknadmetadata")
    }

    @Test
    fun oppdaterLestDittNav() {
        var soknadMetadata = soknadMetadata(behandlingsId, SoknadMetadataInnsendingStatus.UNDER_ARBEID, 12)
        assertThat(soknadMetadata.lestDittNav).isFalse
        soknadMetadataRepository!!.opprett(soknadMetadata)

        soknadMetadata = soknadMetadataRepository.hent(soknadMetadata.behandlingsId)!!
        soknadMetadata.lestDittNav = true

        soknadMetadataRepository.oppdaterLestDittNav(soknadMetadata, EIER)

        val soknadMetadataFraDb = soknadMetadataRepository.hent(behandlingsId)
        assertThat(soknadMetadataFraDb).isNotNull
        assertThat(soknadMetadataFraDb?.lestDittNav).isTrue
    }

    private fun soknadMetadata(
        behandlingsId: String,
        status: SoknadMetadataInnsendingStatus,
        dagerSiden: Int,
    ): SoknadMetadata {
        val meta = SoknadMetadata()
        meta.id = soknadMetadataRepository!!.hentNesteId()
        meta.behandlingsId = behandlingsId
        meta.fnr = EIER
        meta.type = SoknadType.SEND_SOKNAD_KOMMUNAL
        meta.skjema = ""
        meta.status = status
        meta.innsendtDato = LocalDateTime.now().minusDays(dagerSiden.toLong())
        meta.opprettetDato = LocalDateTime.now().minusDays(dagerSiden.toLong())
        meta.sistEndretDato = LocalDateTime.now().minusDays(dagerSiden.toLong())
        meta.lestDittNav = false
        return meta
    }

    companion object {
        private const val EIER = "11111111111"
    }
}
