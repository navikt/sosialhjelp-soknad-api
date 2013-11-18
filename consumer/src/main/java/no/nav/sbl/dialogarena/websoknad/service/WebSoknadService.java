package no.nav.sbl.dialogarena.websoknad.service;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLFaktumListe;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSEmpty;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSSoknadsdata;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.equalToIgnoreCase;
import static no.nav.modig.lang.collections.PredicateUtils.where;
//import static no.nav.sbl.dialogarena.henvendelse.dokumentforventning.DokumentForventningTransformer.TIL_BRUKER_BEHANDLING_TYPE;
//import static no.nav.sbl.dialogarena.henvendelse.dokumentforventning.DokumentForventningTransformer.TIL_DOKUMENTFORVENTNINGENTITY;
//import static no.nav.sbl.dialogarena.websoknad.service.Transformers.TIL_SOKNADID;
//import static no.nav.sbl.dialogarena.websoknad.service.Transformers.TIL_STATUS;
//import static no.nav.sbl.dialogarena.websoknad.service.Transformers.TIL_XMLFAKTUM;

public class WebSoknadService{

    private static final Logger logger = LoggerFactory.getLogger(WebSoknadService.class);

    @Inject
    @Named("sendSoknadService")
    private SendSoknadPortType sendSoknadService;

    public String hentSoknadEier(Long soknadId) {
        return "";
    }

    /* (non-Javadoc)
     * @see no.nav.sbl.dialogarena.websoknad.service.SendSoknadService#startSoknad(java.lang.String)
	 */
    public String startSoknad(String fnr, List<Faktum> fakta) {
        logger.debug("Start søknad");
        try {
            WSStartSoknadRequest request = new WSStartSoknadRequest().withFodselsnummer(fnr);
            XMLFaktumListe faktum = null; 
//            XMLFaktumListe faktumListe = on(fakta).map(TIL_XMLFAKTUM);
//            request.setAny()
            WSBehandlingsId behandlingsId = sendSoknadService.startSoknad(request);
            return behandlingsId.getBehandlingsId();
        } catch (SOAPFaultException e) {
            logger.error("Feil ved oppretting av søknad for bruker " + fnr, e);
            throw new ApplicationException("Kunne ikke opprette ny søknad", e);
        }
    }
    
    public void lagreSoknad(List<Faktum> fakta) {
        
        
    }
    
    /* (non-Javadoc)
     * @see no.nav.sbl.dialogarena.websoknad.service.SendSoknadService#hentSoknad(long)
     */
    public WebSoknad hentSoknad(String behandlingsId) {
        logger.debug("Hent søknad");
        try {
            WSSoknadsdata soknadData = sendSoknadService.hentSoknad(new WSBehandlingsId().withBehandlingsId(behandlingsId));
            return convertToSoknad(soknadData);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved henting av søknadsstruktur for søknad med ID {}", behandlingsId, e);
            throw new ApplicationException("SoapFaultException", e);
        }
    }
    
   
    public no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSEmpty sendSoknad(WSSoknadsdata soknadsData){
        return new WSEmpty();
    }
    
    public void avbrytSoknad(String behandlingsId) {
        logger.debug("Avbryt søknad");
        try {
            sendSoknadService.avbrytSoknad(behandlingsId);
        } catch (SOAPFaultException e) {
            logger.error("Kunne ikke avbryte søknad med ID {}", behandlingsId, e);
            throw new ApplicationException("Feil ved avbryting av søknad", e);
        }
    }
    
    private WebSoknad convertToSoknad(WSSoknadsdata wsSoknad) {
        return null;
        /*
    
        Long soknadId = wsSoknad.ge;
        Map<String, Faktum> fakta = new LinkedHashMap<>();
        for (WSBrukerData wsBrukerData : wsSoknad.getFaktum()) {
            fakta.put(wsBrukerData.getNokkel(), tilFaktum(soknadId).transform(wsBrukerData));
        }

        WebSoknad soknad = new WebSoknad();
        soknad.setSoknadId(soknadId);
        soknad.setBrukerBehandlingId(wsSoknad.getBrukerBehandlingId());
        soknad.setGosysId(wsSoknad.getGosysId());
        soknad.leggTilFakta(fakta);

        return soknad;*/
    }
    
/*
    @Override
    public void lagreSoknadsFelt(long soknadId, String key, String value) {
        logger.debug("Lagre søknad");
        try {
            sendSoknadService.lagreBrukerData(soknadId, key, value);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved henting av søknadsstruktur for søknad med ID {}", soknadId, e);
            throw new ApplicationException("SoapFaultException", e);
        }
    }

    @Override
    public void sendSoknad(long soknadId) {
        logger.debug("Send søknad");
        try {
            sendSoknadService.sendSoknad(soknadId);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved sending av søknad med ID {}", soknadId, e);
            throw new ApplicationException("Feil ved sending av søknad", e);
        }
    }

     @Override
    public List<Long> hentMineSoknader(String aktorId) {
        logger.debug("Hent søknader");
        try {
            // TODO: Endre status til å ikke være string når vi får rett status fra henvendelse
            return on(sendSoknadService.hentSoknadListe(aktorId))
                    .filter(where(TIL_STATUS, equalToIgnoreCase("under_arbeid")))
                    .map(TIL_SOKNADID)
                    .collect();
        } catch (SOAPFaultException e) {
            logger.error("Feil ved sending av søknader for aktør med ID {}", aktorId, e);
            throw new ApplicationException("Feil ved henting av søknader", e);
        }
    }

    @Override
    public void avbrytSoknad(Long soknadId) {
        logger.debug("Avbryt søknad");
        try {
            sendSoknadService.avbrytSoknad(soknadId);
        } catch (SOAPFaultException e) {
            logger.error("Kunne ikke avbryte søknad med ID {}", soknadId, e);
            throw new ApplicationException("Feil ved avbryting av søknad", e);
        }
    }

    @Override
    public void lagreSystemSoknadsFelt(long soknadId, String key, String value) {
        // TODO Auto-generated method stub
        
    }
    */
}