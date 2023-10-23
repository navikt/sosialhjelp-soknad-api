package no.nav.sosialhjelp.soknad.repository

import no.nav.sosialhjelp.soknad.domene.soknad.Fil
import no.nav.sosialhjelp.soknad.domene.soknad.Soknad
import no.nav.sosialhjelp.soknad.domene.soknad.Vedlegg
import no.nav.sosialhjelp.soknad.domene.soknad.VedleggHendelseType
import no.nav.sosialhjelp.soknad.domene.soknad.VedleggType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import java.util.*
import kotlin.jvm.optionals.getOrNull

class VedleggRepositoryTest : RepositoryTest() {

    @Autowired
    lateinit var vedleggRepository: VedleggRepository
    @Autowired
    lateinit var filRepository: FilRepository

    @Test
    fun `Lagre nytt vedlegg`() {
        val soknad = soknadRepository.save(Soknad(id = UUID.randomUUID(), eier = EIER))
        vedleggRepository.save(opprettVedlegg(soknadId = soknad.id))

        assertThat(vedleggRepository.findAll()).size().isEqualTo(1)
        assertThat(vedleggRepository.findAllBySoknadId(soknad.id)).size().isEqualTo(1)
    }

    @Test
    fun `Kan ikke opprette vedlegg uten soknad`() {
        assertThatThrownBy {
            vedleggRepository.save(opprettVedlegg(UUID.randomUUID()))
        }.isInstanceOf(DbActionExecutionException::class.java)
    }

    @Test
    fun `Vedlegg slettes med soknad`() {
        val soknad = soknadRepository.save(Soknad(id = UUID.randomUUID(), eier = EIER))
        val vedlegg = vedleggRepository.save(opprettVedlegg(soknad.id))

        assertThat(vedleggRepository.findById(vedlegg.id).getOrNull()).isNotNull

        soknadRepository.deleteById(soknad.id)

        assertThat(vedleggRepository.findById(vedlegg.id).getOrNull()).isNull()
    }

    @Test
    fun `Opprett fil uten Vedlegg skal gi exception`() {
        soknadRepository.save(Soknad(id = UUID.randomUUID(), eier = EIER))
        assertThatThrownBy {
            filRepository.save(Fil(vedleggId = 1L))
        }.isInstanceOf(DbActionExecutionException::class.java)
    }

    @Test
    fun `Slett Vedlegg sletter ogsa fil`() {
        val soknad = soknadRepository.save(Soknad(id = UUID.randomUUID(), eier = EIER))
        val vedlegg = vedleggRepository.save(opprettVedlegg(soknad.id))

        filRepository.save(Fil(vedleggId = vedlegg.id))
        assertThat(filRepository.findAll()).size().isEqualTo(1)

        vedleggRepository.deleteById(vedlegg.id)
        assertThat(filRepository.findAll()).size().isEqualTo(0)
    }

    @Test
    fun `Slett soknad sletter vedlegg og fil`() {
        val soknad = soknadRepository.save(Soknad(id = UUID.randomUUID(), eier = EIER))
        val vedlegg = vedleggRepository.save(opprettVedlegg(soknadId = soknad.id))

        filRepository.save(Fil(vedleggId = vedlegg.id))
        assertThat(filRepository.findAll()).size().isEqualTo(1)
        assertThat(vedleggRepository.findAll()).size().isEqualTo(1)

        soknadRepository.deleteById(vedlegg.soknadId)
        assertThat(filRepository.findAll()).size().isEqualTo(0)
        assertThat(vedleggRepository.findAll()).size().isEqualTo(0)
    }

    @Test
    fun `Finner alle vedlegg for soknad`() {
        val soknad = opprettSoknad()
        opprettVedlegg(soknadId = soknad.id)
        opprettVedlegg(soknadId = soknad.id)

        val alleVedlegg = vedleggRepository.findAllBySoknadId(soknad.id)
        assertThat(alleVedlegg).size().isEqualTo(2)
    }

    @Test
    fun `Finner alle filer for vedlegg`() {
        val soknad = opprettSoknad()
        val vedlegg = opprettVedlegg(soknadId = soknad.id)
        opprettFil(vedleggId = vedlegg.id)
        opprettFil(vedleggId = vedlegg.id)

        assertThat(filRepository.findAllByVedleggId(vedleggId = vedlegg.id)).size().isEqualTo(2)
    }

    @Test
    fun `Opprett flere vedlegg`() {
        val soknad = opprettSoknad()
        (1..10).map { opprettVedlegg(soknad.id) }
        assertThat(vedleggRepository.findAll()).size().isEqualTo(10)

        soknadRepository.deleteById(soknad.id)
        assertThat(vedleggRepository.findAll()).isEmpty()
    }

    fun opprettVedlegg(soknadId: UUID): Vedlegg = vedleggRepository.save(
        Vedlegg(
            soknadId = soknadId,
            status = "kreves",
            vedleggType = VedleggType.BARNEBIDRAG,
            hendelseType = VedleggHendelseType.BRUKER,
            hendelseReferanse = UUID.randomUUID().toString()
        )
    )
    fun opprettFil(vedleggId: Long) = filRepository.save(Fil(vedleggId = vedleggId))
}
