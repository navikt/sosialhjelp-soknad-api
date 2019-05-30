package no.nav.sbl.sosialhjelp;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.sosialhjelp.domain.*;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.util.*;

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

    public void opprettSendtSoknad(SoknadUnderArbeid soknadUnderArbeid, List<Vedleggstatus> ikkeOpplastedePaakrevdeVedlegg) {
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
        Optional<SoknadUnderArbeid> soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
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
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
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
        JsonInternalSoknad internalSoknad = soknadUnderArbeid.getJsonInternalSoknad();
        if (internalSoknad == null || internalSoknad.getMottaker() == null) {
            throw new IllegalStateException("Søknadsmottaker mangler. internalSoknad eller mottaker er null");
        }

        String orgnummer = internalSoknad.getMottaker().getOrganisasjonsnummer();
        String navEnhetsnavn = internalSoknad.getMottaker().getNavEnhetsnavn();
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

    List<Vedleggstatus> mapOpplastedeVedleggTilVedleggstatusListe(List<OpplastetVedlegg> opplastedeVedlegg) {
        if (opplastedeVedlegg == null) {
            return null;
        } else if (opplastedeVedlegg.isEmpty()) {
            return new ArrayList<>();
        }
        List<Vedleggstatus> vedleggstatuser = new ArrayList<>();
        for (OpplastetVedlegg opplastetVedlegg : opplastedeVedlegg) {
            if (opplastetVedlegg != null) {
                vedleggstatuser.add(new Vedleggstatus()
                        .withVedleggType(opplastetVedlegg.getVedleggType())
                        .withEier(opplastetVedlegg.getEier())
                        .withStatus(Vedleggstatus.Status.LastetOpp));
            }
        }
        fjernDuplikateVedleggstatuser(vedleggstatuser);
        return vedleggstatuser;
    }

    List<Vedleggstatus> fjernDuplikateVedleggstatuser(List<Vedleggstatus> vedleggstatusForOpplastedeVedlegg) {
        Set<VedleggType> vedleggTyper = new HashSet<>();
        vedleggstatusForOpplastedeVedlegg.removeIf(vedleggstatus -> !vedleggTyper.add(vedleggstatus.getVedleggType()));
        return vedleggstatusForOpplastedeVedlegg;
    }
}
