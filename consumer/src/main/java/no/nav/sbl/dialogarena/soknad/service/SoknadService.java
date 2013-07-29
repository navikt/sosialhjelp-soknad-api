package no.nav.sbl.dialogarena.soknad.service;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;

public class SoknadService {

    private static final Logger logger = LoggerFactory.getLogger(SoknadService.class);

    @Inject
    private SendSoknadPortType sendsoknadPortType;

    public String hentSoknad(long soknadId) {
        try {
            return sendsoknadPortType.hentSoknadData(soknadId);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved henting av søknadsstruktur for søknad med ID {}", soknadId, e);
            throw new ApplicationException("SoapFaultException", e);
        }
    }
}
