package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksData.DokumentInfo;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetadataInnfyller {

    @Inject
    HenvendelseService henvendelseService;

    public void byggOppFiksData(FiksData data) {
        SoknadMetadata soknadMetadata = henvendelseService.hentSoknad(data.behandlingsId);

        data.avsenderFodselsnummer = soknadMetadata.fnr;
        data.mottakerOrgNr = soknadMetadata.orgnr;
        data.mottakerNavn = soknadMetadata.navEnhet;
        data.innsendtDato = soknadMetadata.innsendtDato;

        byggOppDokumentInfo(data, soknadMetadata);
    }

    private void byggOppDokumentInfo(FiksData data, SoknadMetadata metadata) {
        List<DokumentInfo> infoer = new ArrayList<>();

        // Bestemt rekkefølge på ting...
        infoer.add(leggTilSoknadJson(metadata.hovedskjema));
        infoer.add(leggTilPdf(metadata.hovedskjema));
        infoer.add(leggTilVedleggJson(metadata.hovedskjema));
        infoer.add(leggTilJuridiskPdf(metadata.hovedskjema));
        infoer.addAll(leggTilVedlegg(metadata.vedlegg));

        data.dokumentInfoer = infoer;
    }

    private List<DokumentInfo> leggTilVedlegg(SoknadMetadata.VedleggMetadataListe vedlegg) {
        return vedlegg.vedleggListe.stream()
                .filter(v -> v.status.equals(Vedlegg.Status.LastetOpp))
                .filter(v -> !"0".equals(v.filStorrelse))
                .map(v -> new DokumentInfo(v.filUuid, v.filnavn, v.mimetype, true))
                .collect(Collectors.toList());
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
