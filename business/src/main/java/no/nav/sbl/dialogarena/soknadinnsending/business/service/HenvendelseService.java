package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.*;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class HenvendelseService {

    private static final Logger logger = getLogger(HenvendelseService.class);

    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;

    public String startSoknad(String fnr) {
        logger.info("Starter søknad");

        SoknadMetadata meta = new SoknadMetadata();
        meta.id = soknadMetadataRepository.hentNesteId();
        meta.behandlingsId = lagBehandlingsId(meta.id);
        meta.fnr = fnr;
        meta.status = SoknadInnsendingStatus.UNDER_ARBEID;

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

    public String startEttersending(SendtSoknad ettersendesPaSoknad) {
        SoknadMetadata ettersendelse = new SoknadMetadata();
        ettersendelse.id = soknadMetadataRepository.hentNesteId();
        ettersendelse.behandlingsId = lagBehandlingsId(ettersendelse.id);
        ettersendelse.tilknyttetBehandlingsId = ettersendesPaSoknad.getBehandlingsId();
        ettersendelse.fnr = ettersendesPaSoknad.getEier();
        ettersendelse.status = UNDER_ARBEID;

        soknadMetadataRepository.opprett(ettersendelse);

        return ettersendelse.behandlingsId;
    }

    public void oppdaterMetadataVedAvslutningAvSoknad(String behandlingsId, SoknadMetadata.VedleggMetadataListe vedlegg, SoknadUnderArbeid soknadUnderArbeid) {
        SoknadMetadata meta = soknadMetadataRepository.hent(behandlingsId);

        meta.vedlegg = vedlegg;

        meta.status = FERDIG;
        soknadMetadataRepository.oppdater(meta);

        logger.info("Søknad avsluttet " + behandlingsId + ", " + vedlegg.vedleggListe.size());
    }

    public void avbrytSoknad(String behandlingsId, boolean avbruttAutomatisk) {
        SoknadMetadata metadata = soknadMetadataRepository.hent(behandlingsId);
        metadata.status = avbruttAutomatisk ? AVBRUTT_AUTOMATISK : AVBRUTT_AV_BRUKER;

        soknadMetadataRepository.oppdater(metadata);
    }


}