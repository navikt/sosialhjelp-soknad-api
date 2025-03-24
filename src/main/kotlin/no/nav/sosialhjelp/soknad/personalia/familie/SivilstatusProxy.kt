package no.nav.sosialhjelp.soknad.personalia.familie

import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.personalia.familie.dto.EktefelleFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.NavnFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.SivilstatusFrontend
import no.nav.sosialhjelp.soknad.v2.familie.EktefelleInput
import no.nav.sosialhjelp.soknad.v2.familie.SivilstandController
import no.nav.sosialhjelp.soknad.v2.familie.SivilstandInput
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.navn.NavnInput
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SivilstatusProxy(private val sivilstandController: SivilstandController) {
    fun updateSivilstand(
        soknadId: String,
        familieFrontend: SivilstatusFrontend,
    ) {
        familieFrontend
            .let {
                if (it.sivilstatus == null) return

                SivilstandInput(
                    sivilstatus = it.sivilstatus.name.let { status -> Sivilstatus.valueOf(status) },
                    ektefelle =
                        it.ektefelle
                            ?.let { ektefelle ->
                                EktefelleInput(
                                    personId = ektefelle.personnummer,
                                    fodselsdato = ektefelle.fodselsdato,
                                    borSammen = familieFrontend.borSammenMed,
                                    navn =
                                        NavnInput(
                                            ektefelle.navn?.fornavn,
                                            ektefelle.navn?.mellomnavn,
                                            ektefelle.navn?.etternavn,
                                        ),
                                )
                            },
                )
            }
            .also { input -> sivilstandController.updateSivilstand(UUID.fromString(soknadId), input) }
    }

    fun getSivilstatus(behandlingsId: String) =
        sivilstandController
            .getSivilstand(UUID.fromString(behandlingsId))
            .let {
                SivilstatusFrontend(
                    kildeErSystem = it.ektefelle?.kildeErSystem,
                    sivilstatus = it.sivilstatus?.let { status -> JsonSivilstatus.Status.valueOf(status.name) },
                    ektefelle =
                        it.ektefelle?.let { ektefelle ->
                            EktefelleFrontend(
                                navn =
                                    NavnFrontend(
                                        ektefelle.navn?.fornavn,
                                        ektefelle.navn?.mellomnavn,
                                        ektefelle.navn?.etternavn,
                                    ),
                                fodselsdato = ektefelle.fodselsdato,
                                personnummer = ektefelle.personId,
                            )
                        },
                    harDiskresjonskode = it.ektefelle?.harDiskresjonskode,
                    borSammenMed = it.ektefelle?.borSammen,
                    erFolkeregistrertSammen = it.ektefelle?.folkeregistrertMedEktefelle,
                )
            }
}
