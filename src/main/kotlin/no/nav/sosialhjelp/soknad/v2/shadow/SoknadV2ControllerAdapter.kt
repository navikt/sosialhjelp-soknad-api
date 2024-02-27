package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sosialhjelp.soknad.arbeid.ArbeidRessurs
import no.nav.sosialhjelp.soknad.begrunnelse.BegrunnelseRessurs
import no.nav.sosialhjelp.soknad.bosituasjon.BosituasjonRessurs
import no.nav.sosialhjelp.soknad.personalia.familie.dto.ForsorgerpliktFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.SivilstatusFrontend
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerRessurs
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.TelefonnummerRessurs
import no.nav.sosialhjelp.soknad.utdanning.UtdanningRessurs
import no.nav.sosialhjelp.soknad.v2.brukerdata.Botype
import no.nav.sosialhjelp.soknad.v2.brukerdata.Studentgrad
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.ArbeidController
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.ArbeidInput
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.BegrunnelseController
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.BegrunnelseDto
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.BosituasjonController
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.BosituasjonDto
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.KontoInformasjonInput
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.KontonummerController
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.TelefonnummerController
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.TelefonnummerInput
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.UtdanningController
import no.nav.sosialhjelp.soknad.v2.brukerdata.controller.UtdanningDto
import no.nav.sosialhjelp.soknad.v2.familie.BarnInput
import no.nav.sosialhjelp.soknad.v2.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.v2.familie.EktefelleInput
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.familie.forsorgerplikt.ForsorgerInput
import no.nav.sosialhjelp.soknad.v2.familie.forsorgerplikt.ForsorgerpliktController
import no.nav.sosialhjelp.soknad.v2.familie.sivilstatus.SivilstandController
import no.nav.sosialhjelp.soknad.v2.familie.sivilstatus.SivilstandInput
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

