package no.nav.sbl.dialogarena.websoknad.service;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.informasjon.WSBrukerData;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.informasjon.WSSoknadData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.equalToIgnoreCase;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.sbl.dialogarena.websoknad.service.Transformers.TIL_SOKNADID;
import static no.nav.sbl.dialogarena.websoknad.service.Transformers.TIL_STATUS;
import static no.nav.sbl.dialogarena.websoknad.service.Transformers.tilFaktum;

public class WebSoknadService implements SendSoknadService {

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
    @Override
    public Long startSoknad(String navSoknadId) {
        logger.debug("Start søknad");
        try {
            return sendSoknadService.startSoknad(navSoknadId);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved oppretting av søknad med ID", navSoknadId, e);
            throw new ApplicationException("Kunne ikke opprette ny søknad", e);
        }
    }

    /* (non-Javadoc)
     * @see no.nav.sbl.dialogarena.websoknad.service.SendSoknadService#hentSoknad(long)
	 */
    public WebSoknad hentSoknad(long soknadId) {
        logger.debug("Hent søknad");
        try {
            WSSoknadData soknadData = sendSoknadService.hentSoknad(soknadId);
            return convertToSoknad(soknadData);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved henting av søknadsstruktur for søknad med ID {}", soknadId, e);
            throw new ApplicationException("SoapFaultException", e);
        }
    }

    /* (non-Javadoc)
	 * @see no.nav.sbl.dialogarena.websoknad.service.SendSoknadService#lagreSoknadsFelt(long, java.lang.String, java.lang.String)
	 */
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

    /* (non-Javadoc)
	 * @see no.nav.sbl.dialogarena.websoknad.service.SendSoknadService#sendSoknad(long)
	 */
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

    /* (non-Javadoc)
	 * @see no.nav.sbl.dialogarena.websoknad.service.SendSoknadService#hentMineSoknader(java.lang.String)
	 */
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

    /* (non-Javadoc)
	 * @see no.nav.sbl.dialogarena.websoknad.service.SendSoknadService#avbrytSoknad(java.lang.Long)
	 */
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

    private WebSoknad convertToSoknad(WSSoknadData wsSoknad) {
        Long soknadId = wsSoknad.getSoknadId();
        Map<String, Faktum> fakta = new LinkedHashMap<>();
        for (WSBrukerData wsBrukerData : wsSoknad.getFaktum()) {
            fakta.put(wsBrukerData.getNokkel(), tilFaktum(soknadId).transform(wsBrukerData));
        }

        WebSoknad soknad = new WebSoknad();
        soknad.setSoknadId(soknadId);
        soknad.setBrukerBehandlingId(wsSoknad.getBrukerBehandlingId());
        soknad.setGosysId(wsSoknad.getGosysId());
        soknad.leggTilFakta(fakta);

        return soknad;
    }
}