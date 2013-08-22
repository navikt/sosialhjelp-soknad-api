package no.nav.sbl.dialogarena.dokumentinnsending.service;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.BrukerBehandlingType;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentInnhold;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Skjema;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.SoknadStatus;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.VirusException;
import no.nav.sbl.dialogarena.dokumentinnsending.kodeverk.KodeverkClient;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandling;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokument;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventning;
import no.nav.tjeneste.domene.brukerdialog.henvendelsesbehandling.v1.HenvendelsesBehandlingPortType;
import no.nav.tjeneste.domene.brukerdialog.oppdaterehenvendelsesbehandling.v1.OppdatereHenvendelsesBehandlingPortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.Comparator;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.TransformerUtils.castTo;
import static no.nav.sbl.dialogarena.dokumentinnsending.cache.Caches.BRUKERBEHANDLING;
import static no.nav.sbl.dialogarena.dokumentinnsending.cache.Caches.DOKUMENT_INNHOLD;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.NAVN;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.EKSTERNT_VEDLEGG;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.EKSTRA_VEDLEGG;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.Type.NAV_VEDLEGG;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.avType;
import static no.nav.sbl.dialogarena.dokumentinnsending.service.Transformers.DOKUMENT_TIL_WS_DOKUMENT;
import static no.nav.sbl.dialogarena.dokumentinnsending.service.Transformers.TIL_DOKUMENTFORVENTNING;
import static no.nav.sbl.dialogarena.dokumentinnsending.service.Transformers.WS_DOKUMENT_TIL_DOKUMENT_INNHOLD;
import static no.nav.sbl.dialogarena.dokumentinnsending.service.Transformers.tilDokument;
import static no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSInnsendingsValg.fromValue;

