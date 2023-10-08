package no.nav.sosialhjelp.soknad.repository

import no.nav.sosialhjelp.soknad.model.Fil
import no.nav.sosialhjelp.soknad.model.Soknad
import no.nav.sosialhjelp.soknad.model.Vedlegg
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
        val soknad = soknadRepository.save(Soknad(soknadId = UUID.randomUUID()))
        vedleggRepository.save(Vedlegg(soknadId = soknad.soknadId))

        assertThat(vedleggRepository.findAll()).size().isEqualTo(1)
        assertThat(vedleggRepository.findAllBySoknadId(soknad.soknadId)).size().isEqualTo(1)
    }

    @Test
    fun `Kan ikke opprette vedlegg uten soknad`() {
        assertThatThrownBy {
            vedleggRepository.save(Vedlegg(soknadId = UUID.randomUUID()))
        }.isInstanceOf(DbActionExecutionException::class.java)
    }

    @Test
    fun `Vedlegg slettes med soknad`() {
        val soknad = soknadRepository.save(Soknad(soknadId = UUID.randomUUID()))
        val vedlegg = vedleggRepository.save(Vedlegg(soknadId = soknad.soknadId))

        assertThat(vedleggRepository.findById(vedlegg.id).getOrNull()).isNotNull

        soknadRepository.deleteById(soknad.id)

        assertThat(vedleggRepository.findById(vedlegg.id).getOrNull()).isNull()
    }

    @Test
    fun `Opprett fil uten Vedlegg skal gi exception`() {
        soknadRepository.save(Soknad(soknadId = UUID.randomUUID()))
        assertThatThrownBy {
            filRepository.save(Fil(vedleggId = 1L))
        }.isInstanceOf(DbActionExecutionException::class.java)
    }

    @Test
    fun `Slett Vedlegg sletter ogsa fil`() {
        val soknad = soknadRepository.save(Soknad(soknadId = UUID.randomUUID()))
        val vedlegg = vedleggRepository.save(Vedlegg(soknadId = soknad.soknadId))

        filRepository.save(Fil(vedleggId = vedlegg.id))
        assertThat(filRepository.findAll()).size().isEqualTo(1)

        vedleggRepository.deleteById(vedlegg.id)
        assertThat(filRepository.findAll()).size().isEqualTo(0)
    }

    @Test
    fun `Slett soknad sletter vedlegg og fil`() {
        val soknad = soknadRepository.save(Soknad(soknadId = UUID.randomUUID()))
        val vedlegg = vedleggRepository.save(Vedlegg(soknadId = soknad.soknadId))

        filRepository.save(Fil(vedleggId = vedlegg.id))
        assertThat(filRepository.findAll()).size().isEqualTo(1)
        assertThat(vedleggRepository.findAll()).size().isEqualTo(1)

        soknadRepository.deleteById(vedlegg.id)
        assertThat(filRepository.findAll()).size().isEqualTo(0)
        assertThat(vedleggRepository.findAll()).size().isEqualTo(0)
    }

    @Test
    fun `Finner alle vedlegg for soknad`() {
        val soknad = opprettSoknad()
        opprettVedlegg(soknadId = soknad.soknadId)
        opprettVedlegg(soknadId = soknad.soknadId)

        val alleVedlegg = vedleggRepository.findAllBySoknadId(soknad.soknadId)
        assertThat(alleVedlegg).size().isEqualTo(2)
    }

    @Test
    fun `Finner alle filer for vedlegg`() {
        val soknad = opprettSoknad()
        val vedlegg = opprettVedlegg(soknadId = soknad.soknadId)
        opprettFil(vedleggId = vedlegg.id)
        opprettFil(vedleggId = vedlegg.id)

        assertThat(filRepository.findAllByVedleggId(vedleggId = vedlegg.id)).size().isEqualTo(2)
    }

    fun opprettVedlegg(soknadId: UUID) = vedleggRepository.save(Vedlegg(soknadId = soknadId))
    fun opprettFil(vedleggId: Long) = filRepository.save(Fil(vedleggId = vedleggId))

}
