package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import com.google.common.collect.ImmutableMap;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksData.DokumentInfo;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service
public class MetadataInnfyller {

    static final String FIKSFORSENDELSEID_KEY = "fiksforsendelseId";
    static final String ORGNUMMER_KEY = "orgnummer";
    static final String NAVENHETSNAVN_KEY = "navenhetsnavn";
    @Inject
    SoknadMetadataRepository soknadMetadataRepository;
    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    @Inject
    private SendtSoknadRepository sendtSoknadRepository;
    @Inject
    private SoknadUnderArbeidService soknadUnderArbeidService;

    public void byggOppFiksData(FiksData data) {
        SoknadMetadata soknadMetadata = soknadMetadataRepository.hent(data.behandlingsId);

        final String eier = data.avsenderFodselsnummer;
        Optional<SoknadUnderArbeid> soknadUnderArbeidOptional = soknadUnderArbeidRepository.hentSoknad(data.behandlingsId, eier);
        if (!soknadUnderArbeidOptional.isPresent()) {
            throw new RuntimeException("Søknad under arbeid finnes ikke for behandlingsId " + data.behandlingsId);
        }
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidOptional.get();
        data.innsendtDato = soknadUnderArbeid.getSistEndretDato();

        if (soknadUnderArbeid.erEttersendelse()) {
            final Map<String, String> mottakerinfo = finnOriginalMottaksinfoVedEttersendelse(soknadMetadata, eier, soknadUnderArbeid.getTilknyttetBehandlingsId());
            data.ettersendelsePa = mottakerinfo.get(FIKSFORSENDELSEID_KEY);
            data.mottakerOrgNr = mottakerinfo.get(ORGNUMMER_KEY);
            data.mottakerNavn = mottakerinfo.get(NAVENHETSNAVN_KEY);
        } else {
            JsonInternalSoknad internalSoknad = soknadUnderArbeidService.hentJsonInternalSoknadFraSoknadUnderArbeid(soknadUnderArbeid);
            data.mottakerOrgNr = internalSoknad.getMottaker().getOrganisasjonsnummer();
            data.mottakerNavn = internalSoknad.getMottaker().getNavEnhetsnavn();
        }
        byggOppDokumentInfo(data, soknadMetadata);
    }

    Map<String, String> finnOriginalMottaksinfoVedEttersendelse(SoknadMetadata soknadMetadata, String eier, String tilknyttetBehandlingsId) {
        Optional<SendtSoknad> originalSoknadOptional = sendtSoknadRepository.hentSendtSoknad(tilknyttetBehandlingsId, eier);
        String originalFiksForsendelseId;
        String orgnummer;
        String navEnhetsnavn;
        if (originalSoknadOptional.isPresent()) {
            SendtSoknad originalSoknad = originalSoknadOptional.get();
            originalFiksForsendelseId = originalSoknad.getFiksforsendelseId();
            orgnummer = originalSoknad.getOrgnummer();
            navEnhetsnavn = originalSoknad.getNavEnhetsnavn();
        } else {
            SoknadMetadata originalSoknadGammeltFormat = soknadMetadataRepository.hent(soknadMetadata.tilknyttetBehandlingsId);
            if (originalSoknadGammeltFormat == null) {
                throw new RuntimeException("Kan ikke ettersende, finner ikke originalsøknad");
            } else {
                originalFiksForsendelseId = originalSoknadGammeltFormat.fiksForsendelseId;
                orgnummer = originalSoknadGammeltFormat.orgnr;
                navEnhetsnavn = originalSoknadGammeltFormat.navEnhet;
            }
        }
        if (isEmpty(originalFiksForsendelseId)) {
            throw new RuntimeException("Kan ikke ettersende, originalsoknaden ikke fått fiksid enda");
        }
        return new ImmutableMap.Builder<String, String>()
                .put(FIKSFORSENDELSEID_KEY, originalFiksForsendelseId)
                .put(ORGNUMMER_KEY, orgnummer)
                .put(NAVENHETSNAVN_KEY, navEnhetsnavn)
                .build();
    }

