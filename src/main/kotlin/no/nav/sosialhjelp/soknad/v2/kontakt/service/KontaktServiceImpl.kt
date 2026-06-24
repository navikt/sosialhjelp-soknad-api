package no.nav.sosialhjelp.soknad.v2.kontakt.service

import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import no.nav.sosialhjelp.soknad.v2.kontakt.VegAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.service.KontaktServiceImpl.Companion.MAX_ANTALL_KOMMUNER
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

interface AdresseService {
    fun findAdresser(soknadId: UUID): Adresser

    fun findMottaker(soknadId: UUID): NavEnhet?

    fun updateAdresse(
        soknadId: UUID,
        adresseValg: AdresseValg,
        brukerAdresse: Adresse?,
        mottaker: NavEnhet?,
    )

    fun updateKommunenavnMottaker(
        soknadId: UUID,
        kommunenavn: String,
    ): String?

    fun validateMottaker(valgtAdresse: Adresse)
}

interface TelefonService {
    fun findTelefonInfo(soknadId: UUID): Telefonnummer?

    fun updateTelefonnummer(
        soknadId: UUID,
        telefonnummerBruker: String?,
    ): Telefonnummer
}

@Service
class KontaktServiceImpl(
    private val kontaktRepository: KontaktRepository,
    private val metadataService: SoknadMetadataService,
) : AdresseService, TelefonService {
    @Transactional(readOnly = true)
    override fun findTelefonInfo(soknadId: UUID) = kontaktRepository.findByIdOrNull(soknadId)?.telefonnummer

    @Transactional
    override fun updateTelefonnummer(
        soknadId: UUID,
        telefonnummerBruker: String?,
    ): Telefonnummer =
        findOrCreate(soknadId)
            .run { copy(telefonnummer = telefonnummer.copy(fraBruker = telefonnummerBruker)) }
            .let { kontaktRepository.save(it) }
            .telefonnummer

    @Transactional
    override fun findAdresser(soknadId: UUID) = findOrCreate(soknadId).adresser

    @Transactional
    override fun findMottaker(soknadId: UUID): NavEnhet? {
        return kontaktRepository.findByIdOrNull(soknadId)?.mottaker
    }

    @Transactional
    override fun updateAdresse(
        soknadId: UUID,
        adresseValg: AdresseValg,
        brukerAdresse: Adresse?,
        mottaker: NavEnhet?,
    ) {
        findOrCreate(soknadId)
            .run {
                copy(
                    adresser =
                        adresser.copy(
                            adressevalg = adresseValg,
                            fraBruker = brukerAdresse,
                        ),
                    mottaker = mottaker,
                )
            }
            .also { kontaktRepository.save(it) }
    }

    override fun updateKommunenavnMottaker(
        soknadId: UUID,
        kommunenavn: String,
    ): String {
        return kontaktRepository.findByIdOrNull(soknadId)
            ?.run { copy(mottaker = mottaker?.copy(kommunenavn = kommunenavn)) }
            ?.let { kontaktRepository.save(it) }
            ?.mottaker?.kommunenavn
            ?: error("Kunne ikke oppdatere mottakers kommunenavn")
    }

    override fun validateMottaker(valgtAdresse: Adresse) {
        // TODO Midlertidig for test
        if (valgtAdresse.getKommunenummer() == "0301") {
            throw ForMangeMottakereException(
                message = "For mange mottakere",
                info =
                    ForMangeMottakereInfo(
                        innsendingGyldigFra = nowWithMillis(),
                        antallMottakere = MAX_ANTALL_KOMMUNER,
                        maksAntallMotatkere = MAX_ANTALL_KOMMUNER,
                    ),
            )
        }

        metadataService.findMetadataForPersonSendtInnAfter(
            personId = getUserIdFromToken(),
            date = nowWithMillis().minusDays(ANTALL_DAGER_BEGRENSET),
        )
            .also { metadatas ->
                metadatas.numberOfMottakere()
                    .also { numberOfMottakere ->


                        // større enn er pga. bakover-kompatibilitet
                        if (numberOfMottakere >= MAX_ANTALL_KOMMUNER) {
                            // Sjekk om gjeldende kommunenummer finnes i listen
                        }



                        if (numberOfMottakere >= MAX_ANTALL_KOMMUNER) {
                            throw ForMangeMottakereException(
                                message =
                                    "Du har sendt soknad til $numberOfMottakere forskjellige kommuner de siste $ANTALL_DAGER_BEGRENSET dagene. " +
                                        "Maks antall kommuner innenfor $ANTALL_DAGER_BEGRENSET dager er $MAX_ANTALL_KOMMUNER.",
                                info =
                                    ForMangeMottakereInfo(
                                        innsendingGyldigFra = metadatas.getInnsendingGyldigIfra(),
                                        antallMottakere = numberOfMottakere,
                                        maksAntallMotatkere = MAX_ANTALL_KOMMUNER,
                                    ),
                            )
                        }
                    }
            }
    }

    private fun findOrCreate(soknadId: UUID) =
        kontaktRepository.findByIdOrNull(soknadId)
            ?: kontaktRepository.save(Kontakt(soknadId))

    companion object {
        // bruker skal kun få lov til å søke i x kommuner innenfor n dager
        const val ANTALL_DAGER_BEGRENSET = 5L
        const val MAX_ANTALL_KOMMUNER = 2
    }
}

private fun List<SoknadMetadata>.getInnsendingGyldigIfra(): LocalDateTime = mapNotNull { it.tidspunkt.sendtInn }.sortedByDescending { it }[MAX_ANTALL_KOMMUNER - 1]

private fun List<SoknadMetadata>.numberOfMottakere(): Int = distinctBy { it.mottakerKommunenummer }.size

private fun Adresse.getKommunenummer() =
    when (this) {
        is VegAdresse -> this.kommunenummer
        is MatrikkelAdresse -> this.kommunenummer
        else -> error("Feil adresse type: ${this::class.simpleName}")
    }

data class ForMangeMottakereException(
    override val message: String,
    val info: ForMangeMottakereInfo,
) : SosialhjelpSoknadApiException(message)

data class ForMangeMottakereInfo(
    val innsendingGyldigFra: LocalDateTime,
    val antallMottakere: Int,
    val maksAntallMotatkere: Int,
)
