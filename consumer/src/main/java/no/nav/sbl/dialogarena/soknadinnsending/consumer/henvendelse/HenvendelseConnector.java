package no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSSoknadsdata;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.soap.SOAPFaultException;

@Component
public class HenvendelseConnector {

    private static final Logger logger = LoggerFactory.getLogger(HenvendelseConnector.class);
    private static final String SOKNADINNSENDING = "SEND_SOKNAD";
    @Inject
    @Named("sendSoknadService")
    private SendSoknadPortType sendSoknadService;

    public String hentSoknadEier(Long soknadId) {
        return "";
    }

    public String startSoknad(String fnr, String hovedskjema) {
        logger.error("Inne i metoden for startSoknad");
        try {
            XMLHovedskjema skjema = new XMLHovedskjema().withInnsendingsvalg("LASTET_OPP").withSkjemanummer(hovedskjema);
            WSStartSoknadRequest request = new WSStartSoknadRequest().withFodselsnummer(fnr).withType(SOKNADINNSENDING).withAny(new XMLMetadataListe().withMetadata(skjema));
            WSBehandlingsId behandlingsId = sendSoknadService.startSoknad(request);
            return behandlingsId.getBehandlingsId();
        } catch (SOAPFaultException e) {
            logger.error("Feil ved start søknad for bruker " + fnr, e);
            throw new ApplicationException("Kunne ikke opprette ny søknad", e);
        }
    }

    public void avsluttSoknad(String behandlingsId, XMLHovedskjema hovedskjema, XMLVedlegg... vedlegg) {
        try {
            WSSoknadsdata parameters = new WSSoknadsdata().withBehandlingsId(behandlingsId).withAny(new XMLMetadataListe()
                    .withMetadata(hovedskjema)
                    .withMetadata(vedlegg));
            sendSoknadService.sendSoknad(parameters);
        } catch (SOAPFaultException e) {
            logger.error("Feil ved innsending av søknad: " + e, e);
            throw new ApplicationException("Kunne ikke opprette ny søknad", e);
        }
    }

    public no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSEmpty sendSoknad(WSSoknadsdata soknadsData) {
        return sendSoknadService.sendSoknad(soknadsData);
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