package no.nav.sosialhjelp.soknad.web.rest.actions;

import no.finn.unleash.Unleash;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.sosialhjelp.metrics.aspects.Timed;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.business.exceptions.SendingTilKommuneErIkkeAktivertException;
import no.nav.sosialhjelp.soknad.business.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException;
import no.nav.sosialhjelp.soknad.business.exceptions.SendingTilKommuneUtilgjengeligException;
import no.nav.sosialhjelp.soknad.business.exceptions.SoknadenHarNedetidException;
import no.nav.sosialhjelp.soknad.business.service.digisosapi.DigisosApiService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService;
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneInfoService;
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneStatus;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper;
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import no.nav.sosialhjelp.soknad.web.utils.NedetidUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils.FEATURE_UTVIDE_VEDLEGGJSON;
import static no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils.addHendelseTypeAndHendelseReferanse;
import static no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API;
import static no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils.isAlltidSendTilNavTestkommune;
import static no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils.isSendingTilFiksEnabled;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
import static no.nav.sosialhjelp.soknad.web.utils.NedetidUtils.NEDETID_SLUTT;
import static no.nav.sosialhjelp.soknad.web.utils.NedetidUtils.getNedetidAsStringOrNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Controller
@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
@Path("/soknader/{behandlingsId}/actions")
@Produces(APPLICATION_JSON)
@Timed(name = "SoknadActionsRessurs")
public class SoknadActions {

    private static final Logger log = getLogger(SoknadActions.class);
    private static final String SVARUT = "SVARUT";
    private static final String FIKS_DIGISOS_API = "FIKS_DIGISOS_API";

    @Inject
    private SoknadService soknadService;

    @Inject
    private KommuneInfoService kommuneInfoService;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;

    @Inject
    private DigisosApiService digisosApiService;

    @Inject
    private Unleash unleash;

    @POST
    @Path("/send")
    public SendTilUrlFrontend sendSoknad(@PathParam("behandlingsId") String behandlingsId, @Context ServletContext servletContext, @HeaderParam(value = AUTHORIZATION) String token) {
        if (NedetidUtils.isInnenforNedetid()) {
            throw new SoknadenHarNedetidException(String.format("Soknaden har nedetid fram til %s ", getNedetidAsStringOrNull(NEDETID_SLUTT)));
        }

        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = SubjectHandler.getUserId();

        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        if (soknadUnderArbeid == null) throw new IllegalStateException(String.format("SoknadUnderArbeid er null ved sending for behandlingsId %s", behandlingsId));

        updateVedleggJsonWithHendelseTypeAndHendelseReferanse(eier, soknadUnderArbeid);

        if (!isSendingTilFiksEnabled()
                || isEttersendelsePaSoknadSendtViaSvarUt(soknadUnderArbeid)) {
            log.info("BehandlingsId {} sendes til SvarUt.", behandlingsId);
            soknadService.sendSoknad(behandlingsId);
            return new SendTilUrlFrontend().withSendtTil(SVARUT).withId(behandlingsId);
        }
        if (soknadUnderArbeid.erEttersendelse()) {
            log.error("Ettersendelse {} blir forsøkt sendt med soknad-api selv om den tiknyttede søknaden ble sendt til Fiks-Digisos-api. Dette skal ikke skje, disse skal sendes via innsyn-api.", behandlingsId);
            throw new IllegalStateException("Ettersendelse på søknad sendt via fiks-digisos-api skal sendes via innsyn-api");
        }

        log.info("BehandlingsId {} sendes til SvarUt eller fiks-digisos-api avhengig av kommuneinfo.", behandlingsId);
        String kommunenummer = getKommunenummerOrMock(soknadUnderArbeid);
        KommuneStatus kommuneStatus = kommuneInfoService.kommuneInfo(kommunenummer);
        log.info("Kommune: {} Status: {}", kommunenummer, kommuneStatus);

        switch (kommuneStatus) {
            case FIKS_NEDETID_OG_TOM_CACHE:
                throw new SendingTilKommuneUtilgjengeligException(String.format("Sending til kommune %s er ikke tilgjengelig fordi fiks har nedetid og kommuneinfo-cache er tom.", kommunenummer));
            case MANGLER_KONFIGURASJON:
            case HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT:
                if (!KommuneTilNavEnhetMapper.getDigisoskommuner().contains(kommunenummer)) {
                    throw new SendingTilKommuneErIkkeAktivertException(String.format("Sending til kommune %s er ikke aktivert og kommunen er ikke i listen over svarUt-kommuner", kommunenummer));
                }
                log.info("BehandlingsId {} sendes til SvarUt (sfa. Fiks-konfigurasjon).", behandlingsId);
                soknadService.sendSoknad(behandlingsId);
                return new SendTilUrlFrontend().withSendtTil(SVARUT).withId(behandlingsId);
            case SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA:
                log.info("BehandlingsId {} sendes til Fiks-digisos-api (sfa. Fiks-konfigurasjon).", behandlingsId);
                String digisosId = digisosApiService.sendSoknad(soknadUnderArbeid, token, kommunenummer);
                return new SendTilUrlFrontend().withSendtTil(FIKS_DIGISOS_API).withId(digisosId);
            case SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER:
                throw new SendingTilKommuneErMidlertidigUtilgjengeligException(String.format("Sending til kommune %s er midlertidig utilgjengelig.", kommunenummer));
            default:
                throw new SendingTilKommuneErMidlertidigUtilgjengeligException(String.format("Det mangler håndtering av case %s for kommunens konfigurasjon. Sending til kommune %s er midlertidig utilgjengelig.", kommuneStatus.name(), kommunenummer));
        }
    }

    private void updateVedleggJsonWithHendelseTypeAndHendelseReferanse(String eier, SoknadUnderArbeid soknadUnderArbeid) {
        JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon = soknadUnderArbeid.getJsonInternalSoknad().getVedlegg();
        boolean isUtvideVedleggJsonFeatureActive = unleash.isEnabled(FEATURE_UTVIDE_VEDLEGGJSON, false);

        addHendelseTypeAndHendelseReferanse(jsonVedleggSpesifikasjon, !soknadUnderArbeid.erEttersendelse(), isUtvideVedleggJsonFeatureActive);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier);
    }

    String getKommunenummerOrMock(SoknadUnderArbeid soknadUnderArbeid) {
        if (ServiceUtils.isNonProduction() && isAlltidSendTilNavTestkommune()) {
            log.error("Sender til Nav-testkommune (3002). Du skal aldri se denne meldingen i PROD");
            return "3002";
        } else {
            return soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getMottaker().getKommunenummer();
        }
    }

    private boolean isEttersendelsePaSoknadSendtViaSvarUt(SoknadUnderArbeid soknadUnderArbeid) {
        if (!soknadUnderArbeid.erEttersendelse()) return false;

        SoknadMetadata soknadensMetadata = soknadMetadataRepository.hent(soknadUnderArbeid.getTilknyttetBehandlingsId());
        return soknadensMetadata != null && soknadensMetadata.status != SENDT_MED_DIGISOS_API;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class SendTilUrlFrontend {
        public String sendtTil;
        public String id;

        public SendTilUrlFrontend withSendtTil(String sendtTil) {
            this.sendtTil = sendtTil;
            return this;
        }

        public SendTilUrlFrontend withId(String id) {
            this.id = id;
            return this;
        }
    }
}
