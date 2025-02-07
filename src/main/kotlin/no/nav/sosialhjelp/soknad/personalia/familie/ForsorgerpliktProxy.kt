package no.nav.sosialhjelp.soknad.personalia.familie

import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag
import no.nav.sosialhjelp.soknad.personalia.familie.dto.AnsvarFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.BarnFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.ForsorgerpliktFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.NavnFrontend
import no.nav.sosialhjelp.soknad.v2.familie.BarnDto
import no.nav.sosialhjelp.soknad.v2.familie.BarnInput
import no.nav.sosialhjelp.soknad.v2.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.v2.familie.ForsorgerDto
import no.nav.sosialhjelp.soknad.v2.familie.ForsorgerInput
import no.nav.sosialhjelp.soknad.v2.familie.ForsorgerpliktController
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ForsorgerpliktProxy(
    private val forsorgerpliktController: ForsorgerpliktController,
) {
    fun getForsorgerplikt(soknadId: String): ForsorgerpliktFrontend =
        forsorgerpliktController
            .getForsorgerplikt(UUID.fromString(soknadId))
            .toForsorgerpliktFrontend()

    fun updateForsorgerplikt(
        soknadId: String,
        forsorgerpliktFrontend: ForsorgerpliktFrontend,
    ) {
        forsorgerpliktController.updateForsorgerplikt(
            soknadId = UUID.fromString(soknadId),
            forsorgerInput =
                ForsorgerInput(
                    barnebidrag = forsorgerpliktFrontend.barnebidrag?.name?.let { Barnebidrag.valueOf(it) },
                    ansvar =
                        forsorgerpliktFrontend.ansvar.map {
                            BarnInput(
                                // bruker personnummer-feltet for UUID (key) i proxy
                                uuid = UUID.fromString(it.barn?.personnummer),
                                personId = null,
                                deltBosted = it.harDeltBosted,
                            )
                        },
                ),
        )
    }
}

private fun ForsorgerDto.toForsorgerpliktFrontend() =
    ForsorgerpliktFrontend(
        harForsorgerplikt = harForsorgerplikt,
        barnebidrag = barnebidrag?.toJsonBarnebidrag(),
        ansvar = this.ansvar.map { it.toAnsvarFrontEnd() },
    )

private fun Barnebidrag.toJsonBarnebidrag(): JsonBarnebidrag.Verdi {
    return when (this) {
        Barnebidrag.BETALER -> JsonBarnebidrag.Verdi.BETALER
        Barnebidrag.MOTTAR -> JsonBarnebidrag.Verdi.MOTTAR
        Barnebidrag.BEGGE -> JsonBarnebidrag.Verdi.BEGGE
        Barnebidrag.INGEN -> JsonBarnebidrag.Verdi.INGEN
    }
}

private fun BarnDto.toAnsvarFrontEnd() =
    AnsvarFrontend(
        barn =
            BarnFrontend(
                navn = navn?.toNavnFrontEnd(),
                fodselsdato = fodselsdato,
                // bruker midlertidig personnummer-feltet for UUID (key) i proxy
                personnummer = this.uuid.toString(),
                fodselsnummer = null,
            ),
        borSammenMed = borSammen,
        erFolkeregistrertSammen = folkeregistrertSammen,
        harDeltBosted = deltBosted,
        samvarsgrad = samvarsgrad,
    )

private fun Navn.toNavnFrontEnd() =
    NavnFrontend(
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn,
        fulltNavn = getFulltNavn(),
    )
