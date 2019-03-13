package no.nav.sbl.dialogarena.rest.ressurser.okonomi;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.mappers.OkonomiskeOpplysningerMapper;
import no.nav.sbl.dialogarena.rest.mappers.SoknadTypeAndPath;
import no.nav.sbl.dialogarena.rest.ressurser.FilFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggRadFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.midlertidig.VedleggConverter;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
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

    @Inject
    private VedleggService vedleggService;

    @Inject
    private VedleggConverter vedleggConverter;

    @Inject
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @GET
    public VedleggFrontends hentOkonomiskeOpplysninger(@PathParam("behandlingsId") String behandlingsId){
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final SoknadUnderArbeid soknad = legacyHelper.hentSoknad(behandlingsId, eier);
        final JsonOkonomi jsonOkonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();
        final List<JsonVedlegg> jsonVedleggs = soknad.getJsonInternalSoknad().getVedlegg() == null ? new ArrayList<>() :
                soknad.getJsonInternalSoknad().getVedlegg().getVedlegg() == null ? new ArrayList<>() :
                        soknad.getJsonInternalSoknad().getVedlegg().getVedlegg();
        final List<JsonVedlegg> paakrevdeVedlegg = VedleggsforventningMaster.finnPaakrevdeVedlegg(soknad.getJsonInternalSoknad());

        final List<OpplastetVedlegg> opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(soknad.getSoknadId(), eier);

        final List<VedleggFrontend> slettedeVedlegg = removeIkkePaakrevdeVedlegg(jsonVedleggs, paakrevdeVedlegg, opplastedeVedlegg);
        addPaakrevdeVedlegg(jsonVedleggs, paakrevdeVedlegg);

        final SoknadUnderArbeid utenFaktumSoknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        utenFaktumSoknad.getJsonInternalSoknad().setVedlegg(jsonVedleggs.isEmpty() ? null : new JsonVedleggSpesifikasjon().withVedlegg(jsonVedleggs));
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        soknadUnderArbeidRepository.oppdaterSoknadsdata(utenFaktumSoknad, eier);

        return new VedleggFrontends().withOkonomiskeOpplysninger(jsonVedleggs.stream()
                .map(vedlegg -> mapper.mapToVedleggFrontend(vedlegg, jsonOkonomi, opplastedeVedlegg)).collect(Collectors.toList()))
                .withSlettedeVedlegg(slettedeVedlegg);
    }

    private List<OpplastetVedlegg> legacyMapVedleggToOpplastetVedlegg(@PathParam("behandlingsId") String behandlingsId, String eier, SoknadUnderArbeid soknad) {
        final WebSoknad webSoknad = legacyHelper.hentWebSoknad(behandlingsId, eier);
        final List<Vedlegg> vedleggListe = vedleggService.hentVedleggOgKvittering(webSoknad);
        return vedleggConverter.mapVedleggListeTilOpplastetVedleggListe(webSoknad.getSoknadId(), soknad.getEier(), vedleggListe);
    }

    @PUT
    public void updateOkonomiskOpplysning(@PathParam("behandlingsId") String behandlingsId, VedleggFrontend vedleggFrontend){
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, vedleggFrontend);
        legacyUpdate(behandlingsId, vedleggFrontend);
    }

    private void update(String behandlingsId, VedleggFrontend vedleggFrontend) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonOkonomi jsonOkonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();
        final String type = vedleggFrontend.type.substring(0, vedleggFrontend.type.indexOf("|"));
        final String tilleggsinfo = vedleggFrontend.type.substring(vedleggFrontend.type.indexOf("|") + 1);

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

        setVedleggStatus(vedleggFrontend, soknad, type, tilleggsinfo);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, VedleggFrontend vedleggFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, true, false);

        final String type = vedleggFrontend.type.substring(0, vedleggFrontend.type.indexOf("|"));
        final String tilleggsinfo = vedleggFrontend.type.substring(vedleggFrontend.type.indexOf("|") + 1);

        final SoknadTypeAndPath soknadTypeAndPath = mapVedleggTypeToSoknadTypeAndPath(type, tilleggsinfo);
        final String jsonType = soknadTypeAndPath.getType();
        String key = jsonTypeToFaktumKey.get(jsonType);
        String belopNavn = jsonTypeToBelopNavn.get(jsonType);

        if (key == null){
            if (soknadTypeAndPath.getType().equals("annen") && soknadTypeAndPath.getPath().equals("opplysningerUtgift")){
                key = "opplysninger.ekstrainfo.utgifter";
                belopNavn = "utgift";
            } else if (soknadTypeAndPath.getType().equals("annen") && soknadTypeAndPath.getPath().equals("utbetaling")){
                key = "opplysninger.inntekt.inntekter.annet";
                belopNavn = "sum";
            }
        }

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

    private List<VedleggFrontend> removeIkkePaakrevdeVedlegg(List<JsonVedlegg> jsonVedleggs, List<JsonVedlegg> paakrevdeVedlegg, List<OpplastetVedlegg> opplastedeVedlegg) {
        final List<JsonVedlegg> ikkeLengerPaakrevdeVedlegg = jsonVedleggs.stream().filter(vedlegg -> paakrevdeVedlegg.stream().noneMatch(
                pVedlegg -> vedlegg.getType().equals(pVedlegg.getType()) && vedlegg.getTilleggsinfo().equals(pVedlegg.getTilleggsinfo())
        )).collect(Collectors.toList());

        ikkeLengerPaakrevdeVedlegg.removeAll(ikkeLengerPaakrevdeVedlegg.stream()
                .filter(vedlegg -> vedlegg.getType().equals("annet") && vedlegg.getTilleggsinfo().equals("annet")).collect(Collectors.toList()));

        jsonVedleggs.removeAll(ikkeLengerPaakrevdeVedlegg);

        final List<VedleggFrontend> slettedeVedlegg = new ArrayList<>();

        for (JsonVedlegg ikkePaakrevdVedlegg : ikkeLengerPaakrevdeVedlegg) {
            for (OpplastetVedlegg oVedlegg : opplastedeVedlegg) {
                if (oVedlegg.getVedleggType().getType().equals(ikkePaakrevdVedlegg.getType())
                        && oVedlegg.getVedleggType().getTilleggsinfo().equals(ikkePaakrevdVedlegg.getTilleggsinfo())){
                    opplastetVedleggRepository.slettVedlegg(oVedlegg.getUuid(), oVedlegg.getEier());
                }
            }

            if (ikkePaakrevdVedlegg.getFiler() != null && !ikkePaakrevdVedlegg.getFiler().isEmpty()){
                slettedeVedlegg.add(new VedleggFrontend()
                        .withType(ikkePaakrevdVedlegg.getType() + "|" + ikkePaakrevdVedlegg.getTilleggsinfo())
                        .withFiler(ikkePaakrevdVedlegg.getFiler().stream()
                                .map(fil -> new FilFrontend().withFilNavn(fil.getFilnavn()))
                                .collect(Collectors.toList())));
            }
        }

        return slettedeVedlegg;
    }

    private void addPaakrevdeVedlegg(List<JsonVedlegg> jsonVedleggs, List<JsonVedlegg> paakrevdeVedlegg) {
        jsonVedleggs.addAll(paakrevdeVedlegg.stream().filter(pVedlegg -> jsonVedleggs.stream().noneMatch(
                vedlegg -> vedlegg.getType().equals(pVedlegg.getType()) && vedlegg.getTilleggsinfo().equals(pVedlegg.getTilleggsinfo())
        )).collect(Collectors.toList()));
    }

    private void setVedleggStatus(VedleggFrontend vedleggFrontend, SoknadUnderArbeid soknad, String type, String tilleggsinfo) {
        final List<JsonVedlegg> jsonVedleggs = soknad.getJsonInternalSoknad().getVedlegg() == null ? new ArrayList<>() :
                soknad.getJsonInternalSoknad().getVedlegg().getVedlegg() == null ? new ArrayList<>() :
                        soknad.getJsonInternalSoknad().getVedlegg().getVedlegg();

        jsonVedleggs.stream().filter(vedlegg -> vedlegg.getType().equals(type) && vedlegg.getTilleggsinfo().equals(tilleggsinfo))
                .findFirst().get().setStatus(vedleggFrontend.vedleggStatus);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class VedleggFrontends {
        public List<VedleggFrontend> okonomiskeOpplysninger;
        public List<VedleggFrontend> slettedeVedlegg;

        public VedleggFrontends withOkonomiskeOpplysninger(List<VedleggFrontend> okonomiskeOpplysninger) {
            this.okonomiskeOpplysninger = okonomiskeOpplysninger;
            return this;
        }

        public VedleggFrontends withSlettedeVedlegg(List<VedleggFrontend> slettedeVedlegg) {
            this.slettedeVedlegg = slettedeVedlegg;
            return this;
        }
    }
}
