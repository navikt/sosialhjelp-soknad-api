//package no.nav.sosialhjelp.soknad.nymodell.repository
//
//import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.Eier
//import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.Soknad
//import no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg.FilMeta
//import no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg.Vedlegg
//import no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg.repository.FilMetaRepository
//import no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg.repository.VedleggRepository
//import no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg.typer.VedleggHendelseType
//import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
//import org.assertj.core.api.Assertions.assertThat
//import org.assertj.core.api.Assertions.assertThatThrownBy
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.data.relational.core.conversion.DbActionExecutionException
//import java.util.*
//import kotlin.jvm.optionals.getOrNull
//
//class VedleggRepositoryTest : RepositoryTest() {
//
//    @Autowired
//    lateinit var vedleggRepository: VedleggRepository
//    @Autowired
//    lateinit var filMetaRepository: FilMetaRepository
//
//    @Test
//    fun `Lagre nytt vedlegg`() {
//        val soknadDetaljer = soknadRepository.save(Soknad(soknadId = UUID.randomUUID(), eier = Eier(EIER)))
//        vedleggRepository.save(opprettVedlegg(soknadId = soknadDetaljer.soknadId))
//
//        assertThat(vedleggRepository.findAll()).size().isEqualTo(1)
//        assertThat(vedleggRepository.findAllBySoknadId(soknadDetaljer.soknadId)).size().isEqualTo(1)
//    }
//
//    @Test
//    fun `Kan ikke opprette vedlegg uten soknad`() {
//        assertThatThrownBy {
//            vedleggRepository.save(opprettVedlegg(UUID.randomUUID()))
//        }.isInstanceOf(DbActionExecutionException::class.java)
//    }
//
//    @Test
//    fun `Vedlegg slettes med soknad`() {
//        val soknadDetaljer = soknadRepository.save(Soknad(id = UUID.randomUUID(), eier = Eier(EIER)))
//        val vedlegg = vedleggRepository.save(opprettVedlegg(soknadDetaljer.id))
//
//        assertThat(vedleggRepository.findById(vedlegg.soknadId).getOrNull()).isNotNull
//
//        soknadRepository.deleteById(soknadDetaljer.id)
//
//        assertThat(vedleggRepository.findById(vedlegg.soknadId).getOrNull()).isNull()
//    }
//
//    @Test
//    fun `Opprett fil uten Vedlegg skal gi exception`() {
//        soknadRepository.save(Soknad(soknadId = UUID.randomUUID(), eier = Eier(EIER)))
//        assertThatThrownBy {
//            filMetaRepository.save(FilMeta(vedleggId = 1L))
//        }.isInstanceOf(DbActionExecutionException::class.java)
//    }
//
//    @Test
//    fun `Slett Vedlegg sletter ogsa fil`() {
//        val soknadDetaljer = soknadRepository.save(Soknad(soknadId = UUID.randomUUID(), eier = Eier(EIER)))
//        val vedlegg = vedleggRepository.save(opprettVedlegg(soknadDetaljer.soknadId))
//
//        filMetaRepository.save(FilMeta(vedleggId = vedlegg.id))
//        assertThat(filMetaRepository.findAll()).size().isEqualTo(1)
//
//        vedleggRepository.deleteById(vedlegg.id)
//        assertThat(filMetaRepository.findAll()).size().isEqualTo(0)
//    }
//
//    @Test
//    fun `Slett soknad sletter vedlegg og fil`() {
//        val soknadDetaljer = soknadRepository.save(Soknad(soknadId = UUID.randomUUID(), eier = Eier(EIER)))
//        val vedlegg = vedleggRepository.save(opprettVedlegg(soknadId = soknadDetaljer.soknadId))
//
//        filMetaRepository.save(FilMeta(vedleggId = vedlegg.id))
//        assertThat(filMetaRepository.findAll()).size().isEqualTo(1)
//        assertThat(vedleggRepository.findAll()).size().isEqualTo(1)
//
//        soknadRepository.deleteById(vedlegg.soknadId)
//        assertThat(filMetaRepository.findAll()).size().isEqualTo(0)
//        assertThat(vedleggRepository.findAll()).size().isEqualTo(0)
//    }
//
//    @Test
//    fun `Finner alle vedlegg for soknad`() {
//        val soknad = opprettSoknad()
//        opprettVedlegg(soknadId = soknad.soknadId)
//        opprettVedlegg(soknadId = soknad.soknadId)
//
//        val alleVedlegg = vedleggRepository.findAllBySoknadId(soknad.soknadId)
//        assertThat(alleVedlegg).size().isEqualTo(2)
//    }
//
//    @Test
//    fun `Finner alle filer for vedlegg`() {
//        val soknad = opprettSoknad()
//        val vedlegg = opprettVedlegg(soknadId = soknad.soknadId)
//        opprettFil(vedleggId = vedlegg.id)
//        opprettFil(vedleggId = vedlegg.id)
//
//        assertThat(filMetaRepository.findAllByVedleggId(vedleggId = vedlegg.id)).size().isEqualTo(2)
//    }
//
//    @Test
//    fun `Opprett flere vedlegg`() {
//        val soknad = opprettSoknad()
//        (1..10).map { opprettVedlegg(soknad.soknadId) }
//        assertThat(vedleggRepository.findAll()).size().isEqualTo(10)
//
//        soknadRepository.deleteById(soknad.soknadId)
//        assertThat(vedleggRepository.findAll()).isEmpty()
//    }
//
//    fun opprettVedlegg(soknadId: UUID): Vedlegg = vedleggRepository.save(
//        Vedlegg(
//            soknadId = soknadId,
//            status = "kreves",
//            vedleggType = VedleggType.BarnebidragBetaler,
//            hendelseType = VedleggHendelseType.BRUKER,
//            hendelseReferanse = UUID.randomUUID().toString()
//        )
//    )
//    fun opprettFil(vedleggId: Long) = filMetaRepository.save(FilMeta(vedleggId = vedleggId))
//}
