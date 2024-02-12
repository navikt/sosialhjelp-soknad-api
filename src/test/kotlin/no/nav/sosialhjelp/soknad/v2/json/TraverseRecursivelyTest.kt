package no.nav.sosialhjelp.soknad.v2.json

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.v2.json.compare.copyJsonClass
import no.nav.sosialhjelp.soknad.v2.json.compare.createGateAdresse
import no.nav.sosialhjelp.soknad.v2.json.compare.createJsonInternalSoknad
import no.nav.sosialhjelp.soknad.v2.json.compare.createMatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.json.compare.JsonStructureComparator
import org.junit.jupiter.api.Test
import java.util.UUID

class TraverseRecursivelyTest {

    private val mapper = jacksonObjectMapper()

    @Test
    fun compareJsonObjects() {
        val original = createJsonInternalSoknad()

        val other = copyJsonClass(original).apply {
            midlertidigAdresse = createMatrikkelAdresse()
            soknad.data.personalia.folkeregistrertAdresse = createMatrikkelAdresse()
            soknad.data.personalia.oppholdsadresse = createGateAdresse()
            vedlegg.vedlegg.clear()
            vedlegg.vedlegg.add(0, original.vedlegg.vedlegg[1])
            vedlegg.vedlegg.add(1, original.vedlegg.vedlegg[0])
            soknad.data.personalia.kontonummer.harIkkeKonto = true
            soknad.data.personalia.kontonummer.verdi = null
            soknad.data.okonomi.oversikt.formue.first().belop = 123445677
            soknad.data.okonomi.opplysninger.utbetaling.clear()
        }

        JsonStructureComparator(UUID.randomUUID())
            .doCompareAndLogErrors(original, other)

        val a = 4
    }
}
