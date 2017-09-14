package no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.core.exception.SystemException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.SoknadType;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.henvendelse.HenvendelsePortType;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentHenvendelseRequest;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.meldinger.WSHentHenvendelseResponse;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.*;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.List;

import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.IKKE_VALGT;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.SoknadType.SEND_SOKNAD;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.SoknadType.SEND_SOKNAD_ETTERSENDING;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class HenvendelseService {

    private static final Logger logger = getLogger(HenvendelseService.class);

    @Inject
    @Named("sendSoknadEndpoint")
    private SendSoknadPortType sendSoknadEndpoint;

    @Inject
    @Named("sendSoknadSelftestEndpoint")
    private SendSoknadPortType sendSoknadSelftestEndpoint;

    @Inject
    private HenvendelsePortType henvendelseInformasjonEndpoint;

    public String startSoknad(String fnr, String skjema, String uid) {
        logger.info("Starter søknad");
        return opprettSoknadIHenvendelse(
                lagOpprettSoknadRequest(fnr, SEND_SOKNAD, new XMLMetadataListe().withMetadata(createXMLSkjema(skjema, uid))));
    }

    public String startEttersending(WSHentSoknadResponse soknadResponse) {
        logger.info("Starter ettersending");
        String behandlingskjedeId = optional(soknadResponse.getBehandlingskjedeId()).getOrElse(soknadResponse.getBehandlingsId());

        return opprettSoknadIHenvendelse(
                lagOpprettSoknadRequest(getSubjectHandler().getUid(), SEND_SOKNAD_ETTERSENDING, (XMLMetadataListe) soknadResponse.getAny())
                        .withBehandlingskjedeId(behandlingskjedeId));
    }

    public List<WSBehandlingskjedeElement> hentBehandlingskjede(String behandlingskjedeId) {
        try {
            List<WSBehandlingskjedeElement> wsBehandlingskjedeElementer = sendSoknadEndpoint.hentBehandlingskjede(behandlingskjedeId);
            if (wsBehandlingskjedeElementer.isEmpty()) {
                throw new ApplicationException("Fant ingen behandlinger i en behandlingskjede med behandlingsID " + behandlingskjedeId);
            }
            return wsBehandlingskjedeElementer;
        } catch (SOAPFaultException e) {
            throw new SystemException("Kunne ikke hente behandlingskjede", e, "exception.system.baksystem");
        }
    }

    public void avsluttSoknad(String behandlingsId, XMLHovedskjema hovedskjema, XMLVedlegg... vedlegg) {
        try {
            WSSoknadsdata parameters = new WSSoknadsdata().withBehandlingsId(behandlingsId).withAny(new XMLMetadataListe()
                    .withMetadata(hovedskjema)
                    .withMetadata(vedlegg));
            logger.info("Søknad avsluttet " + behandlingsId + " " + hovedskjema.getSkjemanummer() + " (" + hovedskjema.getJournalforendeEnhet() + ") " + vedlegg.length + " vedlegg");
            sendSoknadEndpoint.sendSoknad(parameters);
        } catch (SOAPFaultException e) {
            throw new SystemException("Kunne ikke sende inn søknad", e, "exception.system.baksystem");
        }
    }

    public WSHentSoknadResponse hentSoknad(String behandlingsId) {
        return sendSoknadEndpoint.hentSoknad(new WSBehandlingsId().withBehandlingsId(behandlingsId));
    }

    public void avbrytSoknad(String behandlingsId) {
        logger.debug("Avbryt søknad");
        try {
            SendSoknadPortType sendSoknadPortType = sendSoknadEndpoint;
            if (getSubjectHandler().getIdentType() == null) {
                sendSoknadPortType = sendSoknadSelftestEndpoint;
                logger.debug("Bruker systembruker for avbrytkall");
            }
            sendSoknadPortType.avbrytSoknad(behandlingsId);
        } catch (SOAPFaultException e) {
            throw new SystemException("Kunne ikke avbryte søknad", e, "exception.system.baksystem");
        }
    }

    private String opprettSoknadIHenvendelse(WSStartSoknadRequest startSoknadRequest) {
        try {
            return sendSoknadEndpoint.startSoknad(startSoknadRequest).getBehandlingsId();
        } catch (SOAPFaultException e) {
            throw new SystemException("Kunne ikke opprette ny søknad", e, "exception.system.baksystem");
        }
    }

    public XMLHenvendelse hentInformasjonOmAvsluttetSoknad(String behandlingsId) {
        WSHentHenvendelseResponse wsHentHenvendelseResponse = henvendelseInformasjonEndpoint.hentHenvendelse(
                new WSHentHenvendelseRequest()
                        .withBehandlingsId(behandlingsId));
        return (XMLHenvendelse) wsHentHenvendelseResponse.getAny();


    }

    private WSStartSoknadRequest lagOpprettSoknadRequest(String fnr, SoknadType soknadType, XMLMetadataListe xmlMetadataListe) {
        return new WSStartSoknadRequest()
                .withFodselsnummer(fnr)
                .withType(soknadType.name())
                .withBehandlingskjedeId("")
                .withAny(xmlMetadataListe);
    }

    private XMLHovedskjema createXMLSkjema(String skjema, String uid) {
        return new XMLHovedskjema()
                .withSkjemanummer(skjema)
                .withUuid(uid)
                .withInnsendingsvalg(IKKE_VALGT.toString());
    }

}