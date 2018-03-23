package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.FiksMetadataTransformer;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.HovedskjemaMetadata;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.*;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class HenvendelseService {

    private static final Logger logger = getLogger(HenvendelseService.class);

    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;

    @Inject
    private OppgaveHandterer oppgaveHandterer;

    @Inject
    private Clock clock;

    public String startSoknad(String fnr, String skjema, String uid, SoknadType soknadType) {
        logger.info("Starter søknad");

        SoknadMetadata meta = new SoknadMetadata();
        meta.id = soknadMetadataRepository.hentNesteId();
        meta.behandlingsId = lagBehandlingsId(meta.id);
        meta.fnr = fnr;
        meta.type = soknadType;
        meta.skjema = skjema;
        meta.status = SoknadInnsendingStatus.UNDER_ARBEID;
        meta.opprettetDato = LocalDateTime.now(clock);
        meta.sistEndretDato = LocalDateTime.now(clock);

        meta.hovedskjema = new HovedskjemaMetadata();
        meta.hovedskjema.filUuid = uid;

        soknadMetadataRepository.opprett(meta);

        return meta.behandlingsId;
    }

    static String lagBehandlingsId(long databasenokkel) {
        String applikasjonsprefix = "11";
        Long base = Long.parseLong(applikasjonsprefix + "0000000", 36);
        String behandlingsId = Long.toString(base + databasenokkel, 36).toUpperCase().replace("O", "o").replace("I", "i");
        if (!behandlingsId.startsWith(applikasjonsprefix)) {
            throw new ApplicationException("Tildelt sekvensrom for behandlingsId er brukt opp. Kan ikke generer behandlingsId " + behandlingsId);
        }
        return behandlingsId;
    }

    public String startEttersending(Object soknadResponse) {
        throw new NotImplementedException("støtter ikke ettersending enda");
    }

    public void hentBehandlingskjede(String behandlingskjedeId) {
        throw new NotImplementedException("støtter ikke ettersendelser");
    }

    public void avsluttSoknad(String behandlingsId, HovedskjemaMetadata hovedskjema, SoknadMetadata.VedleggMetadataListe vedlegg, Map<String, String> ekstraMetadata) {
        SoknadMetadata meta = soknadMetadataRepository.hent(behandlingsId);

        meta.hovedskjema = hovedskjema;
        meta.vedlegg = vedlegg;
        meta.orgnr = ekstraMetadata.get(FiksMetadataTransformer.FIKS_ORGNR_KEY);
        meta.navEnhet = ekstraMetadata.get(FiksMetadataTransformer.FIKS_ENHET_KEY);
        meta.sistEndretDato = LocalDateTime.now(clock);
        meta.innsendtDato = LocalDateTime.now(clock);

        meta.status = FERDIG;
        soknadMetadataRepository.oppdater(meta);
        oppgaveHandterer.leggTilOppgave(behandlingsId);

        logger.info("Søknad avsluttet " + behandlingsId + " " + meta.skjema + ", " + vedlegg.vedleggListe.size());
    }

    public SoknadMetadata hentSoknad(String behandlingsId) {
        SoknadMetadata hentet = soknadMetadataRepository.hent(behandlingsId);
        hentet.sistEndretDato = LocalDateTime.now(clock);
        soknadMetadataRepository.oppdater(hentet);

        return hentet;
    }

    public void avbrytSoknad(String behandlingsId, boolean avbruttAutomatisk) {
        SoknadMetadata metadata = soknadMetadataRepository.hent(behandlingsId);
        metadata.status = avbruttAutomatisk ? AVBRUTT_AUTOMATISK : AVBRUTT_AV_BRUKER;
        metadata.sistEndretDato = LocalDateTime.now(clock);

        soknadMetadataRepository.oppdater(metadata);
    }


}