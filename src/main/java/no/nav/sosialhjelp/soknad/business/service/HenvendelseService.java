package no.nav.sosialhjelp.soknad.business.service;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations;
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.SosialhjelpSoknadApiException;
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.putToMDC;
import static no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK;
import static no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.AVBRUTT_AV_BRUKER;
import static no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.FERDIG;
import static no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API;
import static no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.UNDER_ARBEID;
import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SosialhjelpInformasjon.SKJEMANUMMER;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class HenvendelseService {

    private static final Logger logger = getLogger(HenvendelseService.class);

    private final SoknadMetadataRepository soknadMetadataRepository;
    private final Clock clock;

    public HenvendelseService(SoknadMetadataRepository soknadMetadataRepository, Clock clock) {
        this.soknadMetadataRepository = soknadMetadataRepository;
        this.clock = clock;
    }

    public String startSoknad(String fnr) {
        logger.info("Starter søknad");

        SoknadMetadata meta = new SoknadMetadata();
        meta.id = soknadMetadataRepository.hentNesteId();
        meta.behandlingsId = lagBehandlingsId(meta.id);
        meta.fnr = fnr;
        meta.type = SoknadType.SEND_SOKNAD_KOMMUNAL;
        meta.skjema = SKJEMANUMMER;
        meta.status = SoknadMetadataInnsendingStatus.UNDER_ARBEID;
        meta.opprettetDato = LocalDateTime.now(clock);
        meta.sistEndretDato = LocalDateTime.now(clock);

        soknadMetadataRepository.opprett(meta);

        return meta.behandlingsId;
    }

    static String lagBehandlingsId(long databasenokkel) {
        String applikasjonsprefix = "11";
        long base = Long.parseLong(applikasjonsprefix + "0000000", 36);
        String behandlingsId = Long.toString(base + databasenokkel, 36).toUpperCase().replace("O", "o").replace("I", "i");
        if (!behandlingsId.startsWith(applikasjonsprefix)) {
            throw new SosialhjelpSoknadApiException("Tildelt sekvensrom for behandlingsId er brukt opp. Kan ikke generer behandlingsId " + behandlingsId);
        }
        putToMDC(MDCOperations.MDC_BEHANDLINGS_ID, behandlingsId);
        return behandlingsId;
    }

    public String startEttersending(SoknadMetadata ettersendesPaSoknad) {
        SoknadMetadata ettersendelse = new SoknadMetadata();
        ettersendelse.id = soknadMetadataRepository.hentNesteId();
        ettersendelse.behandlingsId = lagBehandlingsId(ettersendelse.id);
        ettersendelse.tilknyttetBehandlingsId = ettersendesPaSoknad.behandlingsId;
        ettersendelse.fnr = ettersendesPaSoknad.fnr;
        ettersendelse.type = SoknadType.SEND_SOKNAD_KOMMUNAL_ETTERSENDING;
        ettersendelse.skjema = ettersendesPaSoknad.skjema;
        ettersendelse.status = UNDER_ARBEID;
        ettersendelse.opprettetDato = LocalDateTime.now(clock);
        ettersendelse.sistEndretDato = LocalDateTime.now(clock);

        ettersendelse.orgnr = ettersendesPaSoknad.orgnr;
        ettersendelse.navEnhet = ettersendesPaSoknad.navEnhet;

        soknadMetadataRepository.opprett(ettersendelse);

        return ettersendelse.behandlingsId;
    }

    public List<SoknadMetadata> hentBehandlingskjede(String behandlingskjedeId) {
        return soknadMetadataRepository.hentBehandlingskjede(behandlingskjedeId);
    }

    public int hentAntallInnsendteSoknaderEtterTidspunkt(String fnr, LocalDateTime tidspunkt) {
        return soknadMetadataRepository.hentAntallInnsendteSoknaderEtterTidspunkt(fnr, tidspunkt);
    }

    public void oppdaterMetadataVedAvslutningAvSoknad(String behandlingsId, SoknadMetadata.VedleggMetadataListe vedlegg, SoknadUnderArbeid soknadUnderArbeid, boolean brukerDigisosApi) {
        SoknadMetadata meta = soknadMetadataRepository.hent(behandlingsId);

        meta.vedlegg = vedlegg;

        if (meta.type != SoknadType.SEND_SOKNAD_KOMMUNAL_ETTERSENDING) {
            meta.orgnr = soknadUnderArbeid.getJsonInternalSoknad().getMottaker().getOrganisasjonsnummer();
            meta.navEnhet = soknadUnderArbeid.getJsonInternalSoknad().getMottaker().getNavEnhetsnavn();
        }

        meta.sistEndretDato = LocalDateTime.now(clock);
        meta.innsendtDato = LocalDateTime.now(clock);

        meta.status = brukerDigisosApi ? SENDT_MED_DIGISOS_API : FERDIG;

        soknadMetadataRepository.oppdater(meta);

        logger.info("Søknad avsluttet {} {}, {}", behandlingsId, meta.skjema, vedlegg.vedleggListe.size());
    }

    public SoknadMetadata hentSoknad(String behandlingsId) {
        return soknadMetadataRepository.hent(behandlingsId);
    }

    public void oppdaterSistEndretDatoPaaMetadata(String behandlingsId) {
        SoknadMetadata hentet = soknadMetadataRepository.hent(behandlingsId);
        hentet.sistEndretDato = LocalDateTime.now(clock);
        soknadMetadataRepository.oppdater(hentet);
    }

    public void avbrytSoknad(String behandlingsId, boolean avbruttAutomatisk) {
        SoknadMetadata metadata = soknadMetadataRepository.hent(behandlingsId);
        metadata.status = avbruttAutomatisk ? AVBRUTT_AUTOMATISK : AVBRUTT_AV_BRUKER;
        metadata.sistEndretDato = LocalDateTime.now(clock);

        soknadMetadataRepository.oppdater(metadata);
    }


}