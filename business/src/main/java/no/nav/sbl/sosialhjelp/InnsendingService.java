package no.nav.sbl.sosialhjelp;

import no.nav.sbl.sosialhjelp.domain.*;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import no.nav.sbl.sosialhjelp.sendtsoknad.VedleggstatusRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.util.*;

import static java.util.stream.Collectors.toList;
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
    private VedleggstatusRepository vedleggstatusRepository;

    public void opprettSendtSoknad(SoknadUnderArbeid soknadUnderArbeid, List<Vedleggstatus> ikkeOpplastedePaakrevdeVedlegg, String orgnummer) {
        if (soknadUnderArbeid == null || soknadUnderArbeid.getSoknadId() == null) {
            throw new IllegalStateException("Kan ikke sende søknad som ikke finnes eller som mangler søknadsid");
        }
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                final List<Vedleggstatus> alleVedlegg = finnAlleVedlegg(soknadUnderArbeid, ikkeOpplastedePaakrevdeVedlegg);
                SendtSoknad sendtSoknad = mapSoknadUnderArbeidTilSendtSoknad(soknadUnderArbeid, orgnummer);
                final Long sendtSoknadId = sendtSoknadRepository.opprettSendtSoknad(sendtSoknad, sendtSoknad.getEier());
                sendtSoknad.setSendtSoknadId(sendtSoknadId);

                for (Vedleggstatus vedleggstatus : alleVedlegg) {
                    vedleggstatus.setSendtSoknadId(sendtSoknad.getSendtSoknadId());
                    vedleggstatusRepository.opprettVedlegg(vedleggstatus, sendtSoknad.getEier());
                }
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

    List<Vedleggstatus> finnAlleVedlegg(SoknadUnderArbeid soknadUnderArbeid, List<Vedleggstatus> ikkeOpplastedePaakrevdeVedlegg) {
        List<Vedleggstatus> opplastedeVedlegg = mapOpplastedeVedleggTilVedleggstatusListe(opplastetVedleggRepository
                .hentVedleggForSoknad(soknadUnderArbeid.getSoknadId(), soknadUnderArbeid.getEier()));
        List<Vedleggstatus> alleVedlegg = new ArrayList<>();
        if (opplastedeVedlegg != null && !opplastedeVedlegg.isEmpty()) {
            alleVedlegg.addAll(opplastedeVedlegg.stream()
                    .filter(Objects::nonNull)
                    .collect(toList()));
        }
        if (!soknadUnderArbeid.erEttersendelse() && ikkeOpplastedePaakrevdeVedlegg != null && !ikkeOpplastedePaakrevdeVedlegg.isEmpty()) {
            alleVedlegg.addAll(ikkeOpplastedePaakrevdeVedlegg.stream()
                    .filter(Objects::nonNull)
                    .collect(toList()));
        }
        return alleVedlegg;
    }

    SendtSoknad mapSoknadUnderArbeidTilSendtSoknad(SoknadUnderArbeid soknadUnderArbeid, String orgnummer) {
        if (isEmpty(orgnummer)) {
            if (soknadUnderArbeid.erEttersendelse()) {
                orgnummer = finnOrgnummerForEttersendelse(soknadUnderArbeid);
            } else {
                throw new IllegalStateException("Søknadsmottaker mangler");
            }
        }
        return new SendtSoknad()
                .withBehandlingsId(soknadUnderArbeid.getBehandlingsId())
                .withTilknyttetBehandlingsId(soknadUnderArbeid.getTilknyttetBehandlingsId())
                .withOrgnummer(orgnummer)
                .withEier(soknadUnderArbeid.getEier())
                .withBrukerOpprettetDato(soknadUnderArbeid.getOpprettetDato())
                .withBrukerFerdigDato(soknadUnderArbeid.getSistEndretDato());
    }

    private String finnOrgnummerForEttersendelse(SoknadUnderArbeid soknadUnderArbeid) {
        Optional<SendtSoknad> sendtSoknad = sendtSoknadRepository.hentSendtSoknad(soknadUnderArbeid.getTilknyttetBehandlingsId(),
                soknadUnderArbeid.getEier());
        if (sendtSoknad.isPresent()) {
            return sendtSoknad.get().getOrgnummer();
        } else {
            throw new IllegalStateException("Finner ikke søknaden det skal ettersendes på");
        }
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
        return vedleggstatuser;
    }
}
