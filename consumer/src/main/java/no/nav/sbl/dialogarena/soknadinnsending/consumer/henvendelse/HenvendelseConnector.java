package no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.modig.core.exception.SystemException;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSSoknadsdata;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.soap.SOAPFaultException;

import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.IKKE_VALGT;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class HenvendelseConnector {

    private static final Logger LOGGER = getLogger(HenvendelseConnector.class);
    private static final String SOKNADINNSENDING = "SEND_SOKNAD";
    @Inject
    @Named("sendSoknadService")
    private SendSoknadPortType sendSoknadService;

    public String hentSoknadEier(Long soknadId) {
        return "";
    }

    public String startSoknad(String fnr, String skjema, String uid) {
        LOGGER.info("Inne i metoden for startSoknad");
        try {
            return sendSoknadService.startSoknad(createXMLStartSoknadRequest(fnr, createXMLSkjema(skjema, uid))).getBehandlingsId();
        } catch (SOAPFaultException e) {
            LOGGER.error("Feil ved start søknad for bruker " + fnr, e);
            throw new SystemException("Kunne ikke opprette ny søknad", e, "exception.system.baksystem");
        }
    }

    public void avsluttSoknad(String behandlingsId, XMLHovedskjema hovedskjema, XMLVedlegg... vedlegg) {
        try {
            WSSoknadsdata parameters = new WSSoknadsdata().withBehandlingsId(behandlingsId).withAny(new XMLMetadataListe()
                    .withMetadata(hovedskjema)
                    .withMetadata(vedlegg));
            sendSoknadService.sendSoknad(parameters);
        } catch (SOAPFaultException e) {
            LOGGER.error("Feil ved innsending av søknad: " + e, e);
            throw new SystemException("Kunne ikke opprette ny søknad", e, "exception.system.baksystem");
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
        LOGGER.debug("Avbryt søknad");
        try {
            sendSoknadService.avbrytSoknad(behandlingsId);
        } catch (SOAPFaultException e) {
            LOGGER.error("Kunne ikke avbryte søknad med ID {}", behandlingsId, e);
            throw new SystemException("Kunne ikke avbryte søknad", e, "exception.system.baksystem");
        }
    }

    private WSStartSoknadRequest createXMLStartSoknadRequest(String fnr, XMLHovedskjema skjema) {
        return new WSStartSoknadRequest()
                .withFodselsnummer(fnr)
                .withType(SOKNADINNSENDING)
                .withAny(new XMLMetadataListe()
                        .withMetadata(skjema));
    }

    private XMLHovedskjema createXMLSkjema(String skjema, String uid) {
        return new XMLHovedskjema().withSkjemanummer(skjema).withUuid(uid).withInnsendingsvalg(IKKE_VALGT.toString());
    }

    public WSHentSoknadResponse hentSoknad(String behandlingsId) {
        return sendSoknadService.hentSoknad(new WSBehandlingsId().withBehandlingsId(behandlingsId));
    }
}