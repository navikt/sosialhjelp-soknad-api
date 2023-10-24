package no.nav.sosialhjelp.soknad.repository

import no.nav.sosialhjelp.soknad.domene.personalia.SoknadKilde
import no.nav.sosialhjelp.soknad.domene.personalia.Kontonummer
import no.nav.sosialhjelp.soknad.domene.personalia.repository.KontonummerRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException

class KontonummerRepositoryTest: RepositoryTest() {

    @Autowired
    private lateinit var KontonummerRepository: KontonummerRepository

    @Test
    fun `Lagre Kontonummer`() {
        val soknad = opprettSoknad()

        val Kontonummer = Kontonummer(
            soknadId = soknad.id,
            kilde = SoknadKilde.SYSTEM,
            nummer = "1234556778"
        )
        KontonummerRepository.save(Kontonummer)

        val lagretNummer = KontonummerRepository.findById(Kontonummer.soknadId, Kontonummer.kilde)
            ?: throw IllegalStateException("Kontonummer finnes ikke")
        assertThat(lagretNummer.nummer).isEqualTo(Kontonummer.nummer)
    }

    @Test
    fun `Duplikate id-kolonner skal gi feil`() {
        val soknad = opprettSoknad()

        val Kontonummer = Kontonummer(soknad.id, SoknadKilde.SYSTEM, "1234556778")
        KontonummerRepository.save(Kontonummer)

        val annetKontonummer = Kontonummer(soknad.id, SoknadKilde.SYSTEM, "353422354")
        assertThatThrownBy { KontonummerRepository.save(annetKontonummer) }
            .isInstanceOf(DuplicateKeyException::class.java)
    }

    @Test
    fun `Lagre for SYSTEM og BRUKER pa samme soknad`() {
        val soknad = opprettSoknad()

        val systemNummer = Kontonummer(soknad.id, SoknadKilde.SYSTEM, "1234556778")
        KontonummerRepository.save(systemNummer)

        val brukerNummer = Kontonummer(soknad.id, SoknadKilde.BRUKER, "353422354")
        KontonummerRepository.save(brukerNummer)

        assertThat(KontonummerRepository.findAllBySoknadId(soknad.id).size).isEqualTo(2)
    }

    @Test
    fun `By design kan man ikke oppdatere Kontonummer - en ma slette og lagre nytt`() {
        val soknad = opprettSoknad()
        val opprinneligNummer = "235253242342"
        val Kontonummer = Kontonummer(soknad.id, SoknadKilde.BRUKER, opprinneligNummer).let {
            KontonummerRepository.save(it)
            KontonummerRepository.delete(it)
            KontonummerRepository.save(Kontonummer(it.soknadId, it.kilde, "5425335343"))
        }
        assertThat(Kontonummer.nummer).isNotEqualTo(opprinneligNummer)
    }
}
