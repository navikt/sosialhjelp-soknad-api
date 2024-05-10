package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.arbeid.domain.toV2Arbeidsforhold
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.service.EierRegisterService
import no.nav.sosialhjelp.soknad.v2.familie.Barn
import no.nav.sosialhjelp.soknad.v2.familie.Ektefelle
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.familie.service.FamilieRegisterService
import no.nav.sosialhjelp.soknad.v2.kontakt.service.KontaktRegisterService
import no.nav.sosialhjelp.soknad.v2.livssituasjon.service.LivssituasjonRegisterService
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.register.handlers.person.toV2Adresse
import no.nav.sosialhjelp.soknad.v2.soknad.service.SoknadService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID

@Service
@Transactional(propagation = Propagation.NESTED)
class SoknadV2AdapterService(
    private val soknadService: SoknadService,
    private val livssituasjonService: LivssituasjonRegisterService,
    private val kontaktService: KontaktRegisterService,
    private val hentAdresseService: HentAdresseService,
    private val familieService: FamilieRegisterService,
    private val eierService: EierRegisterService,
) : V2AdapterService {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun createSoknad(
        behandlingsId: String,
        opprettetDato: LocalDateTime,
        eierId: String,
    ) {
        log.info("NyModell: Oppretter ny soknad for $behandlingsId")

        kotlin.runCatching {
            soknadService.createSoknad(
                soknadId = UUID.fromString(behandlingsId),
                opprettetDato = opprettetDato,
                eierId = eierId,
            )
        }
            .onFailure { log.warn("Ny modell: Feil ved oppretting av ny soknad i adapter", it) }
    }

    override fun addArbeidsforholdList(
        soknadId: String,
        arbeidsforhold: List<no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold>?,
    ) {
        arbeidsforhold?.let {
            log.info("NyModell: Legger til arbeidsforhold.")

            kotlin.runCatching {
                livssituasjonService.updateArbeidsforhold(
                    UUID.fromString(soknadId),
                    it.map { it.toV2Arbeidsforhold() },
                )
            }
                .onFailure { log.warn("Ny modell: Kunne ikke legge til arbeidsforhold", it) }
        }
    }

    override fun addAdresserRegister(
        soknadId: String,
        person: Person?,
    ) {
        log.info("NyModell: Legger til adresser.")

        person?.let {
            kotlin.runCatching {
                kontaktService.saveAdresserRegister(
                    soknadId = UUID.fromString(soknadId),
                    folkeregistrert = it.bostedsadresse?.toV2Adresse(hentAdresseService),
                    midlertidig = it.oppholdsadresse?.toV2Adresse(),
                )
            }
                .onFailure { log.warn("NyModell: Legge til Adresser feilet.", it) }
        }
    }

    override fun updateTelefonRegister(
        soknadId: String,
        telefonnummer: String?,
    ) {
        log.info("NyModell: Legger til Telefonnummer.")

        telefonnummer?.let {
            kotlin.runCatching {
                kontaktService.updateTelefonRegister(UUID.fromString(soknadId), it)
            }
                .onFailure { log.warn("Kunne ikke legge til telefonnummer fra register", it) }
        }
    }

    override fun updateEier(
        soknadId: String,
        personalia: JsonPersonalia,
    ) {
        log.info("Ny modell: Legger til Eier")
        kotlin.runCatching {
            eierService.updateFromRegister(personalia.toV2Eier(UUID.fromString(soknadId)))
        }
            .onFailure { log.warn("NyModell: Kunne ikke legge til ny Eier fra register", it) }
    }

    override fun setInnsendingstidspunkt(
        soknadId: String,
        innsendingsTidspunkt: String,
    ) {
        log.info("NyModell: Setter innsendingstidspunkt")

        kotlin.runCatching {
            val zonedDateTime = ZonedDateTime.parse(innsendingsTidspunkt)

            soknadService.setInnsendingstidspunkt(
                UUID.fromString(soknadId),
                zonedDateTime.toLocalDateTime(),
            )
        }
            .onFailure { log.warn("NyModell: Kunne ikke sette innsendingstidspunkt", it) }
    }

    override fun slettSoknad(behandlingsId: String) {
        log.info("NyModell: Sletter SoknadV2")

        kotlin.runCatching {
            soknadService.slettSoknad(UUID.fromString(behandlingsId))
        }
            .onFailure { log.warn("NyModell: Kunne ikke slette Soknad V2") }
    }

    override fun addEktefelle(
        behandlingsId: String,
        systemverdiSivilstatus: JsonSivilstatus,
    ) {
        log.info("NyModell: Legger til systemdata for ektefelle")

        systemverdiSivilstatus.let {
            kotlin.runCatching {
                familieService.updateSivilstatusFraRegister(UUID.fromString(behandlingsId), it.status.toV2Sivilstatus(), it.toV2Ektefelle())
            }
                .onFailure { log.warn("NyModell: Kunne ikke legge til ektefelle for søknad:  $behandlingsId", it) }
        }
    }

    override fun addBarn(
        behandlingsId: String,
        ansvarList: List<JsonAnsvar>,
    ) {
        log.info("NyModell: Legger til systemdata for barn")
        ansvarList.let { jsonAnsvarListe ->
            kotlin.runCatching {
                if (jsonAnsvarListe.isNotEmpty()) {
                    familieService.updateForsorgerpliktRegister(
                        UUID.fromString(behandlingsId),
                        true,
                        jsonAnsvarListe.map { it.toV2Barn() },
                    )
                }
            }
                .onFailure { log.warn("NyModell: Kunne ikke legge til barn for søknad", it) }
        }
    }

    override fun saveKontonummer(
        behandlingsId: String,
        kontonummer: String?,
    ) {
        kontonummer?.let {
            kotlin.runCatching {
                eierService.updateKontonummerFraRegister(behandlingsId, it)
            }
                .onFailure { log.warn("NyModell: Kunne ikke legge til kontonummer for søknad.", it) }
        }
    }
}

