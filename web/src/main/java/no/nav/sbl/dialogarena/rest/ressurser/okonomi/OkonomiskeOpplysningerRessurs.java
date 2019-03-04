package no.nav.sbl.dialogarena.rest.ressurser.okonomi;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.mappers.OkonomiskeOpplysningerMapper;
import no.nav.sbl.dialogarena.rest.mappers.SoknadTypeAndPath;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggRadFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.rest.mappers.FaktumNoklerOgBelopNavnMapper.jsonTypeToBelopNavn;
import static no.nav.sbl.dialogarena.rest.mappers.FaktumNoklerOgBelopNavnMapper.jsonTypeToFaktumKey;
import static no.nav.sbl.dialogarena.rest.mappers.SoknadTypeToVedleggTypeMapper.mapVedleggTypeToSoknadTypeAndPath;

@Controller
@Path("/soknader/{behandlingsId}/okonomiskeOpplysninger")
@Timed
@Produces(APPLICATION_JSON)
public class OkonomiskeOpplysningerRessurs {

    @Inject
    private LegacyHelper legacyHelper;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadService soknadService;

    @Inject
    private FaktaService faktaService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private OkonomiskeOpplysningerMapper mapper;

    @GET
    public VedleggFrontends hentOkonomiskeOpplysninger(@PathParam("behandlingsId") String behandlingsId){
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        final List<JsonVedlegg> jsonVedleggs = soknad.getVedlegg().getVedlegg();
        final JsonOkonomi jsonOkonomi = soknad.getSoknad().getData().getOkonomi();

        if (jsonVedleggs != null && !jsonVedleggs.isEmpty()){
            return new VedleggFrontends().withOkonomiskeOpplysninger(jsonVedleggs.stream()
                    .map(vedlegg -> mapper.mapToVedleggFrontend(vedlegg, jsonOkonomi)).collect(Collectors.toList()));
        }
        return new VedleggFrontends();
    }

    @PUT
    public void updateOkonomiskOpplysning(@PathParam("behandlingsId") String behandlingsId, VedleggFrontend vedleggFrontend){
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
//        update(behandlingsId, vedleggFrontend);
        legacyUpdate(behandlingsId, vedleggFrontend);
    }

    private void update(String behandlingsId, VedleggFrontend vedleggFrontend) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonOkonomi jsonOkonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();
        final String type = vedleggFrontend.type.substring(0, vedleggFrontend.type.indexOf("."));
        final String tilleggsinfo = vedleggFrontend.type.substring(vedleggFrontend.type.indexOf(".") + 1);

        final SoknadTypeAndPath soknadTypeAndPath = mapVedleggTypeToSoknadTypeAndPath(type, tilleggsinfo);
        final String jsonType = soknadTypeAndPath.getType();

        switch (soknadTypeAndPath.getPath()){
            case "utbetaling":
                mapper.addAllUtbetalingerToJsonOkonomi(vedleggFrontend, jsonOkonomi, jsonType);
                break;
            case "opplysningerUtgift":
                mapper.addAllOpplysningUtgifterToJsonOkonomi(vedleggFrontend, jsonOkonomi, jsonType);
                break;
            case "oversiktUtgift":
                mapper.addAllOversiktUtgifterToJsonOkonomi(vedleggFrontend, jsonOkonomi, jsonType);
                break;
            case "formue":
                mapper.addAllFormuerToJsonOkonomi(vedleggFrontend, jsonOkonomi, jsonType);
                break;
            case "inntekt":
                mapper.addAllInntekterToJsonOkonomi(vedleggFrontend, jsonOkonomi, jsonType);
                break;
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, VedleggFrontend vedleggFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, true, false);

        final String type = vedleggFrontend.type.substring(0, vedleggFrontend.type.indexOf("."));
        final String tilleggsinfo = vedleggFrontend.type.substring(vedleggFrontend.type.indexOf(".") + 1);

        final SoknadTypeAndPath soknadTypeAndPath = mapVedleggTypeToSoknadTypeAndPath(type, tilleggsinfo);
        final String jsonType = soknadTypeAndPath.getType();
        final String key = jsonTypeToFaktumKey.get(jsonType);
        final String belopNavn = jsonTypeToBelopNavn.get(jsonType);

        final List<Faktum> fakta = webSoknad.getFaktaMedKey(key);

        mapper.makeFaktumListEqualSizeToFrontendRader(vedleggFrontend, fakta, webSoknad.getBrukerBehandlingId());

        for (int i = 0; i < vedleggFrontend.rader.size(); i++){
            Faktum faktum = fakta.get(i);
            final VedleggRadFrontend vedleggRad = vedleggFrontend.rader.get(i);
            final Map<String, String> properties = faktum.getProperties();
            if (type.equals("nedbetalingsplan") && tilleggsinfo.equals("avdraglaan")){
                properties.put("avdrag", vedleggRad.avdrag.toString());
                properties.put("renter", vedleggRad.renter.toString());
            } else {
                properties.put(belopNavn, vedleggRad.belop.toString());
            }

            mapper.putNettolonnOnPropertiesForJsonTypeJobb(belopNavn, vedleggRad, properties);
            mapper.putBeskrivelseOnRelevantTypes(soknadTypeAndPath, jsonType, vedleggRad, properties);

            faktaService.lagreBrukerFaktum(faktum);
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class VedleggFrontends {
        public List<VedleggFrontend> okonomiskeOpplysninger;

        public VedleggFrontends withOkonomiskeOpplysninger(List<VedleggFrontend> okonomiskeOpplysninger) {
            this.okonomiskeOpplysninger = okonomiskeOpplysninger;
            return this;
        }
    }
}