    public void lagreFiksId(FiksData data, FiksResultat resultat) {
        SoknadMetadata soknadMetadata = soknadMetadataRepository.hent(data.behandlingsId);
        soknadMetadata.fiksForsendelseId = resultat.fiksForsendelsesId;
        soknadMetadataRepository.oppdater(soknadMetadata);
    }

    void byggOppDokumentInfo(FiksData data, SoknadMetadata metadata) {
        List<DokumentInfo> infoer = new ArrayList<>();

        // Bestemt rekkefølge på ting...
        if (metadata.type == SoknadType.SEND_SOKNAD_KOMMUNAL) {
            infoer.add(leggTilSoknadJson(metadata.hovedskjema));
            infoer.add(leggTilPdf(metadata.hovedskjema));
            infoer.add(leggTilVedleggJson(metadata.hovedskjema));
            infoer.add(leggTilJuridiskPdf(metadata.hovedskjema));
            infoer.addAll(leggTilVedlegg(metadata.vedlegg));
        } else if (metadata.type == SoknadType.SEND_SOKNAD_KOMMUNAL_ETTERSENDING) {
            infoer.add(leggTilEttersendelsePdf(metadata.hovedskjema));
            infoer.add(leggTilVedleggJson(metadata.hovedskjema));
            infoer.addAll(leggTilVedlegg(metadata.vedlegg));
        } else {
            throw new RuntimeException("Ugyldig innsendingstype");
        }

        data.dokumentInfoer = infoer;
    }

    private List<DokumentInfo> leggTilVedlegg(SoknadMetadata.VedleggMetadataListe vedlegg) {
        return vedlegg.vedleggListe.stream()
                .filter(v -> v.status.equals(Vedlegg.Status.LastetOpp))
                .filter(v -> !"0".equals(v.filStorrelse))
                .map(v -> new DokumentInfo(v.filUuid, v.filnavn, v.mimetype, true))
                .collect(Collectors.toList());
    }
    
    private DokumentInfo leggTilEttersendelsePdf(SoknadMetadata.HovedskjemaMetadata hovedskjema) {
        return new DokumentInfo(hovedskjema.filUuid, "ettersendelse.pdf", hovedskjema.mimetype);
    }

    private DokumentInfo leggTilPdf(SoknadMetadata.HovedskjemaMetadata hovedskjema) {
        return new DokumentInfo(hovedskjema.filUuid, "Soknad.pdf", hovedskjema.mimetype);
    }

    private DokumentInfo leggTilJuridiskPdf(SoknadMetadata.HovedskjemaMetadata hovedskjema) {
        SoknadMetadata.FilData pdf = hovedskjema.alternativRepresentasjon.stream()
                .filter(rep -> rep.mimetype.equals("application/pdf-fullversjon"))
                .findFirst().orElseThrow(() -> new RuntimeException("Fant ikke fullversjon"));
        return new DokumentInfo(pdf.filUuid, "Soknad-juridisk.pdf", "application/pdf");
    }

    private DokumentInfo leggTilSoknadJson(SoknadMetadata.HovedskjemaMetadata hovedskjema) {
        SoknadMetadata.FilData json = hovedskjema.alternativRepresentasjon.stream()
                .filter(rep -> rep.mimetype.equals("application/json"))
                .filter(rep -> rep.filnavn.equals("soknad.json"))
                .findFirst().orElseThrow(() -> new RuntimeException("Fant ikke soknad.json"));
        return new DokumentInfo(json.filUuid, json.filnavn, json.mimetype, true);
    }

    private DokumentInfo leggTilVedleggJson(SoknadMetadata.HovedskjemaMetadata hovedskjema) {
        SoknadMetadata.FilData json = hovedskjema.alternativRepresentasjon.stream()
                .filter(rep -> rep.mimetype.equals("application/json"))
                .filter(rep -> rep.filnavn.equals("vedlegg.json"))
                .findFirst().orElseThrow(() -> new RuntimeException("Fant ikke vedlegg.json"));
        return new DokumentInfo(json.filUuid, json.filnavn, json.mimetype, true);
    }
}
