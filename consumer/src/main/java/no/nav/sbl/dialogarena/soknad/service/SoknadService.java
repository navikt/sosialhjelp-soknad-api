package no.nav.sbl.dialogarena.soknad.service;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.ArrayList;
import java.util.List;

public class SoknadService {

    private static final Logger logger = LoggerFactory.getLogger(SoknadService.class);

    @Inject
    private SendSoknadPortType sendsoknadPortType;

    public Long startSoknad(String navSoknadId) {
        try {
            return sendsoknadPortType.startSoknad(navSoknadId);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved oppretting av søknad med ID", navSoknadId, e);
            throw new ApplicationException("Kunne ikke opprette ny søknad", e);
        }
    }

    public String hentSoknad(long soknadId) {
        try {
            return sendsoknadPortType.hentSoknadData(soknadId);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved henting av søknadsstruktur for søknad med ID {}", soknadId, e);
            throw new ApplicationException("SoapFaultException", e);
        }
    }

    public void lagreSoknadsFelt(long soknadId, String key, String value) {
        try {
            sendsoknadPortType.lagreBrukerData(soknadId, key, value);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved henting av søknadsstruktur for søknad med ID {}", soknadId, e);
            throw new ApplicationException("SoapFaultException", e);
        }
    }

    public void sendSoknad(long soknadId) {
        try {
            sendsoknadPortType.sendSoknad(soknadId);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved sending av søknad med ID {}", soknadId, e);
            throw new ApplicationException("Feil ved sending av søknad", e);
        }
    }

    public List<Long> hentMineSoknader(String aktorId) {
        List<Long> list = new ArrayList<>();
        list.add(1L);
        list.add(2L);
        list.add(3L);
        list.add(4L);
        list.add(5L);
        return list;
    }
}
