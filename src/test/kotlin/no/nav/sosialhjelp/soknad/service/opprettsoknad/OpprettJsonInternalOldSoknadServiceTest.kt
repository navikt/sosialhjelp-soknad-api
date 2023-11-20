// package no.nav.sosialhjelp.soknad.service.opprettsoknad
//
// import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
// import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
// import no.nav.sosialhjelp.soknad.innsending.OldSoknadService
// import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Bosituasjon
// import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.repository.BosituasjonRepository
// import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.type.Botype
// import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.Eier
// import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.Soknad
// import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.SoknadRepository
// import no.nav.sosialhjelp.soknad.nymodell.fullfort.JsonInternalSoknadCreator
// import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeid
// import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidRepository
// import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidStatus
// import org.assertj.core.api.Assertions.assertThat
// import org.junit.jupiter.api.BeforeEach
// import org.junit.jupiter.api.Test
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.boot.test.context.SpringBootTest
// import org.springframework.test.context.ActiveProfiles
// import org.springframework.transaction.annotation.Transactional
// import java.time.LocalDateTime
// import java.util.*
//
// @Transactional
// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
// @ActiveProfiles(profiles = ["no-redis", "test"])
// class OpprettJsonInternalOldSoknadServiceTest {
//
//    @Autowired
//    private lateinit var creator: JsonInternalSoknadCreator
//
//    @Autowired
//    private lateinit var soknadUnderArbeidRepository: SoknadUnderArbeidRepository
//
//    @Autowired
//    private lateinit var soknadRepository: SoknadRepository
//
//    @Autowired
//    private lateinit var bosituasjonRepository: BosituasjonRepository
//
//    private val EIER = "123456789"
//    private val SOKNAD_ID = UUID.randomUUID()
//
//    @BeforeEach
//    fun setup() {
//        StaticSubjectHandlerImpl().apply {
//            setUser(EIER)
//            SubjectHandlerUtils.setNewSubjectHandlerImpl(this)
//        }
//    }
//
//    @Test
//    fun `Merge Bosituasjon til JsonInternalSoknad`() {
//        opprettSoknadUnderArbeidIDb()
//
//        bosituasjonRepository.save(Bosituasjon(SOKNAD_ID, Botype.EIER, 4))
//
//        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(SOKNAD_ID.toString(), EIER)
//        val jsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad
//            ?: throw IllegalStateException("JsonInternalSoknad er null")
//        val jsonBosituasjon = jsonInternalSoknad.soknad.data.bosituasjon
//
//        assertThat(jsonBosituasjon.botype).isNull()
//        assertThat(jsonBosituasjon.antallPersoner == null).isTrue()
//
//        creator.mapToExistingJsonInternalSoknad(SOKNAD_ID, jsonInternalSoknad)
//
//        val oppdatertJsonInternalSoknad = (soknadUnderArbeidRepository.hentSoknad(SOKNAD_ID.toString(), EIER)
//            .jsonInternalSoknad ?: throw IllegalStateException("JsonInternalSoknad er null"))
//
//        val oppdatertJsonBosituasjon = oppdatertJsonInternalSoknad.soknad.data.bosituasjon
//        assertThat(oppdatertJsonBosituasjon.botype.toString()).isEqualTo(Botype.EIER.name.lowercase())
//        assertThat(oppdatertJsonBosituasjon.antallPersoner).isEqualTo(4)
//    }
//
//    fun opprettSoknadUnderArbeidIDb() {
//        val soknadUnderArbeid = SoknadUnderArbeid(
//            versjon = 0L,
//            behandlingsId = SOKNAD_ID.toString(),
//            tilknyttetBehandlingsId = null,
//            eier = EIER,
//            jsonInternalSoknad = OldSoknadService.createEmptyJsonInternalSoknad(EIER),
//            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
//            opprettetDato = LocalDateTime.now(),
//            sistEndretDato = LocalDateTime.now()
//        )
//        soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, soknadUnderArbeid.eier)
//        soknadRepository.save(
//            Soknad(
//                soknadId = SOKNAD_ID,
//                eier = Eier(EIER),
//                hvorforSoke = "Jeg må",
//                hvaSokesOm = "Penger!"
//            ),
//        )
//    }
// }
