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
public class FillagerConnector {

    private static final Logger LOG = getLogger(FillagerConnector.class);
    @Inject
    @Named("fillagerService")
    private FilLagerPortType portType;
    @Inject
    @Named("fillagerServiceSelftest")
    private FilLagerPortType portTypeSystemSecurity;

    public void lagreFil(String behandlingsId, String uid, String fnr, InputStream fil) {
        LOG.info("Skal lagre soknad til henvendelse. UUID: " + uid + ". Behandlingsid: " + behandlingsId);
        try {

            FilLagerPortType filLagerPortType = portType;
            if (getSubjectHandler().getIdentType() == null) {
                filLagerPortType = portTypeSystemSecurity;
                LOG.debug("Bruker systembruker for kall");
            }
            filLagerPortType.lagre(behandlingsId, uid, fnr, new DataHandler(new ByteArrayDataSource(fil, "application/octet-stream")));
            LOG.info("Søknad lagret til henvendelse");
        } catch (IOException e) {
            LOG.error("Fikk ikke lagret søknad til henvendelse");
            throw new ApplicationException("Kunne ikke lagre fil: " + e + ". BehandlingsID: " + behandlingsId, e);
        } catch (SOAPFaultException ws) {
            LOG.error("Fikk ikke lagret søknad til henvendelse");
            throw new SystemException("Feil i kommunikasjon med fillager: " + ws + ". BehandlingsID: " + behandlingsId, ws);
        }
    }

    public byte[] hentFil(String uuid) {
        Holder<DataHandler> innhold = new Holder<>();
        portType.hent(new Holder<>(uuid), innhold);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            innhold.value.writeTo(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke hente ut fil fra henvendelse");
        }
    }

    public List<WSInnhold> hentFiler(String brukerBehandlingId) {
        return portType.hentAlle(brukerBehandlingId);

    }
}
