package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknad
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknadRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime

open class InnsendingService(
    private val transactionTemplate: TransactionTemplate,
    private val sendtSoknadRepository: SendtSoknadRepository,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val opplastetVedleggRepository: OpplastetVedleggRepository,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val soknadMetadataRepository: SoknadMetadataRepository
) {
    open fun opprettSendtSoknad(soknadUnderArbeid: SoknadUnderArbeid?) {
        check(soknadUnderArbeid != null) { "Kan ikke sende søknad som ikke finnes eller som mangler søknadsid" }
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

    open fun finnOgSlettSoknadUnderArbeidVedSendingTilFiks(behandlingsId: String, eier: String) {
        logger.debug("Henter søknad under arbeid for behandlingsid $behandlingsId og eier $eier")
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknadOptional(behandlingsId, eier)
        soknadUnderArbeid.ifPresent { soknadUnderArbeidRepository.slettSoknad(it, eier) }
    }

    open fun oppdaterSendtSoknadVedSendingTilFiks(fiksforsendelseId: String?, behandlingsId: String?, eier: String?) {
        logger.debug("Oppdaterer sendt søknad for behandlingsid $behandlingsId og eier $eier")
        sendtSoknadRepository.oppdaterSendtSoknadVedSendingTilFiks(fiksforsendelseId, behandlingsId, eier)
    }

    open fun hentSendtSoknad(behandlingsId: String, eier: String?): SendtSoknad {
        val sendtSoknadOptional = sendtSoknadRepository.hentSendtSoknad(behandlingsId, eier)
        if (!sendtSoknadOptional.isPresent) {
            throw RuntimeException("Finner ikke sendt søknad med behandlingsId $behandlingsId")
        }
        return sendtSoknadOptional.get()
    }

    open fun hentSoknadUnderArbeid(behandlingsId: String, eier: String): SoknadUnderArbeid {
        val soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknadOptional(behandlingsId, eier)
        if (!soknadUnderArbeidOptional.isPresent) {
            throw RuntimeException("Finner ikke sendt søknad med behandlingsId $behandlingsId")
        }
        return soknadUnderArbeidOptional.get()
    }

    open fun hentAlleOpplastedeVedleggForSoknad(soknadUnderArbeid: SoknadUnderArbeid): List<OpplastetVedlegg> {
        return opplastetVedleggRepository.hentVedleggForSoknad(soknadUnderArbeid.soknadId, soknadUnderArbeid.eier)
    }

    open fun finnSendtSoknadForEttersendelse(soknadUnderArbeid: SoknadUnderArbeid): SendtSoknad {
        val tilknyttetBehandlingsId = soknadUnderArbeid.tilknyttetBehandlingsId
            ?: throw IllegalStateException("TilknyttetBehandlingsId kan ikke være null for en ettersendelse")

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
        val orgnr = originalSoknadGammeltFormat.orgnr ?: return null
        val navEnhet = originalSoknadGammeltFormat.navEnhet ?: return null
        return SendtSoknad(
            sendtSoknadId = 0L, // dummy id. SendtSoknadRepository.opprettSendtSoknad bruker next sequence value som id
            behandlingsId = "",
            tilknyttetBehandlingsId = null,
            eier = "",
            fiksforsendelseId = originalSoknadGammeltFormat.fiksForsendelseId,
            orgnummer = orgnr,
            navEnhetsnavn = navEnhet,
            brukerOpprettetDato = LocalDateTime.now(),
            brukerFerdigDato = LocalDateTime.now(),
            sendtDato = LocalDateTime.now()
        )
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
        return SendtSoknad(
            sendtSoknadId = 0L, // dummy id. SendtSoknadRepository.opprettSendtSoknad bruker next sequence value som id
            behandlingsId = soknadUnderArbeid.behandlingsId,
            tilknyttetBehandlingsId = soknadUnderArbeid.tilknyttetBehandlingsId,
            eier = soknadUnderArbeid.eier,
            fiksforsendelseId = null,
            orgnummer = orgnummer,
            navEnhetsnavn = navEnhetsnavn,
            brukerOpprettetDato = soknadUnderArbeid.opprettetDato,
            brukerFerdigDato = soknadUnderArbeid.sistEndretDato,
            sendtDato = null
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(InnsendingService::class.java)
    }
}
