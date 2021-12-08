//package no.nav.sosialhjelp.soknad.business;
//
//import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
//import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker;
//import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository;
//import no.nav.sosialhjelp.soknad.business.db.repositories.sendtsoknad.SendtSoknadRepository;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
//import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
//import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
//import no.nav.sosialhjelp.soknad.domain.SendtSoknad;
//import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
//import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus;
//import org.slf4j.Logger;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.TransactionStatus;
//import org.springframework.transaction.support.TransactionCallbackWithoutResult;
//import org.springframework.transaction.support.TransactionTemplate;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.apache.commons.lang3.StringUtils.isEmpty;
//import static org.slf4j.LoggerFactory.getLogger;
//
//@Component
//public class InnsendingService {
//    private static final Logger logger = getLogger(InnsendingService.class);
//    private final TransactionTemplate transactionTemplate;
//    private final SendtSoknadRepository sendtSoknadRepository;
//    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
//    private final OpplastetVedleggRepository opplastetVedleggRepository;
//    private final SoknadUnderArbeidService soknadUnderArbeidService;
//    private final SoknadMetadataRepository soknadMetadataRepository;
//
//    public InnsendingService(
//            TransactionTemplate transactionTemplate,
//            SendtSoknadRepository sendtSoknadRepository,
//            SoknadUnderArbeidRepository soknadUnderArbeidRepository,
//            OpplastetVedleggRepository opplastetVedleggRepository,
//            SoknadUnderArbeidService soknadUnderArbeidService,
//            SoknadMetadataRepository soknadMetadataRepository
//    ) {
//        this.transactionTemplate = transactionTemplate;
//        this.sendtSoknadRepository = sendtSoknadRepository;
//        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
//        this.opplastetVedleggRepository = opplastetVedleggRepository;
//        this.soknadUnderArbeidService = soknadUnderArbeidService;
//        this.soknadMetadataRepository = soknadMetadataRepository;
//    }
//
//    public void opprettSendtSoknad(SoknadUnderArbeid soknadUnderArbeid) {
//        if (soknadUnderArbeid == null || soknadUnderArbeid.getSoknadId() == null) {
//            throw new IllegalStateException("Kan ikke sende søknad som ikke finnes eller som mangler søknadsid");
//        }
//        soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(soknadUnderArbeid);
//        soknadUnderArbeid.setStatus(SoknadUnderArbeidStatus.LAAST);
//        soknadUnderArbeidRepository.oppdaterInnsendingStatus(soknadUnderArbeid, soknadUnderArbeid.getEier());
//
//        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
//            @Override
//            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
//                SendtSoknad sendtSoknad = mapSoknadUnderArbeidTilSendtSoknad(soknadUnderArbeid);
//                sendtSoknadRepository.opprettSendtSoknad(sendtSoknad, sendtSoknad.getEier());
//            }
//        });
//    }
//
//    public void finnOgSlettSoknadUnderArbeidVedSendingTilFiks(String behandlingsId, String eier) {
//        logger.debug("Henter søknad under arbeid for behandlingsid {} og eier {}", behandlingsId, eier);
//        Optional<SoknadUnderArbeid> soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknadOptional(behandlingsId, eier);
//        soknadUnderArbeid.ifPresent(soknadUnderArbeidFraDb -> soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeidFraDb, eier));
//    }
//
//    public void oppdaterSendtSoknadVedSendingTilFiks(String fiksforsendelseId, String behandlingsId, String eier) {
//        logger.debug("Oppdaterer sendt søknad for behandlingsid {} og eier {}", behandlingsId, eier);
//        sendtSoknadRepository.oppdaterSendtSoknadVedSendingTilFiks(fiksforsendelseId, behandlingsId, eier);
//    }
//
//    public SendtSoknad hentSendtSoknad(String behandlingsId, String eier) {
//        Optional<SendtSoknad> sendtSoknadOptional = sendtSoknadRepository.hentSendtSoknad(behandlingsId, eier);
//        if (!sendtSoknadOptional.isPresent()) {
//            throw new RuntimeException("Finner ikke sendt søknad med behandlingsId " + behandlingsId);
//        }
//        return sendtSoknadOptional.get();
//    }
//
//    public SoknadUnderArbeid hentSoknadUnderArbeid(String behandlingsId, String eier) {
//        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknadOptional(behandlingsId, eier);
//        if (!soknadUnderArbeidOptional.isPresent()) {
//            throw new RuntimeException("Finner ikke sendt søknad med behandlingsId " + behandlingsId);
//        }
//        return soknadUnderArbeidOptional.get();
//    }
//
//    public List<OpplastetVedlegg> hentAlleOpplastedeVedleggForSoknad(SoknadUnderArbeid soknadUnderArbeid) {
//        if (soknadUnderArbeid == null) {
//            throw new RuntimeException("Kan ikke hente vedlegg fordi søknad mangler");
//        }
//        return opplastetVedleggRepository.hentVedleggForSoknad(soknadUnderArbeid.getSoknadId(), soknadUnderArbeid.getEier());
//    }
//
//    public SendtSoknad finnSendtSoknadForEttersendelse(SoknadUnderArbeid soknadUnderArbeid) {
//        final String tilknyttetBehandlingsId = soknadUnderArbeid.getTilknyttetBehandlingsId();
//        Optional<SendtSoknad> sendtSoknad = sendtSoknadRepository.hentSendtSoknad(tilknyttetBehandlingsId,
//                soknadUnderArbeid.getEier());
//        if (sendtSoknad.isPresent()) {
//            return sendtSoknad.get();
//        } else {
//            final SendtSoknad konvertertGammelSoknad = finnSendtSoknadForEttersendelsePaGammeltFormat(tilknyttetBehandlingsId);
//            if (konvertertGammelSoknad == null) {
//                throw new IllegalStateException("Finner ikke søknaden det skal ettersendes på");
//            }
//            return konvertertGammelSoknad;
//        }
//    }
//
//    private SendtSoknad finnSendtSoknadForEttersendelsePaGammeltFormat(String tilknyttetBehandlingsId) {
//        SoknadMetadata originalSoknadGammeltFormat = soknadMetadataRepository.hent(tilknyttetBehandlingsId);
//        if (originalSoknadGammeltFormat == null) {
//            return null;
//        }
//        return new SendtSoknad()
//                .withOrgnummer(originalSoknadGammeltFormat.orgnr)
//                .withNavEnhetsnavn(originalSoknadGammeltFormat.navEnhet)
//                .withFiksforsendelseId(originalSoknadGammeltFormat.fiksForsendelseId);
//    }
//
//    SendtSoknad mapSoknadUnderArbeidTilSendtSoknad(SoknadUnderArbeid soknadUnderArbeid) {
//        JsonInternalSoknad internalSoknad = soknadUnderArbeid.getJsonInternalSoknad();
//        if (internalSoknad == null || internalSoknad.getMottaker() == null) {
//            throw new IllegalStateException("Søknadsmottaker mangler. internalSoknad eller mottaker er null");
//        }
//
//        String orgnummer = internalSoknad.getMottaker().getOrganisasjonsnummer();
//        String navEnhetsnavn = internalSoknad.getMottaker().getNavEnhetsnavn();
//        if (isEmpty(orgnummer) || isEmpty(navEnhetsnavn)) {
//
//            String soknadEnhetsnavn = "";
//            String soknadEnhetsnummer = "";
//            String soknadKommunenummer = "";
//            if (internalSoknad.getSoknad() != null && internalSoknad.getSoknad().getMottaker() != null) {
//                JsonSoknadsmottaker soknadsmottaker = internalSoknad.getSoknad().getMottaker();
//                soknadEnhetsnavn = soknadsmottaker.getNavEnhetsnavn();
//                soknadEnhetsnummer = soknadsmottaker.getEnhetsnummer();
//                soknadKommunenummer = soknadsmottaker.getKommunenummer();
//            }
//
//            throw new IllegalStateException(
//                    String.format("Søknadsmottaker mangler for behandlingsid %s. internal-orgnummer: %s, internal-navEnhetsnavn: %s. soknad-enhetsnavn: %s, soknad-enhetsnummer: %s, soknad-kommunenummer: %s. IsEttersendelse: %b",
//                            soknadUnderArbeid.getBehandlingsId(),
//                            orgnummer,
//                            navEnhetsnavn,
//                            soknadEnhetsnavn,
//                            soknadEnhetsnummer,
//                            soknadKommunenummer,
//                            soknadUnderArbeid.getTilknyttetBehandlingsId() != null
//                    ));
//        }
//
//        return new SendtSoknad()
//                .withBehandlingsId(soknadUnderArbeid.getBehandlingsId())
//                .withTilknyttetBehandlingsId(soknadUnderArbeid.getTilknyttetBehandlingsId())
//                .withOrgnummer(orgnummer)
//                .withNavEnhetsnavn(navEnhetsnavn)
//                .withEier(soknadUnderArbeid.getEier())
//                .withBrukerOpprettetDato(soknadUnderArbeid.getOpprettetDato())
//                .withBrukerFerdigDato(soknadUnderArbeid.getSistEndretDato());
//    }
//}
