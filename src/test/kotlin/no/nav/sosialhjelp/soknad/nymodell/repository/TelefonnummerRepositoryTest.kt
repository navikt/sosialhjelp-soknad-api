//package no.nav.sosialhjelp.soknad.nymodell.repository
//
//import no.nav.sosialhjelp.soknad.nymodell.domene.Kilde
//import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.Telefonnummer
//import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.TelefonnummerRepository
//import org.assertj.core.api.Assertions.assertThat
//import org.assertj.core.api.Assertions.assertThatThrownBy
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.dao.DuplicateKeyException
//
//class TelefonnummerRepositoryTest: RepositoryTest() {
//
//    @Autowired
//    private lateinit var telefonnummerRepository: TelefonnummerRepository
//
//    @Test
//    fun `Lagre telefonnummer`() {
//        val soknad = opprettSoknad()
//
//        val telefonnummer = Telefonnummer(
//            soknadId = soknad.soknadId,
//            kilde = Kilde.SYSTEM,
//            nummer = "1234556778"
//        )
//        telefonnummerRepository.save(telefonnummer)
//
//        val lagretNummer = telefonnummerRepository.findById(telefonnummer.soknadId, telefonnummer.kilde)
//            ?: throw IllegalStateException("Telefonnummer finnes ikke")
//        assertThat(lagretNummer.nummer).isEqualTo(telefonnummer.nummer)
//    }
//
//    @Test
//    fun `Duplikate id-kolonner skal gi feil`() {
//        val soknad = opprettSoknad()
//
//        val telefonnummer = Telefonnummer(soknad.soknadId, Kilde.SYSTEM, "1234556778")
//        telefonnummerRepository.save(telefonnummer)
//
//        val annetTelefonnummer = Telefonnummer(soknad.soknadId, Kilde.SYSTEM, "353422354")
//        assertThatThrownBy { telefonnummerRepository.save(annetTelefonnummer) }
//            .isInstanceOf(DuplicateKeyException::class.java)
//    }
//
//    @Test
//    fun `Lagre for SYSTEM og BRUKER pa samme soknad`() {
//        val soknad = opprettSoknad()
//
//        val systemNummer = Telefonnummer(soknad.soknadId, Kilde.SYSTEM, "1234556778")
//        telefonnummerRepository.save(systemNummer)
//
//        val brukerNummer = Telefonnummer(soknad.soknadId, Kilde.BRUKER, "353422354")
//        telefonnummerRepository.save(brukerNummer)
//
//        assertThat(telefonnummerRepository.findAllBySoknadId(soknad.soknadId).size).isEqualTo(2)
//    }
//
//    @Test
//    fun `By design kan man ikke oppdatere telefonnummer - en ma slette og lagre nytt`() {
//        val soknad = opprettSoknad()
//        val opprinneligNummer = "235253242342"
//        val telefonnummer = Telefonnummer(soknad.soknadId, Kilde.BRUKER, opprinneligNummer).let {
//            telefonnummerRepository.save(it)
//            telefonnummerRepository.delete(it)
//            telefonnummerRepository.save(Telefonnummer(it.soknadId, it.kilde, "5425335343"))
//        }
//        assertThat(telefonnummer.nummer).isNotEqualTo(opprinneligNummer)
//    }
//}
