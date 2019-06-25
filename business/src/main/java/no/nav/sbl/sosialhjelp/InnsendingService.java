package no.nav.sbl.sosialhjelp;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class InnsendingService {
    private static final Logger logger = getLogger(InnsendingService.class);
    @Inject
    private TransactionTemplate transactionTemplate;
    @Inject
    private SendtSoknadRepository sendtSoknadRepository;
    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    @Inject
    private OpplastetVedleggRepository opplastetVedleggRepository;
    @Inject
    private SoknadUnderArbeidService soknadUnderArbeidService;
    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;

    public void opprettSendtSoknad(SoknadUnderArbeid soknadUnderArbeid) {
        if (soknadUnderArbeid == null || soknadUnderArbeid.getSoknadId() == null) {
            throw new IllegalStateException("Kan ikke sende søknad som ikke finnes eller som mangler søknadsid");
        }
        soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(soknadUnderArbeid);
        soknadUnderArbeid.setInnsendingStatus(SoknadInnsendingStatus.LAAST);
        soknadUnderArbeidRepository.oppdaterInnsendingStatus(soknadUnderArbeid, soknadUnderArbeid.getEier());

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                SendtSoknad sendtSoknad = mapSoknadUnderArbeidTilSendtSoknad(soknadUnderArbeid);
                sendtSoknadRepository.opprettSendtSoknad(sendtSoknad, sendtSoknad.getEier());
            }
        });
    }

    public void finnOgSlettSoknadUnderArbeidVedSendingTilFiks(String behandlingsId, String eier) {
        logger.debug("Henter søknad under arbeid for behandlingsid {} og eier {}", behandlingsId, eier);
        Optional<SoknadUnderArbeid> soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknadOptional(behandlingsId, eier);
        soknadUnderArbeid.ifPresent(soknadUnderArbeidFraDb -> soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeidFraDb, eier));
    }

    public void oppdaterSendtSoknadVedSendingTilFiks(String fiksforsendelseId, String behandlingsId, String eier) {
        logger.debug("Oppdaterer sendt søknad for behandlingsid {} og eier {}", behandlingsId, eier);
        sendtSoknadRepository.oppdaterSendtSoknadVedSendingTilFiks(fiksforsendelseId, behandlingsId, eier);
    }

    public SendtSoknad hentSendtSoknad(String behandlingsId, String eier) {
        Optional<SendtSoknad> sendtSoknadOptional = sendtSoknadRepository.hentSendtSoknad(behandlingsId, eier);
        if (!sendtSoknadOptional.isPresent()) {
            throw new RuntimeException("Finner ikke sendt søknad med behandlingsId " + behandlingsId);
        }
        return sendtSoknadOptional.get();
    }

    public SoknadUnderArbeid hentSoknadUnderArbeid(String behandlingsId, String eier) {
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknadOptional(behandlingsId, eier);
        if (!soknadUnderArbeidOptional.isPresent()) {
            throw new RuntimeException("Finner ikke sendt søknad med behandlingsId " + behandlingsId);
        }
        return soknadUnderArbeidOptional.get();
    }

    public List<OpplastetVedlegg> hentAlleOpplastedeVedleggForSoknad(SoknadUnderArbeid soknadUnderArbeid) {
        if (soknadUnderArbeid == null) {
            throw new RuntimeException("Kan ikke hente vedlegg fordi søknad mangler");
        }
        return opplastetVedleggRepository.hentVedleggForSoknad(soknadUnderArbeid.getSoknadId(), soknadUnderArbeid.getEier());
    }

    public SendtSoknad finnSendtSoknadForEttersendelse(SoknadUnderArbeid soknadUnderArbeid) {
        final String tilknyttetBehandlingsId = soknadUnderArbeid.getTilknyttetBehandlingsId();
        Optional<SendtSoknad> sendtSoknad = sendtSoknadRepository.hentSendtSoknad(tilknyttetBehandlingsId,
                soknadUnderArbeid.getEier());
        if (sendtSoknad.isPresent()) {
            return sendtSoknad.get();
        } else {
            final SendtSoknad konvertertGammelSoknad = finnSendtSoknadForEttersendelsePaGammeltFormat(tilknyttetBehandlingsId);
            if (konvertertGammelSoknad == null) {
                throw new IllegalStateException("Finner ikke søknaden det skal ettersendes på");
            }
            return konvertertGammelSoknad;
        }
    }

    private SendtSoknad finnSendtSoknadForEttersendelsePaGammeltFormat(String tilknyttetBehandlingsId) {
        SoknadMetadata originalSoknadGammeltFormat = soknadMetadataRepository.hent(tilknyttetBehandlingsId);
        if (originalSoknadGammeltFormat == null) {
            return null;
        }
        return new SendtSoknad()
                .withOrgnummer(originalSoknadGammeltFormat.orgnr)
                .withNavEnhetsnavn(originalSoknadGammeltFormat.navEnhet)
                .withFiksforsendelseId(originalSoknadGammeltFormat.fiksForsendelseId);
    }

    SendtSoknad mapSoknadUnderArbeidTilSendtSoknad(SoknadUnderArbeid soknadUnderArbeid) {
        JsonSoknadsmottaker internalMottaker = soknadUnderArbeid.getJsonInternalSoknad().getMottaker();
        JsonSoknad soknad = soknadUnderArbeid.getJsonInternalSoknad().getSoknad();
        no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker mottaker = soknad == null ? null : soknad.getMottaker();
        if (internalMottaker == null && mottaker == null) {
            throw new IllegalStateException("Søknadsmottaker mangler.");
        }
        String orgnummer;
        String navEnhetsnavn;
        if (internalMottaker != null) {
            orgnummer = internalMottaker.getOrganisasjonsnummer();
            navEnhetsnavn = internalMottaker.getNavEnhetsnavn();
        } else {
            orgnummer = KommuneTilNavEnhetMapper.getOrganisasjonsnummer(mottaker.getEnhetsnummer());
            navEnhetsnavn = mottaker.getNavEnhetsnavn();
        }

        if (isEmpty(orgnummer) || isEmpty(navEnhetsnavn)) {
            throw new IllegalStateException("Søknadsmottaker mangler. orgnummer: " + orgnummer + ", navEnhetsnavn: " + navEnhetsnavn);
        }

        return new SendtSoknad()
                .withBehandlingsId(soknadUnderArbeid.getBehandlingsId())
                .withTilknyttetBehandlingsId(soknadUnderArbeid.getTilknyttetBehandlingsId())
                .withOrgnummer(orgnummer)
                .withNavEnhetsnavn(navEnhetsnavn)
                .withEier(soknadUnderArbeid.getEier())
                .withBrukerOpprettetDato(soknadUnderArbeid.getOpprettetDato())
                .withBrukerFerdigDato(soknadUnderArbeid.getSistEndretDato());
    }
}
