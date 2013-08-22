package no.nav.sbl.dialogarena.dokumentinnsending.service;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.dokumentinnsending.convert.PdfGenerator;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.BrukerBehandlingType;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Skjema;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandlingOppsummering;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventning;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentInnhold;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.dokumentinnsending.cache.Caches.BRUKERBEHANDLING;
import static no.nav.sbl.dialogarena.dokumentinnsending.kodeverk.KodeverkClient.KVITTERING_KODEVERKSID;
import static no.nav.sbl.dialogarena.dokumentinnsending.service.Transformers.BEHANDLINGSID;
import static no.nav.sbl.dialogarena.dokumentinnsending.service.Transformers.TIL_DOKUMENTFORVENTNING;

public class DefaultBrukerBehandlingServiceIntegration extends DefaultOpprettBrukerBehandlingServiceIntegration implements BrukerBehandlingServiceIntegration {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBrukerBehandlingServiceIntegration.class);


    @Inject
    private SoknadService soknadService;

    private PdfGenerator pdfGenerator = new PdfGenerator();

    @Override
    @CacheEvict(value = BRUKERBEHANDLING, key = "#behandlingsId")
    public void oppdaterBrukerBehandling(String behandlingsId, String brukerIdent) {
        try {
            oppdatereService.identifiserAktor(behandlingsId, brukerIdent);
        } catch (SOAPFaultException e) {
            logger.error("Kunne ikke lagre aktørId {} i behandling med ID {}", brukerIdent, behandlingsId, e);
            throw new ApplicationException("SoapFaultException", e, "ws.feil.oppdaterbrukerbehandling");
        }
    }

    @Override
    public List<String> hentBrukerBehandlingIder(String aktorId) {
        try {
            List<WSBrukerBehandlingOppsummering> brukerBehandlinger = henvendelsesBehandlingService.hentBrukerBehandlingListe(aktorId);
            return on(brukerBehandlinger).map(BEHANDLINGSID).collect();
        } catch (SOAPFaultException e) {
            logger.error("Kunne ikke hente brukerbehandlinger for aktørId {}", aktorId, e);
            throw new ApplicationException("SoapFaultException", e, "ws.feil.hentbrukerbehandlingider");
        }
    }

    @Override
    @CacheEvict(value = BRUKERBEHANDLING, key = "T(no.nav.sbl.dialogarena.dokumentinnsending.cache.UserKeyGenerator).generate(#behandlingsId)")
    public void sendBrukerBehandling(String behandlingsId, String journalFoerendeEnhet) {
        DokumentSoknad soknad = soknadService.hentSoknad(behandlingsId);
        leggTilForsideVedEttersending(soknad);
        // håndtere exception hvis man f.eks. har to vinduer oppe.
        try {
            leggTilKvittering(soknad, behandlingsId);
            oppdatereService.opprettElektroniskSamtykke(behandlingsId);
            oppdatereService.sendHenvendelse(behandlingsId, journalFoerendeEnhet);
        } catch (SOAPFaultException e) {
            throw new ApplicationException("Kunne ikke sende henvendelse", e);
        }
    }

    private void leggTilKvittering(DokumentSoknad soknad, String behandlingsId) {

        WSDokumentForventning forventning = TIL_DOKUMENTFORVENTNING.transform(KVITTERING_KODEVERKSID);
        long skjemaId = oppdatereService.opprettDokumentForventning(forventning, behandlingsId);
        oppdatereService.opprettDokument(
                new WSDokumentInnhold()
                        .withFilnavn("kvittering.pdf")
                        .withInnhold(new DataHandler(new ByteArrayDataSource(pdfGenerator.lagKvitteringsSide(soknad), "application/octet-stream"))), skjemaId);
    }

    private void leggTilForsideVedEttersending(DokumentSoknad soknad) {
        if (soknad.er(BrukerBehandlingType.DOKUMENT_ETTERSENDING)) {
            Skjema hovedskjema = soknad.hovedskjema;
            DataHandler data = new DataHandler(new ByteArrayDataSource(pdfGenerator.lagForsideEttersending(soknad), "application/octet-stream"));
            oppdatereService.opprettDokument(new WSDokumentInnhold().withFilnavn("forside.pdf").withInnhold(data), hovedskjema.getDokumentForventningsId());
        }
    }
}