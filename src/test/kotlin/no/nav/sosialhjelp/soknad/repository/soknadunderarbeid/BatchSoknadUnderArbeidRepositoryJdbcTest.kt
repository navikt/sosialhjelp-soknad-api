package no.nav.sosialhjelp.soknad.repository.soknadunderarbeid

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.BatchOpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.BatchOpplastetVedleggRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.repository.RepositoryTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime

internal class BatchSoknadUnderArbeidRepositoryJdbcTest : RepositoryTest() {
    
    private var soknadUnderArbeidRepository: SoknadUnderArbeidRepository? = null
    private var opplastetVedleggRepository: OpplastetVedleggRepository? = null
    private var batchSoknadUnderArbeidRepository: BatchSoknadUnderArbeidRepository? = null
    private var batchOpplastetVedleggRepository: BatchOpplastetVedleggRepository? = null

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    @BeforeEach
    fun setup() {
        if (opplastetVedleggRepository == null) {
            opplastetVedleggRepository = OpplastetVedleggRepositoryJdbc(jdbcTemplate)
        }
        if (soknadUnderArbeidRepository == null) {
            soknadUnderArbeidRepository = SoknadUnderArbeidRepositoryJdbc(
                jdbcTemplate, transactionTemplate, opplastetVedleggRepository!!
            )
        }
        if (batchOpplastetVedleggRepository == null) {
            batchOpplastetVedleggRepository = BatchOpplastetVedleggRepositoryJdbc(jdbcTemplate)
        }
        if (batchSoknadUnderArbeidRepository == null) {
            batchSoknadUnderArbeidRepository = BatchSoknadUnderArbeidRepositoryJdbc(
                jdbcTemplate,transactionTemplate, batchOpplastetVedleggRepository!!)
        }
    }

    @Test
    fun hentSoknaderForBatchSkalFinneGamleSoknader() {
        val skalIkkeSlettes = lagSoknadUnderArbeid(BEHANDLINGSID, 13)
        val skalIkkeSlettesId = soknadUnderArbeidRepository!!.opprettSoknad(skalIkkeSlettes, EIER)
        val skalSlettes = lagSoknadUnderArbeid("annen_behandlingsid", 14)
        val skalSlettesId = soknadUnderArbeidRepository!!.opprettSoknad(skalSlettes, EIER)
        val soknader = batchSoknadUnderArbeidRepository!!.hentGamleSoknadUnderArbeidForBatch()
        assertThat(soknader).hasSize(1)
        assertThat(soknader[0]).isEqualTo(skalSlettesId).isNotEqualTo(skalIkkeSlettesId)
    }

    @Test
    fun slettSoknadGittSoknadUnderArbeidIdSkalSletteSoknad() {
        val soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID, 15)
        val soknadUnderArbeidId = soknadUnderArbeidRepository!!.opprettSoknad(soknadUnderArbeid, EIER)
        soknadUnderArbeid.soknadId = soknadUnderArbeidId!!
        val opplastetVedleggUuid = opplastetVedleggRepository!!.opprettVedlegg(lagOpplastetVedlegg(soknadUnderArbeidId), EIER)
        batchSoknadUnderArbeidRepository!!.slettSoknad(soknadUnderArbeid.soknadId)
        assertThat(soknadUnderArbeidRepository!!.hentSoknad(soknadUnderArbeidId, EIER)).isNull()
        assertThat(opplastetVedleggRepository!!.hentVedlegg(opplastetVedleggUuid, EIER)).isNull()
    }

    private fun lagSoknadUnderArbeid(behandlingsId: String, antallDagerSiden: Int): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = behandlingsId,
            tilknyttetBehandlingsId = TILKNYTTET_BEHANDLINGSID,
            eier = EIER,
            jsonInternalSoknad = JSON_INTERNAL_SOKNAD,
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now().minusDays(antallDagerSiden.toLong()).minusMinutes(5),
            sistEndretDato = LocalDateTime.now().minusDays(antallDagerSiden.toLong()).minusMinutes(5)
        )
    }

    private fun lagOpplastetVedlegg(soknadId: Long): OpplastetVedlegg {
        return OpplastetVedlegg(
            eier = EIER,
            vedleggType = OpplastetVedleggType("bostotte|annetboutgift"),
            data = byteArrayOf(1, 2, 3),
            soknadId = soknadId,
            filnavn = "dokumentasjon.pdf",
            sha512 = "aaa"
        )
    }

    companion object {
        private const val EIER = "12345678901"
        private const val BEHANDLINGSID = "1100020"
        private const val TILKNYTTET_BEHANDLINGSID = "4567"
        private val JSON_INTERNAL_SOKNAD = JsonInternalSoknad()
    }
}
