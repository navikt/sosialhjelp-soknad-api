package no.nav.sbl.dialogarena.websoknad.service;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSEmpty;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSSoknadsdata;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.List;

@Component
public class HenvendelseConnector{

    private static final Logger logger = LoggerFactory.getLogger(HenvendelseConnector.class);

    private static final String SOKNADINNSENDING = "SEND_SOKNAD";

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
            WSStartSoknadRequest request = new WSStartSoknadRequest().withFodselsnummer(fnr).withType(SOKNADINNSENDING).withAny(Transformers.convertToFaktumListe(fakta));
            WSBehandlingsId behandlingsId = sendSoknadService.startSoknad(request);
            return behandlingsId.getBehandlingsId();
        } catch (SOAPFaultException e) {
            logger.error("Feil ved oppretting av søknad for bruker " + fnr, e);
            throw new ApplicationException("Kunne ikke opprette ny søknad", e);
        }
    }
    

    public void lagreSoknad(List<Faktum> fakta) {
        WSSoknadsdata soknadsdata = new WSSoknadsdata();
        soknadsdata.setAny(Transformers.convertToFaktumListe(fakta));
        sendSoknadService.mellomlagreSoknad(soknadsdata);
    }

    
    /* (non-Javadoc)
     * @see no.nav.sbl.dialogarena.websoknad.service.SendSoknadService#hentSoknad(long)
     */
    public WebSoknad hentSoknad(String behandlingsId) {
        logger.debug("Hent søknad");
        try {
            WSSoknadsdata soknadData = sendSoknadService.hentSoknad(new WSBehandlingsId().withBehandlingsId(behandlingsId));
            return Transformers.convertToSoknad(soknadData);
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
   
}