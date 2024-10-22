package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sosialhjelp.soknad.arbeid.ArbeidRessurs
import no.nav.sosialhjelp.soknad.begrunnelse.BegrunnelseRessurs
import no.nav.sosialhjelp.soknad.bosituasjon.BosituasjonRessurs
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresserFrontendInput
import no.nav.sosialhjelp.soknad.personalia.familie.dto.ForsorgerpliktFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.SivilstatusFrontend
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerInputDto
import no.nav.sosialhjelp.soknad.situasjonsendring.SituasjonsendringFrontend
import no.nav.sosialhjelp.soknad.utdanning.UtdanningFrontend
import no.nav.sosialhjelp.soknad.utgifter.BarneutgiftRessurs
import no.nav.sosialhjelp.soknad.utgifter.BoutgiftRessurs
import no.nav.sosialhjelp.soknad.v2.bostotte.BostotteController
import no.nav.sosialhjelp.soknad.v2.bostotte.BostotteInput
import no.nav.sosialhjelp.soknad.v2.bostotte.SamtykkeInput
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
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektSkattetatenController
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.BarneutgiftController
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.BoutgiftController
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.HarBarneutgifterInput
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.HarBoutgifterInput
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.HarIkkeBarneutgifterInput
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.HarIkkeBoutgifterInput
import no.nav.sosialhjelp.soknad.v2.shadow.adapters.V2AdresseControllerAdapter
import no.nav.sosialhjelp.soknad.v2.situasjonsendring.SituasjonsendringController
import no.nav.sosialhjelp.soknad.v2.situasjonsendring.SituasjonsendringDto
import no.nav.sosialhjelp.soknad.v2.soknad.BegrunnelseController
import no.nav.sosialhjelp.soknad.v2.soknad.BegrunnelseDto
import no.nav.sosialhjelp.soknad.v2.soknad.HarIkkeKontoInput
import no.nav.sosialhjelp.soknad.v2.soknad.KontonummerBrukerInput
import no.nav.sosialhjelp.soknad.v2.soknad.KontonummerController
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@Component
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
    private val boutgiftController: BoutgiftController,
    private val barneutgiftController: BarneutgiftController,
    private val situasjonsendringController: SituasjonsendringController,
    private val bostotteController: BostotteController,
    private val inntektSkattetatenController: InntektSkattetatenController,
    platformTransactionManager: PlatformTransactionManager,
) : V2ControllerAdapter {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val transactionTemplate = TransactionTemplate(platformTransactionManager)

    override fun updateArbeid(
        soknadId: String,
        arbeidFrontend: ArbeidRessurs.ArbeidsforholdRequest,
    ) {
        logger.info("NyModell: Oppdaterer Arbeid.")

        runWithNestedTransaction {
            arbeidController.updateKommentarArbeidsforhold(
                soknadId = UUID.fromString(soknadId),
                input = ArbeidInput(arbeidFrontend.kommentarTilArbeidsforhold),
            )
        }.onFailure { logger.warn("Ny Modell: Oppdatere arbeid feilet", it) }
    }

    override fun updateBegrunnelse(
        soknadId: String,
        begrunnelseFrontend: BegrunnelseRessurs.BegrunnelseFrontend,
    ) {
        logger.info("NyModell: Oppdaterer Begrunnelse.")

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
        }.onFailure { logger.warn("Ny Modell: Oppdatere Begrunnelse feilet", it) }
    }

    override fun updateBosituasjon(
        soknadId: String,
        bosituasjonFrontend: BosituasjonRessurs.BosituasjonFrontend,
    ) {
        logger.info("NyModell: Oppdaterer Bosituasjon.")

        runWithNestedTransaction {
            with(bosituasjonFrontend) {
                bosituasjonController.updateBosituasjon(
                    UUID.fromString(soknadId),
                    BosituasjonDto(
                        botype = botype?.let { Botype.valueOf(it.name) },
                        antallPersoner = antallPersoner,
                    ),
                )
            }
        }.onFailure { logger.warn("Ny modell: Oppdatere Bosituasjon feilet", it) }
    }

    override fun updateKontonummer(
        soknadId: String,
        kontoInputDto: KontonummerInputDto,
    ) {
        logger.info("NyModell: Oppdaterer Kontonummer.")

        val kontoInput =
            kontoInputDto.run {
                when {
                    harIkkeKonto == true -> HarIkkeKontoInput(harIkkeKonto)
                    brukerutfyltVerdi != null -> KontonummerBrukerInput(brukerutfyltVerdi)
                    else -> KontonummerBrukerInput(null)
                }
            }
        runWithNestedTransaction {
            kontonummerController.updateKontoInformasjonBruker(
                soknadId = UUID.fromString(soknadId),
                input = kontoInput,
            )
        }.onFailure { logger.warn("Ny modell: Oppdatere kontonummer feilet", it) }
    }

    override fun updateTelefonnummer(
        soknadId: String,
        telefonnummerBruker: String?,
    ) {
        logger.info("NyModell: Oppdaterer Telefonnummer.")

        runWithNestedTransaction {
            telefonnummerController.updateTelefonnummer(
                UUID.fromString(soknadId),
                TelefonnummerInput(telefonnummerBruker),
            )
        }.onFailure { logger.warn("Ny modell: Oppdatere Telefonnummer feilet", it) }
    }

    override fun updateUtdanning(
        soknadId: String,
        utdanningFrontend: UtdanningFrontend,
    ) {
        logger.info("NyModell: Oppdaterer Utdanning.")

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
        }.onFailure { logger.warn("Ny modell: Oppdatere Utdanning feilet", it) }
    }

    override fun updateSivilstand(
        soknadId: String,
        familieFrontend: SivilstatusFrontend,
    ) {
        logger.info("NyModell: Oppdaterer Sivilstatus.")

        val sivilstandInput =
            familieFrontend.run {
                SivilstandInput(
                    sivilstatus?.name?.let { Sivilstatus.valueOf(it) },
                    ektefelle?.let {
                        EktefelleInput(
                            ektefelle.personnummer,
                            Navn(
                                ektefelle.navn?.fornavn ?: "",
                                ektefelle.navn?.mellomnavn ?: "",
                                ektefelle.navn?.etternavn ?: "",
                            ),
                            ektefelle.fodselsdato,
                            familieFrontend.borSammenMed,
                        )
                    },
                )
            }
        runWithNestedTransaction {
            sivilstandController.updateSivilstand(UUID.fromString(soknadId), sivilstandInput)
        }.onFailure { logger.warn("Ny modell: Oppdatering av Sivilstand feilet", it) }
    }

    override fun updateForsorger(
        soknadId: String,
        forsorgerpliktFrontend: ForsorgerpliktFrontend,
    ) {
        logger.info("NyModell: Oppdaterer Forsorger.")

        val forsorgerInput =
            forsorgerpliktFrontend.run {
                ForsorgerInput(
                    barnebidrag?.name?.let { Barnebidrag.valueOf(it) },
                    ansvar.map { BarnInput(null, it.barn?.fodselsnummer, it.harDeltBosted) },
                )
            }
        runWithNestedTransaction {
            forsorgerpliktController.updateForsorgerplikt(UUID.fromString(soknadId), forsorgerInput)
        }.onFailure { logger.warn("Ny modell: Oppdatering av forsorgerplikt feilet", it) }
    }

    override fun updateAdresse(
        soknadId: String,
        adresser: AdresserFrontendInput,
    ) {
        adresser.valg?.let {
            runWithNestedTransaction {
                v2AdresseControllerAdapter.updateAdresse(soknadId = UUID.fromString(soknadId), it, adresser.soknad)
            }.onFailure { logger.warn("Ny modell: Oppdatering av adresser feilet.", it) }
        }
            ?: logger.warn("Ny modell: Oppdatering av adresser feilet. Adressevalg er null.")
    }

    override fun updateBoutgifter(
        behandlingsId: String,
        boutgifterFrontend: BoutgiftRessurs.BoutgifterFrontend,
    ) {
        if (boutgifterFrontend.bekreftelse == null) return

        val input =
            if (boutgifterFrontend.bekreftelse) {
                HarBoutgifterInput(
                    hasHusleie = boutgifterFrontend.husleie,
                    hasStrom = boutgifterFrontend.strom,
                    hasOppvarming = boutgifterFrontend.oppvarming,
                    hasKommunalAvgift = boutgifterFrontend.kommunalAvgift,
                    hasBoliglan = boutgifterFrontend.boliglan,
                    hasAnnenBoutgift = boutgifterFrontend.annet,
                )
            } else {
                HarIkkeBoutgifterInput()
            }

        runWithNestedTransaction {
            boutgiftController.updateBoutgifter(
                soknadId = UUID.fromString(behandlingsId),
                input = input,
            )
        }.onFailure { logger.warn("NyModell: Oppdatering av Boutgifter feilet", it) }
    }

    override fun updateBarneutgifter(
        behandlingsId: String,
        barneutgifterFrontend: BarneutgiftRessurs.BarneutgifterFrontend,
    ) {
        if (!barneutgifterFrontend.harForsorgerplikt) return
        if (barneutgifterFrontend.bekreftelse == null) return

        val input =
            barneutgifterFrontend.let {
                if (barneutgifterFrontend.bekreftelse) {
                    HarBarneutgifterInput(
                        hasFritidsaktiviteter = it.fritidsaktiviteter,
                        hasSfo = it.sfo,
                        hasTannregulering = it.tannregulering,
                        hasBarnehage = it.barnehage,
                        hasAnnenUtgiftBarn = it.annet,
                    )
                } else {
                    HarIkkeBarneutgifterInput()
                }
            }
        runWithNestedTransaction {
            barneutgiftController.updateBarneutgifter(
                soknadId = UUID.fromString(behandlingsId),
                input = input,
            )
        }.onFailure { logger.warn("NyModell: Oppdatering av Barneutgifter feilet", it) }
    }

    override fun updateSituasjonsendring(
        soknadId: String,
        situasjonsendring: SituasjonsendringFrontend,
    ) {
        runWithNestedTransaction {
            situasjonsendringController.updateSituasjonsendring(UUID.fromString(soknadId), SituasjonsendringDto(situasjonsendring.hvaErEndret, situasjonsendring.endring))
        }.onFailure { logger.warn("Ny modell: Oppdatering av situasjonsendring feilet.", it) }
    }

    override fun updateBostotteBekreftelse(
        soknadId: String,
        hasBostotte: Boolean?,
    ) {
        hasBostotte?.also {
            runWithNestedTransaction {
                bostotteController.updateHasBostotte(UUID.fromString(soknadId), BostotteInput(hasBostotte))
            }.onFailure { logger.warn("NyModell: Oppdatering av Bostotte feilet", it) }
        }
    }

    override fun updateBostotteSamtykke(
        soknadId: String,
        hasSamtykke: Boolean,
        userToken: String?,
    ) {
        runWithNestedTransaction {
            bostotteController.updateHasSamtykke(
                soknadId = UUID.fromString(soknadId),
                input = SamtykkeInput(hasSamtykke),
                token = userToken,
            )
        }.onFailure { logger.warn("NyModell: Oppdatering av samtykke Bostotte feilet", it) }
    }

    override fun updateSamtykkeSkatteetaten(
        behandlingsId: String,
        samtykke: Boolean,
    ) {
        runWithNestedTransaction {
            inntektSkattetatenController.updateSamtykke(
                soknadId = UUID.fromString(behandlingsId),
                samtykke = samtykke,
            )
        }.onFailure { logger.warn("NyModell: Oppdatering av samtykke Skatteetaten feilet", it) }
    }

    private fun runWithNestedTransaction(function: () -> Unit): Result<Unit> =
        kotlin.runCatching {
            transactionTemplate.propagationBehavior = TransactionDefinition.PROPAGATION_NESTED
            transactionTemplate.execute { function.invoke() }
        }
}
