package no.nav.sbl.dialogarena.dokumentinnsending.service;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventning;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSInnsendingsValg;
import no.nav.tjeneste.domene.brukerdialog.henvendelsesbehandling.v1.HenvendelsesBehandlingPortType;
import no.nav.tjeneste.domene.brukerdialog.oppdaterehenvendelsesbehandling.v1.OppdatereHenvendelsesBehandlingPortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.dokumentinnsending.service.Transformers.TIL_DOKUMENTFORVENTNING;
import static no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandlingType.DOKUMENT_BEHANDLING;
import static no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandlingType.DOKUMENT_ETTERSENDING;
import static org.apache.commons.collections15.CollectionUtils.collect;

/**
 * Klasse som oppretter DokumentBehandling i henvendelse-applikasjonen via Webservice-kall. Den har også kall mot ping-tjeneste.
 *
 */
public class DefaultOpprettBrukerBehandlingServiceIntegration implements OpprettBrukerBehandlingService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultOpprettBrukerBehandlingServiceIntegration.class);

    @Inject
    protected OppdatereHenvendelsesBehandlingPortType oppdatereService;

    @Inject
    protected HenvendelsesBehandlingPortType henvendelsesBehandlingService;

    @Override
    public String opprettDokumentBehandling(String hovedskjemaId, List<String> vedleggsIder, boolean erEttersending) {
        List<WSDokumentForventning> dokumentForventninger = new ArrayList<>();
        WSDokumentForventning hovedskjema = lagHovedskjema(hovedskjemaId, erEttersending);
        dokumentForventninger.add(hovedskjema);
        if (!vedleggsIder.isEmpty()) {
            dokumentForventninger.addAll(collect(vedleggsIder, TIL_DOKUMENTFORVENTNING));
        }

        return opprettDokumentforventning(erEttersending, dokumentForventninger);
    }

    @Override
    public Boolean ping() {
        return henvendelsesBehandlingService.ping();
    }


    private WSDokumentForventning lagHovedskjema(String hovedskjemaId, boolean erEttersending) {
        WSDokumentForventning hovedskjema = TIL_DOKUMENTFORVENTNING.transform(hovedskjemaId);
        hovedskjema.setHovedskjema(true);
        if (erEttersending) {
            hovedskjema.setInnsendingsValg(WSInnsendingsValg.INNSENDT);
        }
        return hovedskjema;
    }

    private String opprettDokumentforventning(boolean erEttersending, List<WSDokumentForventning> dokumentForventninger) {
        String behandlingsId;
        try {
            behandlingsId = oppdatereService.opprettDokumentBehandling(dokumentForventninger, erEttersending ? DOKUMENT_ETTERSENDING
                    : DOKUMENT_BEHANDLING);
            logger.info("Opprettet behandling med brukerBehandlingId {} ", behandlingsId);
        } catch (SOAPFaultException e) {
            logger.error("Kunne ikke opprette behandling", e);
            throw new ApplicationException("SoapFaultException", e, "ws.feil.oppprettdokumentbehandling");
        }
        return behandlingsId;
    }
}
