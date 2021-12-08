package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.business.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.sendtsoknad.SendtSoknadRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.domain.SendtSoknad
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate

open class InnsendingService(
    private val transactionTemplate: TransactionTemplate,
    private val sendtSoknadRepository: SendtSoknadRepository,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val opplastetVedleggRepository: OpplastetVedleggRepository,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val soknadMetadataRepository: SoknadMetadataRepository
) {
    open fun opprettSendtSoknad(soknadUnderArbeid: SoknadUnderArbeid?) {
        check(!(soknadUnderArbeid == null || soknadUnderArbeid.soknadId == null)) { "Kan ikke sende søknad som ikke finnes eller som mangler søknadsid" }
        soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(soknadUnderArbeid)
        soknadUnderArbeid.status = SoknadUnderArbeidStatus.LAAST
        soknadUnderArbeidRepository.oppdaterInnsendingStatus(soknadUnderArbeid, soknadUnderArbeid.eier)
        transactionTemplate.execute(object : TransactionCallbackWithoutResult() {
            override fun doInTransactionWithoutResult(transactionStatus: TransactionStatus) {
                val sendtSoknad = mapSoknadUnderArbeidTilSendtSoknad(soknadUnderArbeid)
                sendtSoknadRepository.opprettSendtSoknad(sendtSoknad, sendtSoknad.eier)
            }
        })
    }

    open fun finnOgSlettSoknadUnderArbeidVedSendingTilFiks(behandlingsId: String?, eier: String?) {
        logger.debug("Henter søknad under arbeid for behandlingsid {} og eier {}", behandlingsId, eier)
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknadOptional(behandlingsId, eier)
        soknadUnderArbeid.ifPresent { soknadUnderArbeidRepository.slettSoknad(it, eier) }
    }

    open fun oppdaterSendtSoknadVedSendingTilFiks(fiksforsendelseId: String?, behandlingsId: String?, eier: String?) {
        logger.debug("Oppdaterer sendt søknad for behandlingsid {} og eier {}", behandlingsId, eier)
        sendtSoknadRepository.oppdaterSendtSoknadVedSendingTilFiks(fiksforsendelseId, behandlingsId, eier)
    }

    open fun hentSendtSoknad(behandlingsId: String, eier: String?): SendtSoknad {
        val sendtSoknadOptional = sendtSoknadRepository.hentSendtSoknad(behandlingsId, eier)
        if (!sendtSoknadOptional.isPresent) {
            throw RuntimeException("Finner ikke sendt søknad med behandlingsId $behandlingsId")
        }
        return sendtSoknadOptional.get()
    }

    open fun hentSoknadUnderArbeid(behandlingsId: String, eier: String?): SoknadUnderArbeid {
        val soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknadOptional(behandlingsId, eier)
        if (!soknadUnderArbeidOptional.isPresent) {
            throw RuntimeException("Finner ikke sendt søknad med behandlingsId $behandlingsId")
        }
        return soknadUnderArbeidOptional.get()
    }

    open fun hentAlleOpplastedeVedleggForSoknad(soknadUnderArbeid: SoknadUnderArbeid?): List<OpplastetVedlegg> {
        if (soknadUnderArbeid == null) {
            throw RuntimeException("Kan ikke hente vedlegg fordi søknad mangler")
        }
        return opplastetVedleggRepository.hentVedleggForSoknad(soknadUnderArbeid.soknadId, soknadUnderArbeid.eier)
    }

    open fun finnSendtSoknadForEttersendelse(soknadUnderArbeid: SoknadUnderArbeid): SendtSoknad {
        val tilknyttetBehandlingsId = soknadUnderArbeid.tilknyttetBehandlingsId
        val sendtSoknad = sendtSoknadRepository.hentSendtSoknad(tilknyttetBehandlingsId, soknadUnderArbeid.eier)
        return if (sendtSoknad.isPresent) {
            sendtSoknad.get()
        } else {
            val konvertertGammelSoknad = finnSendtSoknadForEttersendelsePaGammeltFormat(tilknyttetBehandlingsId)
                ?: throw IllegalStateException("Finner ikke søknaden det skal ettersendes på")
            konvertertGammelSoknad
        }
    }

    private fun finnSendtSoknadForEttersendelsePaGammeltFormat(tilknyttetBehandlingsId: String): SendtSoknad? {
        val originalSoknadGammeltFormat = soknadMetadataRepository.hent(tilknyttetBehandlingsId) ?: return null
        return SendtSoknad().withOrgnummer(originalSoknadGammeltFormat.orgnr)
            .withNavEnhetsnavn(originalSoknadGammeltFormat.navEnhet)
            .withFiksforsendelseId(originalSoknadGammeltFormat.fiksForsendelseId)
    }

    fun mapSoknadUnderArbeidTilSendtSoknad(soknadUnderArbeid: SoknadUnderArbeid): SendtSoknad {
        val internalSoknad = soknadUnderArbeid.jsonInternalSoknad
        check(!(internalSoknad == null || internalSoknad.mottaker == null)) { "Søknadsmottaker mangler. internalSoknad eller mottaker er null" }
        val orgnummer = internalSoknad.mottaker.organisasjonsnummer
        val navEnhetsnavn = internalSoknad.mottaker.navEnhetsnavn
        if (StringUtils.isEmpty(orgnummer) || StringUtils.isEmpty(navEnhetsnavn)) {
            var soknadEnhetsnavn = ""
            var soknadEnhetsnummer = ""
            var soknadKommunenummer = ""
            if (internalSoknad.soknad != null && internalSoknad.soknad.mottaker != null) {
                val soknadsmottaker = internalSoknad.soknad.mottaker
                soknadEnhetsnavn = soknadsmottaker.navEnhetsnavn
                soknadEnhetsnummer = soknadsmottaker.enhetsnummer
                soknadKommunenummer = soknadsmottaker.kommunenummer
            }
            throw IllegalStateException(
                "Søknadsmottaker mangler for behandlingsid ${soknadUnderArbeid.behandlingsId}. " +
                    "internal-orgnummer: $orgnummer, internal-navEnhetsnavn: $navEnhetsnavn. " +
                    "soknad-enhetsnavn: $soknadEnhetsnavn, soknad-enhetsnummer: $soknadEnhetsnummer, " +
                    "soknad-kommunenummer: $soknadKommunenummer. IsEttersendelse: ${soknadUnderArbeid.tilknyttetBehandlingsId != null}"
            )
        }
        return SendtSoknad().withBehandlingsId(soknadUnderArbeid.behandlingsId)
            .withTilknyttetBehandlingsId(soknadUnderArbeid.tilknyttetBehandlingsId).withOrgnummer(orgnummer)
            .withNavEnhetsnavn(navEnhetsnavn).withEier(soknadUnderArbeid.eier)
            .withBrukerOpprettetDato(soknadUnderArbeid.opprettetDato)
            .withBrukerFerdigDato(soknadUnderArbeid.sistEndretDato)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(InnsendingService::class.java)
    }
}
