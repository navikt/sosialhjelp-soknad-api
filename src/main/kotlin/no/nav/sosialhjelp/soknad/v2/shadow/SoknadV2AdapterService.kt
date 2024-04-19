package no.nav.sosialhjelp.soknad.v2.shadow

import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.arbeid.domain.toV2Arbeidsforhold
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.EierService
import no.nav.sosialhjelp.soknad.v2.familie.Barn
import no.nav.sosialhjelp.soknad.v2.familie.Ektefelle
import no.nav.sosialhjelp.soknad.v2.familie.FamilieService
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktService
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonService
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.shadow.adapter.V2AdresseAdapter.toV2Adresse
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional(propagation = Propagation.NESTED)
class SoknadV2AdapterService(
    private val soknadService: SoknadService,
    private val livssituasjonService: LivssituasjonService,
    private val kontaktService: KontaktService,
    private val hentAdresseService: HentAdresseService,
    private val eierService: EierService,
    private val familieService: FamilieService,
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
            .onFailure { log.error("Ny modell: Feil ved oppretting av ny soknad i adapter", it) }
    }

    override fun addArbeidsforholdList(
        soknadId: String,
        arbeidsforhold: List<no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold>?,
    ) {
        arbeidsforhold?.let {
            log.info("NyModell: Legger til arbeidsforhold for $soknadId")

            kotlin.runCatching {
                livssituasjonService.updateArbeidsforhold(
                    UUID.fromString(soknadId),
                    it.map { it.toV2Arbeidsforhold() },
                )
            }
                .onFailure { log.error("Ny modell: Kunne ikke legge til arbeidsforhold", it) }
        }
    }

    override fun addAdresserRegister(
        soknadId: String,
        person: Person?,
    ) {
        log.info("NyModell: Legger til adresser for $soknadId")

        person?.let {
            kotlin.runCatching {
                kontaktService.saveAdresserRegister(
                    soknadId = UUID.fromString(soknadId),
                    folkeregistrertAdresse = it.bostedsadresse?.toV2Adresse(hentAdresseService),
                    midlertidigAdresse = it.oppholdsadresse?.toV2Adresse(),
                )
            }
                .onFailure { log.error("NyModell: Legge til Adresser feilet for $soknadId", it) }
        }
    }

    override fun updateTelefonRegister(
        soknadId: String,
        telefonnummer: String?,
    ) {
        log.info("NyModell: Legger til Telefonnummer for $soknadId")

        telefonnummer?.let {
            kotlin.runCatching {
                kontaktService.updateTelefonRegister(UUID.fromString(soknadId), it)
            }
                .onFailure { log.error("Kunne ikke legge til telefonnummer fra register", it) }
        }
    }

    override fun updateEier(
        soknadId: String,
        personalia: JsonPersonalia,
    ) {
        log.info("Ny modell: Legger til Eier")
        kotlin.runCatching {
            eierService.updateEier(personalia.toV2Eier(UUID.fromString(soknadId)))
        }
            .onFailure { log.error("NyModell: Kunne ikke legge til ny Eier fra register", it) }
    }

    override fun setInnsendingstidspunkt(
        soknadId: String,
        innsendingsTidspunkt: LocalDateTime,
    ) {
        log.info("NyModell: Setter innsendingstidspunkt")

        kotlin.runCatching {
            soknadService.setInnsendingstidspunkt(
                UUID.fromString(soknadId),
                innsendingsTidspunkt,
            )
        }
            .onFailure { log.error("NyModell: Kunne ikke sette innsendingstidspunkt", it) }
    }

    override fun slettSoknad(behandlingsId: String) {
        log.info("NyModell: Sletter SoknadV2")

        kotlin.runCatching {
            soknadService.slettSoknad(UUID.fromString(behandlingsId))
        }
            .onFailure { log.error("NyModell: Kunne ikke slette Soknad V2") }
    }

    override fun addEktefelle(
        behandlingsId: String,
        systemverdiSivilstatus: JsonSivilstatus?,
    ) {
        log.info("NyModell: Legger til systemdata for ektefelle")

        systemverdiSivilstatus?.let {
            kotlin.runCatching {
                familieService.addEktefelle(UUID.fromString(behandlingsId), it.toV2Ektefelle())
            }
                .onFailure { log.error("NyModell: Kunne ikke legge til ektefelle for søknad:  $behandlingsId", it) }
        }
    }

    override fun addBarn(behandlingsId: String, ansvarList: MutableList<JsonAnsvar>) {
        log.info("NyModell: Legger til systemdata for barn")
        ansvarList.let {
            kotlin.runCatching {
                familieService.addBarn(UUID.fromString(behandlingsId), it.map { it.toV2Barn() })
            }
                .onFailure { log.error("NyModell: Kunne ikke legge til barn for søknad:  $behandlingsId", it) }
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
            fornavn = this.ektefelle.navn.fornavn,
            mellomnavn = this.ektefelle.navn.mellomnavn,
            etternavn = this.ektefelle.navn.etternavn,
        ),
        fodselsdato = this.ektefelle.fodselsdato,
        personId = this.ektefelle.personIdentifikator,
        folkeregistrertMedEktefelle = this.folkeregistrertMedEktefelle,
        borSammen = this.borSammenMed,
        kildeErSystem = true,
    )
}

private fun JsonAnsvar.toV2Barn(): Barn {
    return Barn(
//        TODO hvor skal familieKey opprettes.
        familieKey = UUID.randomUUID(),
        personId = this.barn.personIdentifikator,
        navn =
        Navn(
            fornavn = this.barn.navn.fornavn,
            mellomnavn = this.barn.navn.mellomnavn,
            etternavn = this.barn.navn.etternavn,
        ),
        fodselsdato = this.barn.fodselsdato,
        borSammen = this.borSammenMed?.verdi,
        folkeregistrertSammen = this.erFolkeregistrertSammen.verdi
    )
}
