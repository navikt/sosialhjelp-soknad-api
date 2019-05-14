package no.nav.sbl.dialogarena.rest.ressurser.okonomi;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.rest.ressurser.FilFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggFrontend;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggRadFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.OpplastetVedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.rest.mappers.OkonomiskGruppeMapper.getGruppe;
import static no.nav.sbl.dialogarena.rest.mappers.OkonomiskeOpplysningerMapper.*;
import static no.nav.sbl.dialogarena.rest.mappers.VedleggMapper.mapToVedleggFrontend;
import static no.nav.sbl.dialogarena.rest.mappers.VedleggTypeToSoknadTypeMapper.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.FaktumNoklerOgBelopNavnMapper.soknadTypeToBelopNavn;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.FaktumNoklerOgBelopNavnMapper.soknadTypeToFaktumKey;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.JsonVedleggUtils.getVedleggFromInternalSoknad;
import static no.nav.sbl.sosialhjelp.domain.Vedleggstatus.Status.VedleggKreves;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
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
    @Named("soknadInnsendingRepository")
    private SoknadRepository repository;

    @Inject
    private FaktaService faktaService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @Inject
    private OpplastetVedleggService opplastetVedleggService;

    @GET
    public VedleggFrontends hentOkonomiskeOpplysninger(@PathParam("behandlingsId") String behandlingsId){
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);

        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknad = legacyHelper.hentSoknad(behandlingsId, eier, true);
        final JsonOkonomi jsonOkonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();
        final List<JsonVedlegg> jsonVedleggs = getVedleggFromInternalSoknad(soknad);
        final List<JsonVedlegg> paakrevdeVedlegg = VedleggsforventningMaster.finnPaakrevdeVedlegg(soknad.getJsonInternalSoknad());

        final SoknadUnderArbeid utenFaktumSoknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();

        legacyConvertVedleggToOpplastetVedleggAndUploadToRepository(behandlingsId, eier, utenFaktumSoknad);

        final List<OpplastetVedlegg> newModelOpplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(utenFaktumSoknad.getSoknadId(), utenFaktumSoknad.getEier());

        final List<VedleggFrontend> slettedeVedlegg = removeIkkePaakrevdeVedlegg(jsonVedleggs, paakrevdeVedlegg, newModelOpplastedeVedlegg);
        addPaakrevdeVedlegg(jsonVedleggs, paakrevdeVedlegg);

        utenFaktumSoknad.getJsonInternalSoknad().setVedlegg(jsonVedleggs.isEmpty() ? null : new JsonVedleggSpesifikasjon().withVedlegg(jsonVedleggs));
        soknadUnderArbeidRepository.oppdaterSoknadsdata(utenFaktumSoknad, eier);

        return new VedleggFrontends().withOkonomiskeOpplysninger(jsonVedleggs.stream()
                .map(vedlegg -> mapToVedleggFrontend(vedlegg, jsonOkonomi, newModelOpplastedeVedlegg)).collect(Collectors.toList()))
                .withSlettedeVedlegg(slettedeVedlegg);
    }

    private void legacyConvertVedleggToOpplastetVedleggAndUploadToRepository(String behandlingsId, String eier, SoknadUnderArbeid utenFaktumSoknad) {
        final List<OpplastetVedlegg> opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(utenFaktumSoknad.getSoknadId(), utenFaktumSoknad.getEier());

        if (opplastedeVedlegg == null || opplastedeVedlegg.isEmpty()) {
            final List<OpplastetVedlegg> konvertertOpplastedeVedlegg = opplastetVedleggService.legacyMapVedleggToOpplastetVedlegg(behandlingsId, eier, utenFaktumSoknad.getSoknadId());
            if (konvertertOpplastedeVedlegg != null) {
                for (OpplastetVedlegg opplastetVedlegg : konvertertOpplastedeVedlegg) {
                    opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, utenFaktumSoknad.getEier());
                }
            }
        }
    }

    @PUT
    public void updateOkonomiskOpplysning(@PathParam("behandlingsId") String behandlingsId, VedleggFrontend vedleggFrontend) throws Exception {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, vedleggFrontend);
        legacyUpdate(behandlingsId, vedleggFrontend);
    }

    private void update(String behandlingsId, VedleggFrontend vedleggFrontend) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonOkonomi jsonOkonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();

        if (isInSoknadJson(vedleggFrontend.type)){
            final String soknadType = vedleggTypeToSoknadType.get(vedleggFrontend.type);
            final String soknadPath = getSoknadPath(vedleggFrontend.type);

            switch (soknadPath) {
                case "utbetaling":
                    addAllUtbetalingerToJsonOkonomi(vedleggFrontend, jsonOkonomi, soknadType);
                    break;
                case "opplysningerUtgift":
                    addAllOpplysningUtgifterToJsonOkonomi(vedleggFrontend, jsonOkonomi, soknadType);
                    break;
                case "oversiktUtgift":
                    addAllOversiktUtgifterToJsonOkonomi(vedleggFrontend, jsonOkonomi, soknadType);
                    break;
                case "formue":
                    addAllFormuerToJsonOkonomi(vedleggFrontend, jsonOkonomi, soknadType);
                    break;
                case "inntekt":
                    addAllInntekterToJsonOkonomi(vedleggFrontend, jsonOkonomi, soknadType);
                    break;
            }
        }

        setVedleggStatus(vedleggFrontend, soknad);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, VedleggFrontend vedleggFrontend) throws Exception {
        if (!isInSoknadJson(vedleggFrontend.type)){
            return;
        }
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, true, false);

        final String soknadType = vedleggTypeToSoknadType.get(vedleggFrontend.type);
        final String soknadPath = getSoknadPath(vedleggFrontend.type);
        String key = soknadTypeToFaktumKey.get(soknadType);
        String belopNavn = soknadTypeToBelopNavn.get(soknadType);

        if (key == null){
            if (soknadType.equals("annen") && soknadPath.equals("opplysningerUtgift")){
                key = "opplysninger.ekstrainfo.utgifter";
                belopNavn = "utgift";
            } else if (soknadType.equals("annen") && soknadPath.equals("utbetaling")){
                key = "opplysninger.inntekt.inntekter.annet";
                belopNavn = "sum";
            } else if (soknadType.equals("barnebidrag") && soknadPath.equals("inntekt")){
                key = "opplysninger.familiesituasjon.barnebidrag.mottar";
                belopNavn = "mottar";
            } else if (soknadType.equals("barnebidrag") && soknadPath.equals("oversiktUtgift")){
                key = "opplysninger.familiesituasjon.barnebidrag.betaler";
                belopNavn = "betaler";
            }
        }

        final List<Faktum> fakta = webSoknad.getFaktaMedKey(key);

        if (vedleggFrontend.type.equals("annet|annet") && checkIfTypeAnnetAnnetShouldBeRemoved(vedleggFrontend)){
            vedleggFrontend.rader = Collections.emptyList();
        }
        makeFaktumListEqualSizeToFrontendRader(vedleggFrontend, fakta, webSoknad);

        for (int i = 0; i < vedleggFrontend.rader.size(); i++){
            Faktum faktum = fakta.get(i);
            final VedleggRadFrontend vedleggRad = vedleggFrontend.rader.get(i);
            final Map<String, String> properties = faktum.getProperties();
            if (vedleggFrontend.type.equals("nedbetalingsplan|avdraglaan")){
                properties.put("avdrag", vedleggRad.avdrag != null ? vedleggRad.avdrag.toString() : null);
                properties.put("renter", vedleggRad.renter != null ? vedleggRad.renter.toString() : null);
            } else if (vedleggFrontend.type.equals("lonnslipp|arbeid")){
                properties.put("bruttolonn", vedleggRad.brutto != null ? vedleggRad.brutto.toString() : null);
                properties.put("nettolonn", vedleggRad.netto != null ? vedleggRad.netto.toString() : null);
            } else {
                properties.put(belopNavn, vedleggRad.belop != null ? vedleggRad.belop.toString() : null);
            }

            putBeskrivelseOnRelevantTypes(soknadPath, soknadType, vedleggRad, properties);

            faktaService.lagreBrukerFaktum(faktum);
        }
    }

    private void makeFaktumListEqualSizeToFrontendRader(VedleggFrontend vedleggFrontend, List<Faktum> fakta, WebSoknad webSoknad) throws Exception {
        final int sizeDiff = vedleggFrontend.rader.size() - fakta.size();
        if (sizeDiff > 0){
            Iterator<Long> faktumIder = repository.hentLedigeFaktumIder(sizeDiff).iterator();
            try{
                for (int i = 0; i < sizeDiff; i++){
                    final Faktum faktum = new Faktum()
                            .medFaktumId(faktumIder.next())
                            .medParrentFaktumId(fakta.get(0).getParrentFaktum())
                            .medKey(fakta.get(0).getKey())
                            .medType(BRUKERREGISTRERT)
                            .medSoknadId(fakta.get(0).getSoknadId());
                    faktaService.opprettBrukerFaktum(webSoknad.getBrukerBehandlingId(), faktum);
                    fakta.add(faktum);
                }
            } catch (Exception e){
                throw new Exception("makeEqualSize feilet ved type: " + vedleggFrontend.type, e);
            }
        } else if (sizeDiff < 0){
            for (int i = 0; i < -sizeDiff; i++){
                faktaService.slettBrukerFaktum(fakta.get(fakta.size() - 1).getFaktumId());
                fakta.remove(fakta.size() - 1);
            }
        }
        if (vedleggFrontend.type.equals("annet|annet") && vedleggFrontend.rader.size() == 0){
            final Faktum annetParrentFaktum = webSoknad.getFaktumMedKey("opplysninger.ekstrainfo");
            Iterator<Long> faktumIder = repository.hentLedigeFaktumIder(sizeDiff).iterator();
            Faktum faktum = new Faktum()
                    .medFaktumId(faktumIder.next())
                    .medParrentFaktumId(annetParrentFaktum.getFaktumId())
                    .medKey("opplysninger.ekstrainfo.utgifter")
                    .medType(BRUKERREGISTRERT)
                    .medSoknadId(annetParrentFaktum.getSoknadId());
            faktaService.opprettBrukerFaktum(webSoknad.getBrukerBehandlingId(), faktum);
            fakta.add(faktum);
        }
    }

    private List<VedleggFrontend> removeIkkePaakrevdeVedlegg(List<JsonVedlegg> jsonVedleggs, List<JsonVedlegg> paakrevdeVedlegg, List<OpplastetVedlegg> opplastedeVedlegg) {
        final List<JsonVedlegg> ikkeLengerPaakrevdeVedlegg = jsonVedleggs.stream().filter(isNotInList(paakrevdeVedlegg)).collect(Collectors.toList());

        excludeTypeAnnetAnnetFromList(ikkeLengerPaakrevdeVedlegg);

        jsonVedleggs.removeAll(ikkeLengerPaakrevdeVedlegg);

        final List<VedleggFrontend> slettedeVedlegg = new ArrayList<>();

        for (JsonVedlegg ikkePaakrevdVedlegg : ikkeLengerPaakrevdeVedlegg) {
            for (OpplastetVedlegg oVedlegg : opplastedeVedlegg) {
                if (isSameType(ikkePaakrevdVedlegg, oVedlegg)){
                    opplastetVedleggRepository.slettVedlegg(oVedlegg.getUuid(), oVedlegg.getEier());
                }
            }

            if (ikkePaakrevdVedlegg.getFiler() != null && !ikkePaakrevdVedlegg.getFiler().isEmpty()){
                final String vedleggstype = ikkePaakrevdVedlegg.getType() + "|" + ikkePaakrevdVedlegg.getTilleggsinfo();
                slettedeVedlegg.add(new VedleggFrontend()
                        .withType(vedleggstype)
                        .withGruppe(getGruppe(vedleggstype))
                        .withFiler(ikkePaakrevdVedlegg.getFiler().stream()
                                .map(fil -> new FilFrontend().withFilNavn(fil.getFilnavn()))
                                .collect(Collectors.toList())));
            }
        }

        return slettedeVedlegg;
    }

    private void excludeTypeAnnetAnnetFromList(List<JsonVedlegg> jsonVedleggs) {
        jsonVedleggs.removeAll(jsonVedleggs.stream()
                .filter(vedlegg -> vedlegg.getType().equals("annet") &&
                        vedlegg.getTilleggsinfo().equals("annet")).collect(Collectors.toList()));
    }

    private boolean isSameType(JsonVedlegg jsonVedlegg, OpplastetVedlegg opplastetVedlegg) {
        return opplastetVedlegg.getVedleggType().getSammensattType().equals(jsonVedlegg.getType() + "|" + jsonVedlegg.getTilleggsinfo());
    }

    private void addPaakrevdeVedlegg(List<JsonVedlegg> jsonVedleggs, List<JsonVedlegg> paakrevdeVedlegg) {
        jsonVedleggs.addAll(paakrevdeVedlegg.stream().filter(isNotInList(jsonVedleggs))
                .map(jsonVedlegg -> jsonVedlegg.withStatus(VedleggKreves.toString()))
                .collect(Collectors.toList()));
    }

    private Predicate<JsonVedlegg> isNotInList(List<JsonVedlegg> jsonVedleggs) {
        return v -> jsonVedleggs.stream().noneMatch(
                vedlegg -> vedlegg.getType().equals(v.getType()) &&
                        vedlegg.getTilleggsinfo().equals(v.getTilleggsinfo())
        );
    }

    private void setVedleggStatus(VedleggFrontend vedleggFrontend, SoknadUnderArbeid soknad) {
        final List<JsonVedlegg> jsonVedleggs = getVedleggFromInternalSoknad(soknad);

        jsonVedleggs.stream().filter(vedlegg -> vedleggFrontend.type.equals(vedlegg.getType() + "|" + vedlegg.getTilleggsinfo()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Vedlegget finnes ikke"))
                .setStatus(vedleggFrontend.vedleggStatus);
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
