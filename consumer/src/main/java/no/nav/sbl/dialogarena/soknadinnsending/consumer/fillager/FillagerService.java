package no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager;


import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.core.exception.SystemException;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.meldinger.WSInnhold;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class FillagerService {

    private static final Logger logger = getLogger(FillagerService.class);

    @Inject
    @Named("fillagerEndpoint")
    private FilLagerPortType filLagerEndpoint;

    @Inject
    @Named("fillagerSelftestEndpoint")
    private FilLagerPortType filLagerSelftestEndpoint;

    public void lagreFil(String behandlingsId, String uid, String fnr, InputStream fil) {
        logger.info("Skal lagre fil til henvendelse for behandling med ID {}. UUID: {}", behandlingsId, uid);
        try {
            FilLagerPortType filLagerPortType = filLagerEndpoint;
            if (getSubjectHandler().getIdentType() == null) {
                filLagerPortType = filLagerSelftestEndpoint;
                logger.debug("Bruker systembruker for kall");
            }
            filLagerPortType.lagre(behandlingsId, uid, fnr, new DataHandler(new ByteArrayDataSource(fil, "application/octet-stream")));
            logger.info("Fil lagret til henvendelse");
        } catch (IOException e) {
            logger.error("Fikk ikke lagret fil til henvendelse");
            throw new ApplicationException("Kunne ikke lagre fil: " + e + ". BehandlingsID: " + behandlingsId + ". UUID: " + uid, e, "exception.system.baksystem");
        } catch (SOAPFaultException ws) {
            logger.error("Fikk ikke lagret fil til henvendelse");
            throw new SystemException("Feil i kommunikasjon med fillager: " + ws + ". BehandlingsID: " + behandlingsId + ". UUID: " + uid, ws, "exception.system.baksystem");
        }
    }

    public byte[] hentFil(String uuid) {
        Holder<DataHandler> innhold = new Holder<>();
        try {
            filLagerEndpoint.hent(new Holder<>(uuid), innhold);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            innhold.value.writeTo(stream);
            return stream.toByteArray();
        } catch (IOException e) {
            throw new SystemException("Kunne ikke hente ut fil fra henvendelse", e, "exception.system.baksystem");
        } catch (SOAPFaultException e) {
            throw new SystemException("Kunne ikke hente filer fra baksystem", e, "exception.system.baksystem");
        }
    }

    public List<WSInnhold> hentFiler(String brukerBehandlingId) {
        try {
            return filLagerEndpoint.hentAlle(brukerBehandlingId);
        } catch (SOAPFaultException e) {
            throw new SystemException("Kunne ikke hente filer fra baksystem", e, "exception.system.baksystem");
        }
    }

    public void slettAlle(String behandlingsId) {
        try {
            filLagerEndpoint.slettAlle(behandlingsId);
        } catch (SOAPFaultException e) {
            throw new SystemException("Kunne ikke slette filer fra baksystem", e, "exception.system.baksystem");
        }
    }
}
