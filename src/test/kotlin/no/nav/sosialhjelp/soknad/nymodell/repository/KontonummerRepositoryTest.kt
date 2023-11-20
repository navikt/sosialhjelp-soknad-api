// package no.nav.sosialhjelp.soknad.nymodell.repository
//
// import no.nav.sosialhjelp.soknad.nymodell.domene.Kilde
// import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.Kontonummer
// import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.KontonummerRepository
// import org.assertj.core.api.Assertions.assertThat
// import org.assertj.core.api.Assertions.assertThatThrownBy
// import org.junit.jupiter.api.Test
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.dao.DuplicateKeyException
//
// class KontonummerRepositoryTest: RepositoryTest() {
//
//    @Autowired
//    private lateinit var kontonummerRepository: KontonummerRepository
//
//    @Test
//    fun `Lagre Kontonummer`() {
//        val soknad = opprettSoknad()
//
//        val Kontonummer = Kontonummer(
//            soknadId = soknad.soknadId,
//            kilde = Kilde.SYSTEM,
//            nummer = "1234556778"
//        )
//        kontonummerRepository.save(Kontonummer)
//
//        val lagretNummer = kontonummerRepository.findById(Kontonummer.soknadId, Kontonummer.kilde)
//            ?: throw IllegalStateException("Kontonummer finnes ikke")
//        assertThat(lagretNummer.nummer).isEqualTo(Kontonummer.nummer)
//    }
//
//    @Test
//    fun `Duplikate id-kolonner skal gi feil`() {
//        val soknad = opprettSoknad()
//
//        val Kontonummer = Kontonummer(soknad.soknadId, Kilde.SYSTEM, "1234556778")
//        kontonummerRepository.save(Kontonummer)
//
//        val annetKontonummer = Kontonummer(soknad.soknadId, Kilde.SYSTEM, "353422354")
//        assertThatThrownBy { kontonummerRepository.save(annetKontonummer) }
//            .isInstanceOf(DuplicateKeyException::class.java)
//    }
//
//    @Test
//    fun `Lagre for SYSTEM og BRUKER pa samme soknad`() {
//        val soknad = opprettSoknad()
//
//        val systemNummer = Kontonummer(soknad.soknadId, Kilde.SYSTEM, "1234556778")
//        kontonummerRepository.save(systemNummer)
//
//        val brukerNummer = Kontonummer(soknad.soknadId, Kilde.BRUKER, "353422354")
//        kontonummerRepository.save(brukerNummer)
//
//        assertThat(kontonummerRepository.findAllBySoknadId(soknad.soknadId).size).isEqualTo(2)
//    }
//
//    @Test
//    fun `By design kan man ikke oppdatere Kontonummer - en ma slette og lagre nytt`() {
//        val soknad = opprettSoknad()
//        val opprinneligNummer = "235253242342"
//        val Kontonummer = Kontonummer(soknad.soknadId, Kilde.BRUKER, opprinneligNummer).let {
//            kontonummerRepository.save(it)
//            kontonummerRepository.delete(it)
//            kontonummerRepository.save(Kontonummer(it.soknadId, it.kilde, "5425335343"))
//        }
//        assertThat(Kontonummer.nummer).isNotEqualTo(opprinneligNummer)
//    }
// }