@Controller
class SoknadV2ControllerAdapter(
    private val arbeidController: ArbeidController,
    private val begrunnelseController: BegrunnelseController,
    private val bosituasjonController: BosituasjonController,
    private val kontonummerController: KontonummerController,
    private val telefonnummerController: TelefonnummerController,
    private val utdanningController: UtdanningController,
    private val sivilstandController: SivilstandController,
    private val forsorgerpliktController: ForsorgerpliktController,
    private val transactionTemplate: TransactionTemplate
) : ControllerAdapter {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun updateArbeid(
        soknadId: String,
        arbeidFrontend: ArbeidRessurs.ArbeidsforholdRequest,
    ) {
        log.info("NyModell: Oppdaterer Arbeid for $soknadId")

        arbeidFrontend.kommentarTilArbeidsforhold?.let {
            runWithNewTransaction {
                arbeidController.updateKommentarArbeidsforhold(UUID.fromString(soknadId), ArbeidInput(it))
            }
                .onFailure { log.error("Ny Modell: Oppdatere arbeid feilet", it) }
        }
    }

    override fun updateBegrunnelse(
        soknadId: String,
        begrunnelseFrontend: BegrunnelseRessurs.BegrunnelseFrontend,
    ) {
        log.info("NyModell: Oppdaterer Begrunnelse for $soknadId")

        runWithNewTransaction {
            with(begrunnelseFrontend) {
                if (hvaSokesOm != null || hvorforSoke != null) {
                    begrunnelseController.updateBegrunnelse(
                        UUID.fromString(soknadId),
                        BegrunnelseDto(
                            hvaSokesOm = hvaSokesOm,
                            hvorforSoke = hvorforSoke
                        )
                    )
                }
            }
        }
            .onFailure { log.error("Ny Modell: Oppdatere Begrunnelse feilet", it) }
    }

    override fun updateBosituasjon(
        soknadId: String,
        bosituasjonFrontend: BosituasjonRessurs.BosituasjonFrontend,
    ) {
        log.info("NyModell: Oppdaterer Bosituasjon for $soknadId")

        runWithNewTransaction {
            with(bosituasjonFrontend) {
                if (botype != null || antallPersoner != null) {
                    bosituasjonController.updateBosituasjon(
                        UUID.fromString(soknadId),
                        BosituasjonDto(
                            botype = botype?.let { Botype.valueOf(it.name) },
                            antallPersoner = antallPersoner
                        )
                    )
                }
            }
        }
            .onFailure { log.error("Ny modell: Oppdatere Bosituasjon feilet", it) }
    }

    override fun updateKontonummer(
        soknadId: String,
        kontoInputDto: KontonummerRessurs.KontonummerInputDTO,
    ) {
        log.info("NyModell: Oppdaterer Kontonummer for $soknadId")

        runWithNewTransaction {
            with(kontoInputDto) {
                if (harIkkeKonto != null || brukerutfyltVerdi != null) {
                    kontonummerController.updateKontoInformasjonBruker(
                        UUID.fromString(soknadId),
                        KontoInformasjonInput(
                            harIkkeKonto = harIkkeKonto,
                            kontonummerBruker = brukerutfyltVerdi
                        )
                    )
                }
            }
        }
            .onFailure { log.error("Ny modell: Oppdatere kontonummer feilet", it) }
    }

    override fun updateTelefonnummer(
        soknadId: String,
        telefonnummerFrontend: TelefonnummerRessurs.TelefonnummerFrontend,
    ) {
        log.info("NyModell: Oppdaterer Telefonnummer for $soknadId")

        runWithNewTransaction {
            telefonnummerFrontend.brukerutfyltVerdi?.let {
                telefonnummerController.updateTelefonnummer(UUID.fromString(soknadId), TelefonnummerInput(it))
            }
        }
            .onFailure { log.error("Ny modell: Oppdatere Telefonnummer feilet", it) }
    }

    override fun updateUtdanning(
        soknadId: String,
        utdanningFrontend: UtdanningRessurs.UtdanningFrontend,
    ) {
        log.info("NyModell: Oppdaterer Utdanning for $soknadId")
        runWithNewTransaction {
            utdanningFrontend.erStudent?.let {
                utdanningController.updateUtdanning(
                    UUID.fromString(soknadId),
                    UtdanningDto(
                        erStudent = it,
                        studentgrad = utdanningFrontend.studengradErHeltid
                            ?.let { if (it) Studentgrad.HELTID else Studentgrad.DELTID }
                    )
                )
            }
        }
            .onFailure { log.error("Ny modell: Oppdatere Utdanning feilet", it) }
    }

    override fun updateSivilstand(soknadId: String, familieFrontend: SivilstatusFrontend) {
        log.info("NyModell: Oppdaterer Sivilstatus for $soknadId")

        val sivilstandInput = familieFrontend.run {
            SivilstandInput(
                sivilstatus?.name?.let { Sivilstatus.valueOf(it) },
                ektefelle?.let {
                    EktefelleInput(
                        ektefelle.personnummer,
                        Navn(ektefelle.navn?.fornavn ?: "", ektefelle.navn?.mellomnavn, ektefelle.navn?.etternavn ?: ""),
                        ektefelle.fodselsdato,
                        familieFrontend.borSammenMed
                    )
                },
            )
        }
        runWithNewTransaction {
            sivilstandController.updateSivilstand(UUID.fromString(soknadId), sivilstandInput)
        }
            .onFailure { log.error("Ny modell: Oppdatering av Sivilstand feilet", it) }
    }

    override fun updateForsorger(soknadId: String, forsorgerpliktFrontend: ForsorgerpliktFrontend) {
        log.info("NyModell: Oppdaterer Forsorger for $soknadId")

        val forsorgerInput = forsorgerpliktFrontend.run {
            ForsorgerInput(
                barnebidrag?.name?.let { Barnebidrag.valueOf(it) },
                ansvar.map { BarnInput(null, it.barn?.personnummer, it.harDeltBosted) }
            )
        }
        runWithNewTransaction {
            forsorgerpliktController.updateForsorgerplikt(UUID.fromString(soknadId), forsorgerInput)
        }
            .onFailure { log.error("Ny modell: Oppdatering av forsorgerplikt feilet", it) }
    }

    private fun runWithNewTransaction(function: () -> Unit): Result<Unit> {
        return kotlin.runCatching {
            transactionTemplate.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
            transactionTemplate.execute { function.invoke() }
        }
    }
}
