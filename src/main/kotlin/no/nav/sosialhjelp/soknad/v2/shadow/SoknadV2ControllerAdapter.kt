package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sosialhjelp.soknad.arbeid.ArbeidRessurs
import no.nav.sosialhjelp.soknad.begrunnelse.BegrunnelseRessurs
import no.nav.sosialhjelp.soknad.bosituasjon.BosituasjonRessurs
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresserFrontendInput
import no.nav.sosialhjelp.soknad.personalia.familie.dto.ForsorgerpliktFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.SivilstatusFrontend
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerInputDTO
import no.nav.sosialhjelp.soknad.utdanning.UtdanningFrontend
import no.nav.sosialhjelp.soknad.v2.familie.BarnInput
import no.nav.sosialhjelp.soknad.v2.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.v2.familie.EktefelleInput
import no.nav.sosialhjelp.soknad.v2.familie.ForsorgerInput
import no.nav.sosialhjelp.soknad.v2.familie.ForsorgerpliktController
import no.nav.sosialhjelp.soknad.v2.familie.SivilstandController
import no.nav.sosialhjelp.soknad.v2.familie.SivilstandInput
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.kontakt.TelefonnummerController
import no.nav.sosialhjelp.soknad.v2.kontakt.TelefonnummerInput
import no.nav.sosialhjelp.soknad.v2.livssituasjon.ArbeidController
import no.nav.sosialhjelp.soknad.v2.livssituasjon.ArbeidInput
import no.nav.sosialhjelp.soknad.v2.livssituasjon.BosituasjonController
import no.nav.sosialhjelp.soknad.v2.livssituasjon.BosituasjonDto
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Botype
import no.nav.sosialhjelp.soknad.v2.livssituasjon.IkkeStudentInput
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Studentgrad
import no.nav.sosialhjelp.soknad.v2.livssituasjon.StudentgradInput
import no.nav.sosialhjelp.soknad.v2.livssituasjon.UtdanningController
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.shadow.adapters.V2AdresseControllerAdapter
import no.nav.sosialhjelp.soknad.v2.soknad.BegrunnelseController
import no.nav.sosialhjelp.soknad.v2.soknad.BegrunnelseDto
import no.nav.sosialhjelp.soknad.v2.soknad.HarIkkeKontoInput
import no.nav.sosialhjelp.soknad.v2.soknad.KontonummerBrukerInput
import no.nav.sosialhjelp.soknad.v2.soknad.KontonummerController
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

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
    private val v2AdresseControllerAdapter: V2AdresseControllerAdapter,
    private val transactionTemplate: TransactionTemplate,
) : ControllerAdapter {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun updateArbeid(
        soknadId: String,
        arbeidFrontend: ArbeidRessurs.ArbeidsforholdRequest,
    ) {
        log.info("NyModell: Oppdaterer Arbeid for $soknadId")

        arbeidFrontend.kommentarTilArbeidsforhold?.let {
            runWithNestedTransaction {
                arbeidController.updateKommentarArbeidsforhold(UUID.fromString(soknadId), ArbeidInput(it))
            }
                .onFailure { log.warn("Ny Modell: Oppdatere arbeid feilet", it) }
        }
    }

    override fun updateBegrunnelse(
        soknadId: String,
        begrunnelseFrontend: BegrunnelseRessurs.BegrunnelseFrontend,
    ) {
        log.info("NyModell: Oppdaterer Begrunnelse for $soknadId")

        runWithNestedTransaction {
            with(begrunnelseFrontend) {
                if (hvaSokesOm != null || hvorforSoke != null) {
                    begrunnelseController.updateBegrunnelse(
                        UUID.fromString(soknadId),
                        BegrunnelseDto(
                            hvaSokesOm = hvaSokesOm ?: "",
                            hvorforSoke = hvorforSoke ?: "",
                        ),
                    )
                }
            }
        }
            .onFailure { log.warn("Ny Modell: Oppdatere Begrunnelse feilet", it) }
    }

    override fun updateBosituasjon(
        soknadId: String,
        bosituasjonFrontend: BosituasjonRessurs.BosituasjonFrontend,
    ) {
        log.info("NyModell: Oppdaterer Bosituasjon for $soknadId")

        runWithNestedTransaction {
            with(bosituasjonFrontend) {
                if (botype != null || antallPersoner != null) {
                    bosituasjonController.updateBosituasjon(
                        UUID.fromString(soknadId),
                        BosituasjonDto(
                            botype = botype?.let { Botype.valueOf(it.name) },
                            antallPersoner = antallPersoner,
                        ),
                    )
                }
            }
        }
            .onFailure { log.warn("Ny modell: Oppdatere Bosituasjon feilet", it) }
    }

    override fun updateKontonummer(
        soknadId: String,
        kontoInputDto: KontonummerInputDTO,
    ) {
        log.info("NyModell: Oppdaterer Kontonummer for $soknadId")

        val kontoInput =
            kontoInputDto.run {
                when {
                    harIkkeKonto == true -> HarIkkeKontoInput(harIkkeKonto)
                    brukerutfyltVerdi != null -> KontonummerBrukerInput(brukerutfyltVerdi)
                    else -> return
                }
            }
        runWithNestedTransaction {
            kontonummerController.updateKontoInformasjonBruker(
                soknadId = UUID.fromString(soknadId),
                input = kontoInput,
            )
        }
            .onFailure { log.warn("Ny modell: Oppdatere kontonummer feilet", it) }
    }

    override fun updateTelefonnummer(
        soknadId: String,
        telefonnummerBruker: String?,
    ) {
        log.info("NyModell: Oppdaterer Telefonnummer for $soknadId")

        runWithNestedTransaction {
            telefonnummerController.updateTelefonnummer(UUID.fromString(soknadId), TelefonnummerInput())
        }
            .onFailure { log.warn("Ny modell: Oppdatere Telefonnummer feilet", it) }
    }

    override fun updateUtdanning(
        soknadId: String,
        utdanningFrontend: UtdanningFrontend,
    ) {
        log.info("NyModell: Oppdaterer Utdanning for $soknadId")

        val utdanningInput =
            utdanningFrontend.run {
                when {
                    erStudent == false -> IkkeStudentInput()
                    erStudent == true && studengradErHeltid != null -> {
                        StudentgradInput(if (studengradErHeltid as Boolean) Studentgrad.HELTID else Studentgrad.DELTID)
                    }
                    else -> return
                }
            }

        runWithNestedTransaction {
            utdanningFrontend.erStudent?.let {
                utdanningController.updateUtdanning(
                    soknadId = UUID.fromString(soknadId),
                    input = utdanningInput,
                )
            }
        }
            .onFailure { log.warn("Ny modell: Oppdatere Utdanning feilet", it) }
    }

    override fun updateSivilstand(
        soknadId: String,
        familieFrontend: SivilstatusFrontend,
    ) {
        log.info("NyModell: Oppdaterer Sivilstatus for $soknadId")

        val sivilstandInput =
            familieFrontend.run {
                SivilstandInput(
                    sivilstatus?.name?.let { Sivilstatus.valueOf(it) },
                    ektefelle?.let {
                        EktefelleInput(
                            ektefelle.personnummer,
                            Navn(ektefelle.navn?.fornavn ?: "", ektefelle.navn?.mellomnavn, ektefelle.navn?.etternavn ?: ""),
                            ektefelle.fodselsdato,
                            familieFrontend.borSammenMed,
                        )
                    },
                )
            }
        runWithNestedTransaction {
            sivilstandController.updateSivilstand(UUID.fromString(soknadId), sivilstandInput)
        }
            .onFailure { log.warn("Ny modell: Oppdatering av Sivilstand feilet", it) }
    }

    override fun updateForsorger(
        soknadId: String,
        forsorgerpliktFrontend: ForsorgerpliktFrontend,
    ) {
        log.info("NyModell: Oppdaterer Forsorger for $soknadId")

        val forsorgerInput =
            forsorgerpliktFrontend.run {
                ForsorgerInput(
                    barnebidrag?.name?.let { Barnebidrag.valueOf(it) },
                    ansvar.map { BarnInput(null, it.barn?.personnummer, it.harDeltBosted) },
                )
            }
        runWithNestedTransaction {
            forsorgerpliktController.updateForsorgerplikt(UUID.fromString(soknadId), forsorgerInput)
        }
            .onFailure { log.warn("Ny modell: Oppdatering av forsorgerplikt feilet", it) }
    }

    override fun updateAdresseOgNavEnhet(
        soknadId: String,
        adresser: AdresserFrontendInput,
        navEnhet: NavEnhetFrontend?,
    ) {
        adresser.valg?.let {
            runWithNestedTransaction {
                v2AdresseControllerAdapter.updateAdresse(soknadId = UUID.fromString(soknadId), it, adresser.soknad)
            }
                .onFailure { log.warn("Ny modell: Oppdatering av adresser feilet.", it) }
        }
            ?: log.warn("Ny modell: Oppdatering av adresser feilet. Adressevalg er null.")

        navEnhet?.let {
            runWithNestedTransaction {
                v2AdresseControllerAdapter.updateNavEnhet(soknadId = UUID.fromString(soknadId), it)
            }
                .onFailure { log.warn("Ny modell: Oppdatering av NAV-enhet feilet.", it) }
        }
            ?: log.warn("Ny modell: Oppdatering av NAV-enhet feilet. NAV-enhet er null.")
    }

    private fun runWithNestedTransaction(function: () -> Unit): Result<Unit> {
        return kotlin.runCatching {
            transactionTemplate.propagationBehavior = TransactionDefinition.PROPAGATION_NESTED
            transactionTemplate.execute { function.invoke() }
        }
    }
}