private fun JsonPersonalia.toV2Eier(soknadId: UUID): Eier {
    return Eier(
        soknadId = soknadId,
        navn =
            Navn(
                fornavn = this.navn.fornavn,
                mellomnavn = this.navn.mellomnavn,
                etternavn = this.navn.etternavn,
            ),
        statsborgerskap = this.statsborgerskap.verdi,
        nordiskBorger = this.nordiskBorger.verdi,
    )
}

private fun JsonSivilstatus.toV2Ektefelle(): Ektefelle {
    return Ektefelle(
        navn =
            Navn(
                fornavn = ektefelle.navn.fornavn,
                mellomnavn = ektefelle.navn.mellomnavn,
                etternavn = ektefelle.navn.etternavn,
            ),
        fodselsdato = ektefelle.fodselsdato,
        personId = ektefelle.personIdentifikator,
        folkeregistrertMedEktefelle = folkeregistrertMedEktefelle,
        borSammen = borSammenMed,
        kildeErSystem = true,
    )
}

private fun Status.toV2Sivilstatus(): Sivilstatus {
    return when (this) {
        Status.GIFT -> Sivilstatus.GIFT
        Status.SAMBOER -> Sivilstatus.SAMBOER
        Status.ENKE -> Sivilstatus.ENKE
        Status.SKILT -> Sivilstatus.SKILT
        Status.SEPARERT -> Sivilstatus.SEPARERT
        Status.UGIFT -> Sivilstatus.UGIFT
    }
}

private fun JsonAnsvar.toV2Barn(): Barn {
    return Barn(
        familieKey = UUID.randomUUID(),
        personId = barn.personIdentifikator,
        navn =
            Navn(
                fornavn = barn.navn.fornavn,
                mellomnavn = barn.navn.mellomnavn,
                etternavn = barn.navn.etternavn,
            ),
        fodselsdato = barn.fodselsdato,
        borSammen = borSammenMed?.verdi,
        deltBosted = harDeltBosted?.verdi,
        folkeregistrertSammen = erFolkeregistrertSammen.verdi,
    )
}