public class DefaultSoknadService implements SoknadService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultSoknadService.class);

    @Inject
    private HenvendelsesBehandlingPortType henvendelsesBehandlingService;

    @Inject
    private OppdatereHenvendelsesBehandlingPortType oppdatereHenvendelseWebService;

    @Inject
    private KodeverkClient kodeverkClient;

    @Inject
    protected CmsContentRetriever cmsContentRetriever;

    @Override
    @CacheEvict(value = BRUKERBEHANDLING, key = "T(no.nav.sbl.dialogarena.dokumentinnsending.cache.UserKeyGenerator).generate(#dokument.behandlingsId)")
    public void oppdaterInnsendingsvalg(Dokument dokument) {
        try {
            oppdatereHenvendelseWebService.oppdaterDokumentForventning(dokument.getDokumentForventningsId(), fromValue(dokument.getValg().name()));
        } catch (SOAPFaultException e) {
            logger.error("Feil ved endring av innsendingsvalg for dokumentforventing med ID {}", dokument.getDokumentForventningsId(), e);
            throw new ApplicationException("SoapFaultException", e, "ws.feil.oppdaterdokumentforventning");
        }
    }

    @Override
    @CacheEvict(value = BRUKERBEHANDLING, key = "T(no.nav.sbl.dialogarena.dokumentinnsending.cache.UserKeyGenerator).generate(#brukerbehandlingId)")
    public Long leggTilVedlegg(String brukerbehandlingId, String vedleggsTekst) {
        WSDokumentForventning forventning = TIL_DOKUMENTFORVENTNING.transform(Kodeverk.ANNET);
        forventning.setFriTekst(vedleggsTekst);
        return oppdatereHenvendelseWebService.opprettDokumentForventning(forventning, brukerbehandlingId);
    }

    @Override
    @Cacheable(value = BRUKERBEHANDLING, key = "T(no.nav.sbl.dialogarena.dokumentinnsending.cache.UserKeyGenerator).generate(#brukerbehandlingId)")
    public DokumentSoknad hentSoknad(String brukerbehandlingId) {
        try {
            WSBrukerBehandling behandling = henvendelsesBehandlingService.hentBrukerBehandling(brukerbehandlingId);
            return konverterTilSoknad(behandling, brukerbehandlingId);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved henting av søknad med behandlings-ID {}", brukerbehandlingId, e);
            throw new ApplicationException("SoapFaultException", e, "ws.feil.hentdokumentforventning");
        }
    }

    @Override
    public Dokument hentDokument(long dokumentForventningId, String behandlingsId) {
        WSDokumentForventning forventning = henvendelsesBehandlingService.hentDokumentForventning(dokumentForventningId);
        return tilDokument(kodeverkClient, behandlingsId).transform(forventning);
    }

    @Override
    public Dokument hentOppdatertDokument(Dokument dokument) {
        WSDokumentForventning forventning = henvendelsesBehandlingService.hentDokumentForventning(dokument.getDokumentForventningsId());
        return tilDokument(kodeverkClient, dokument.getBehandlingsId()).transform(forventning);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "forhaandsvisningId"),
                    @CacheEvict(value = BRUKERBEHANDLING, key = "T(no.nav.sbl.dialogarena.dokumentinnsending.cache.UserKeyGenerator).generate(#dokument.behandlingsId)")
            }
    )
    public void oppdaterInnhold(Dokument dokument, DokumentInnhold innhold) {
        try {
            oppdatereHenvendelseWebService.opprettDokument(DOKUMENT_TIL_WS_DOKUMENT.transform(innhold), dokument.getDokumentForventningsId());
        } catch (SOAPFaultException e) {
            logger.error("Feil ved lagring av dokument til dokumentforventning med ID {}", dokument.getDokumentForventningsId(), e);
            if (e.getFault().getFaultString().toLowerCase().contains("virus found")) {
                logger.warn("Virus oppdaget i fil som ble forsøkt opplastet til dokumentforventning med ID {}", dokument.getDokumentForventningsId(), e);
                throw new VirusException("Virus oppdaget i filen", e);
            } else {
                throw new ApplicationException("SoapFaultException", e, "ws.feil.lagredokument");
            }
        }
    }

    @Override
    @CacheEvict(value = BRUKERBEHANDLING, key = "T(no.nav.sbl.dialogarena.dokumentinnsending.cache.UserKeyGenerator).generate(#dokument.behandlingsId)")
    public void oppdaterBeskrivelseAnnetVedlegg(Dokument dokument, String beskrivelse) {
        try {
            oppdatereHenvendelseWebService.oppdaterDokumentForventningBeskrivelse(dokument.getDokumentForventningsId(), beskrivelse);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved oppdatering av beskrivelse for dokumentforventning med ID {}", dokument.getDokumentForventningsId(), e);
            throw new ApplicationException("SoapFaultException", e, "ws.feil.hentdokumentforventning");
        }
    }

    @Override
    @CacheEvict(value = BRUKERBEHANDLING, key = "T(no.nav.sbl.dialogarena.dokumentinnsending.cache.UserKeyGenerator).generate(#brukerbehandlingId)")
    public void slettSoknad(String brukerbehandlingId) {
        try {
            oppdatereHenvendelseWebService.avbrytHenvendelse(brukerbehandlingId);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved sletting av søknad knyttet til brukerbehandling med ID {}", brukerbehandlingId, e);
            throw new ApplicationException("SoapFaultException", e, "ws.feil.slettsoknad");
        }
    }

    @Override
    @Cacheable(value = DOKUMENT_INNHOLD, key = "T(no.nav.sbl.dialogarena.dokumentinnsending.cache.UserKeyGenerator).generate(#dokument.dokumentId)")
    public DokumentInnhold hentDokumentInnhold(Dokument dokument) {
        if (!dokument.erOpplastet()) {
            return null;
        }
        Long dokumentId = dokument.getDokumentId();
        return hentDokumentInnhold(dokumentId);
    }

    @Override
    @Cacheable(value = DOKUMENT_INNHOLD, key = "T(no.nav.sbl.dialogarena.dokumentinnsending.cache.UserKeyGenerator).generate(#dokumentId)")
    public DokumentInnhold hentDokumentInnhold(Long dokumentId) {
        try {
            WSDokument wsDokument = henvendelsesBehandlingService.hentDokument(dokumentId);
            return WS_DOKUMENT_TIL_DOKUMENT_INNHOLD.transform(wsDokument);
        } catch (SOAPFaultException e) {
            throw new ApplicationException("Kunne ikke hente ut innhold til dokumentet", e, "ws.feil.hentDokumentInnhold");
        }
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = BRUKERBEHANDLING, key = "T(no.nav.sbl.dialogarena.dokumentinnsending.cache.UserKeyGenerator).generate(#dokument.behandlingsId)"),
                    @CacheEvict(value = DOKUMENT_INNHOLD, key = "T(no.nav.sbl.dialogarena.dokumentinnsending.cache.UserKeyGenerator).generate(#dokument.dokumentId)")
            }
    )
    public void slettInnhold(Dokument dokument) {
        if (dokument != null) {
            oppdatereHenvendelseWebService.slettDokument(dokument.getDokumentId());
        }
    }

    private DokumentSoknad konverterTilSoknad(WSBrukerBehandling behandling, String behandlingsId) {
        Iterable<Dokument> dokumenter = on(behandling.getDokumentForventninger().getDokumentForventning())
                .map(tilDokument(kodeverkClient, behandlingsId))
                .collect(BY_DOKUMENTNAVN);

        DokumentSoknad soknad = new DokumentSoknad();
        soknad.brukerbehandlingId = behandling.getBehandlingsId();
        soknad.ident = hentAutentisertBruker();
        soknad.soknadTittel = on(dokumenter).filter(avType(Type.HOVEDSKJEMA)).map(NAVN).head().get();
        soknad.brukerBehandlingType = BrukerBehandlingType.valueOf(behandling.getBrukerBehandlingType().name());
        soknad.status = SoknadStatus.valueOf(behandling.getStatus().name());
        soknad.sistEndret = behandling.getSistEndret();
        soknad.innsendtDato = behandling.getInnsendtDato();

        soknad.hovedskjema = on(dokumenter).filter(avType(Type.HOVEDSKJEMA)).map(castTo(Skjema.class)).head().get();
        soknad.leggTilVedlegg(on(dokumenter).filter(avType(NAV_VEDLEGG)).map(castTo(Skjema.class)));
        soknad.leggTilVedlegg(on(dokumenter).filter(avType(EKSTERNT_VEDLEGG)));
        soknad.leggTilVedlegg(on(dokumenter).filter(avType(EKSTRA_VEDLEGG)));

        return soknad;
    }

    private static final Comparator<Dokument> BY_DOKUMENTNAVN = new Comparator<Dokument>() {
        @Override
        public int compare(Dokument dok1, Dokument dok2) {
            return dok1.getNavn().compareTo(dok2.getNavn());
        }
    };

    private String hentAutentisertBruker() {
        return SubjectHandler.getSubjectHandler().getUid();
    }
}