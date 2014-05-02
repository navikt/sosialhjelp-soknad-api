package no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.core.exception.SystemException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.SoknadType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSSoknadsdata;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.List;

import static java.util.UUID.randomUUID;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.IKKE_VALGT;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class HenvendelseConnector {

    private static final Logger logger = getLogger(HenvendelseConnector.class);
    @Inject
    @Named("sendSoknadService")
    private SendSoknadPortType sendSoknadService;

    public String hentSoknadEier(Long soknadId) {
        return "";
    }

    public String startSoknad(String fnr, String skjema, String uid) {
        logger.info("Starter søknad");
        XMLHovedskjema xmlSkjema = createXMLSkjema(skjema, uid);
        XMLMetadataListe xmlMetadataListe = new XMLMetadataListe().withMetadata(xmlSkjema);
        WSStartSoknadRequest xmlStartSoknadRequest = createXMLStartSoknadRequest(fnr, SoknadType.SEND_SOKNAD, xmlMetadataListe);
        return startSoknadEllerEttersending(xmlStartSoknadRequest);
    }

    public String startEttersending(WSHentSoknadResponse soknadResponse) {
        logger.info("Starter ettersending");
        String fnr = getSubjectHandler().getUid();
        String uid = randomUUID().toString();
        XMLMetadataListe xmlMetadataListe = (XMLMetadataListe) soknadResponse.getAny();
        XMLHovedskjema xmlHovedskjema = (XMLHovedskjema)xmlMetadataListe.getMetadata().get(0);
        XMLHovedskjema xmlSkjema = createXMLSkjema(xmlHovedskjema.getSkjemanummer(), uid);


        String behandlingskjedeId;
        if(soknadResponse.getBehandlingskjedeId() != null) {
             behandlingskjedeId = soknadResponse.getBehandlingskjedeId();
        } else {
            behandlingskjedeId = soknadResponse.getBehandlingsId();
        }
        WSStartSoknadRequest xmlStartSoknadRequest = createXMLStartEttersendingRequest(fnr, SoknadType.SEND_SOKNAD_ETTERSENDING, xmlMetadataListe, behandlingskjedeId);

        return startSoknadEllerEttersending(xmlStartSoknadRequest);
    }

    private String startSoknadEllerEttersending(WSStartSoknadRequest xmlStartSoknadRequest) {
        try {
            return sendSoknadService.startSoknad(xmlStartSoknadRequest).getBehandlingsId();
        } catch (SOAPFaultException e) {
            logger.error("Feil ved start søknad for bruker " + xmlStartSoknadRequest.getFodselsnummer(), e);
            throw new SystemException("Kunne ikke opprette ny søknad", e, "exception.system.baksystem");
        }
    }

    public List<WSBehandlingskjedeElement> hentBehandlingskjede(String behandlingskjedeId) {
        try {
            List<WSBehandlingskjedeElement> wsBehandlingskjedeElementer = sendSoknadService.hentBehandlingskjede(behandlingskjedeId);
            if (wsBehandlingskjedeElementer.isEmpty()) {
                throw new ApplicationException("Fant ingen behandlinger i en behandlingskjede med behandlingsID " + behandlingskjedeId);
            }
            return wsBehandlingskjedeElementer;
        } catch (SOAPFaultException e) {
            logger.error("Feil ved henting av behandlingdskjede" + e, e);
            throw new SystemException("Kunne ikke hente behandlingskjede", e, "exception.system.baksystem");
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
            throw new SystemException("Kunne ikke sende inn søknad", e, "exception.system.baksystem");
        }
    }

    public no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSEmpty sendSoknad(WSSoknadsdata soknadsData) {
        try {
            return sendSoknadService.sendSoknad(soknadsData);
        } catch (Exception e) {
            throw new SystemException("Kunne ikke sende søknad", e, "exception.system.baksystem");
        }
    }

    public void avbrytSoknad(String behandlingsId) {
        logger.debug("Avbryt søknad");
        try {
            sendSoknadService.avbrytSoknad(behandlingsId);
        } catch (SOAPFaultException e) {
            logger.error("Kunne ikke avbryte søknad med ID {}", behandlingsId, e);
            throw new SystemException("Kunne ikke avbryte søknad", e, "exception.system.baksystem");
        }
    }

    private WSStartSoknadRequest createXMLStartEttersendingRequest(String fnr, SoknadType type, XMLMetadataListe xmlMetadataListe, String behandlingsId) {
        WSStartSoknadRequest xmlStartSoknadRequest = createXMLStartSoknadRequest(fnr, type, xmlMetadataListe);
            xmlStartSoknadRequest.setBehandlingskjedeId(behandlingsId);
        return xmlStartSoknadRequest;
    }

    private WSStartSoknadRequest createXMLStartSoknadRequest(String fnr, SoknadType soknadType, XMLMetadataListe xmlMetadataListe) {
        WSStartSoknadRequest wsStartSoknadRequest = new WSStartSoknadRequest()
                .withFodselsnummer(fnr)
                .withType(soknadType.name())
                .withAny(xmlMetadataListe);
        wsStartSoknadRequest.setBehandlingskjedeId("");
        return wsStartSoknadRequest;
    }

    private XMLHovedskjema createXMLSkjema(String skjema, String uid) {
        return new XMLHovedskjema().withSkjemanummer(skjema).withUuid(uid).withInnsendingsvalg(IKKE_VALGT.toString());
    }

    public WSHentSoknadResponse hentSoknad(String behandlingsId) {
        return sendSoknadService.hentSoknad(new WSBehandlingsId().withBehandlingsId(behandlingsId));
    }
}