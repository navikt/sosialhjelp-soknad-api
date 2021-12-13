//package no.nav.sosialhjelp.soknad.web.rest.ressurser.okonomi;
//
//import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
//import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
//import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
//import no.nav.security.token.support.core.api.ProtectedWithClaims;
//import no.nav.sosialhjelp.metrics.aspects.Timed;
//import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
//import no.nav.sosialhjelp.soknad.business.util.JsonOkonomiUtils;
//import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg;
//import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
//import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
//import no.nav.sosialhjelp.soknad.web.rest.ressurser.FilFrontend;
//import no.nav.sosialhjelp.soknad.web.rest.ressurser.VedleggFrontend;
//import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
//import org.springframework.stereotype.Controller;
//
//import javax.ws.rs.GET;
//import javax.ws.rs.PUT;
//import javax.ws.rs.Path;
//import javax.ws.rs.PathParam;
//import javax.ws.rs.Produces;
//import javax.xml.bind.annotation.XmlAccessType;
//import javax.xml.bind.annotation.XmlAccessorType;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Predicate;
//import java.util.stream.Collectors;
//
//import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN;
//import static no.nav.sosialhjelp.soknad.business.util.JsonVedleggUtils.getVedleggFromInternalSoknad;
//import static no.nav.sosialhjelp.soknad.domain.Vedleggstatus.VedleggKreves;
//import static no.nav.sosialhjelp.soknad.web.rest.mappers.OkonomiskGruppeMapper.getGruppe;
//import static no.nav.sosialhjelp.soknad.web.rest.mappers.OkonomiskeOpplysningerMapper.addAllFormuerToJsonOkonomi;
//import static no.nav.sosialhjelp.soknad.web.rest.mappers.OkonomiskeOpplysningerMapper.addAllInntekterToJsonOkonomi;
//import static no.nav.sosialhjelp.soknad.web.rest.mappers.OkonomiskeOpplysningerMapper.addAllInntekterToJsonOkonomiUtbetalinger;
//import static no.nav.sosialhjelp.soknad.web.rest.mappers.OkonomiskeOpplysningerMapper.addAllOpplysningUtgifterToJsonOkonomi;
//import static no.nav.sosialhjelp.soknad.web.rest.mappers.OkonomiskeOpplysningerMapper.addAllOversiktUtgifterToJsonOkonomi;
//import static no.nav.sosialhjelp.soknad.web.rest.mappers.OkonomiskeOpplysningerMapper.addAllUtbetalingerToJsonOkonomi;
//import static no.nav.sosialhjelp.soknad.web.rest.mappers.VedleggMapper.mapToVedleggFrontend;
//import static no.nav.sosialhjelp.soknad.web.rest.mappers.VedleggTypeToSoknadTypeMapper.getSoknadPath;
//import static no.nav.sosialhjelp.soknad.web.rest.mappers.VedleggTypeToSoknadTypeMapper.isInSoknadJson;
//import static no.nav.sosialhjelp.soknad.web.rest.mappers.VedleggTypeToSoknadTypeMapper.vedleggTypeToSoknadType;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
//
//@Controller
//@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
//@Path("/soknader/{behandlingsId}/okonomiskeOpplysninger")
//@Timed
//@Produces(APPLICATION_JSON)
//public class OkonomiskeOpplysningerRessurs {
//
//    private final Tilgangskontroll tilgangskontroll;
//    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
//    private final OpplastetVedleggRepository opplastetVedleggRepository;
//
//    public OkonomiskeOpplysningerRessurs(Tilgangskontroll tilgangskontroll, SoknadUnderArbeidRepository soknadUnderArbeidRepository, OpplastetVedleggRepository opplastetVedleggRepository) {
//        this.tilgangskontroll = tilgangskontroll;
//        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
//        this.opplastetVedleggRepository = opplastetVedleggRepository;
//    }
//
//    @GET
//    public VedleggFrontends hentOkonomiskeOpplysninger(@PathParam("behandlingsId") String behandlingsId){
//        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
//        String eier = SubjectHandler.getUserId();
//        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
//        JsonOkonomi jsonOkonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();
//        List<JsonVedlegg> jsonVedleggs = getVedleggFromInternalSoknad(soknad);
//        List<JsonVedlegg> paakrevdeVedlegg = VedleggsforventningMaster.finnPaakrevdeVedlegg(soknad.getJsonInternalSoknad());
//        List<OpplastetVedlegg> opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(soknad.getSoknadId(), soknad.getEier());
//
//        List<VedleggFrontend> slettedeVedlegg = removeIkkePaakrevdeVedlegg(jsonVedleggs, paakrevdeVedlegg, opplastedeVedlegg);
//        addPaakrevdeVedlegg(jsonVedleggs, paakrevdeVedlegg);
//
//        soknad.getJsonInternalSoknad().setVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(jsonVedleggs));
//        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
//
//        return new VedleggFrontends().withOkonomiskeOpplysninger(jsonVedleggs.stream()
//                .map(vedlegg -> mapToVedleggFrontend(vedlegg, jsonOkonomi, opplastedeVedlegg)).collect(Collectors.toList()))
//                .withSlettedeVedlegg(slettedeVedlegg)
//                .withIsOkonomiskeOpplysningerBekreftet(JsonOkonomiUtils.isOkonomiskeOpplysningerBekreftet(jsonOkonomi));
//    }
//
//    @PUT
//    public void updateOkonomiskOpplysning(@PathParam("behandlingsId") String behandlingsId, VedleggFrontend vedleggFrontend) {
//        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
//        final String eier = SubjectHandler.getUserId();
//        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
//        final JsonOkonomi jsonOkonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();
//
//        if (isInSoknadJson(vedleggFrontend.type)){
//            final String soknadType = vedleggTypeToSoknadType.get(vedleggFrontend.type);
//            final String soknadPath = getSoknadPath(vedleggFrontend.type);
//
//            switch (soknadPath) {
//                case "utbetaling":
//                    if(soknadType.equalsIgnoreCase(UTBETALING_HUSBANKEN)) {
//                        addAllInntekterToJsonOkonomiUtbetalinger(vedleggFrontend, jsonOkonomi, UTBETALING_HUSBANKEN);
//                    } else {
//                        addAllUtbetalingerToJsonOkonomi(vedleggFrontend, jsonOkonomi, soknadType);
//                    }
//                    break;
//                case "opplysningerUtgift":
//                    addAllOpplysningUtgifterToJsonOkonomi(vedleggFrontend, jsonOkonomi, soknadType);
//                    break;
//                case "oversiktUtgift":
//                    addAllOversiktUtgifterToJsonOkonomi(vedleggFrontend, jsonOkonomi, soknadType);
//                    break;
//                case "formue":
//                    addAllFormuerToJsonOkonomi(vedleggFrontend, jsonOkonomi, soknadType);
//                    break;
//                case "inntekt":
//                    addAllInntekterToJsonOkonomi(vedleggFrontend, jsonOkonomi, soknadType);
//                    break;
//            }
//        }
//
//        setVedleggStatus(vedleggFrontend, soknad);
//
//        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
//    }
//
//    private List<VedleggFrontend> removeIkkePaakrevdeVedlegg(List<JsonVedlegg> jsonVedleggs, List<JsonVedlegg> paakrevdeVedlegg, List<OpplastetVedlegg> opplastedeVedlegg) {
//        final List<JsonVedlegg> ikkeLengerPaakrevdeVedlegg = jsonVedleggs.stream().filter(isNotInList(paakrevdeVedlegg)).collect(Collectors.toList());
//
//        excludeTypeAnnetAnnetFromList(ikkeLengerPaakrevdeVedlegg);
//
//        jsonVedleggs.removeAll(ikkeLengerPaakrevdeVedlegg);
//
//        final List<VedleggFrontend> slettedeVedlegg = new ArrayList<>();
//
//        for (JsonVedlegg ikkePaakrevdVedlegg : ikkeLengerPaakrevdeVedlegg) {
//            for (OpplastetVedlegg oVedlegg : opplastedeVedlegg) {
//                if (isSameType(ikkePaakrevdVedlegg, oVedlegg)){
//                    opplastetVedleggRepository.slettVedlegg(oVedlegg.getUuid(), oVedlegg.getEier());
//                }
//            }
//
//            if (ikkePaakrevdVedlegg.getFiler() != null && !ikkePaakrevdVedlegg.getFiler().isEmpty()){
//                final String vedleggstype = ikkePaakrevdVedlegg.getType() + "|" + ikkePaakrevdVedlegg.getTilleggsinfo();
//                slettedeVedlegg.add(new VedleggFrontend()
//                        .withType(vedleggstype)
//                        .withGruppe(getGruppe(vedleggstype))
//                        .withFiler(ikkePaakrevdVedlegg.getFiler().stream()
//                                .map(fil -> new FilFrontend().withFilNavn(fil.getFilnavn()))
//                                .collect(Collectors.toList())));
//            }
//        }
//
//        return slettedeVedlegg;
//    }
//
//    private void excludeTypeAnnetAnnetFromList(List<JsonVedlegg> jsonVedleggs) {
//        jsonVedleggs.removeAll(jsonVedleggs.stream()
//                .filter(vedlegg -> vedlegg.getType().equals("annet") &&
//                        vedlegg.getTilleggsinfo().equals("annet")).collect(Collectors.toList()));
//    }
//
//    private boolean isSameType(JsonVedlegg jsonVedlegg, OpplastetVedlegg opplastetVedlegg) {
//        return opplastetVedlegg.getVedleggType().getSammensattType().equals(jsonVedlegg.getType() + "|" + jsonVedlegg.getTilleggsinfo());
//    }
//
//    private void addPaakrevdeVedlegg(List<JsonVedlegg> jsonVedleggs, List<JsonVedlegg> paakrevdeVedlegg) {
//        jsonVedleggs.addAll(paakrevdeVedlegg.stream().filter(isNotInList(jsonVedleggs))
//                .map(jsonVedlegg -> jsonVedlegg.withStatus(VedleggKreves.toString()))
//                .collect(Collectors.toList()));
//    }
//
//    private Predicate<JsonVedlegg> isNotInList(List<JsonVedlegg> jsonVedleggs) {
//        return v -> jsonVedleggs.stream().noneMatch(
//                vedlegg -> vedlegg.getType().equals(v.getType()) &&
//                        vedlegg.getTilleggsinfo().equals(v.getTilleggsinfo())
//        );
//    }
//
//    private void setVedleggStatus(VedleggFrontend vedleggFrontend, SoknadUnderArbeid soknad) {
//        final List<JsonVedlegg> jsonVedleggs = getVedleggFromInternalSoknad(soknad);
//
//        jsonVedleggs.stream().filter(vedlegg -> vedleggFrontend.type.equals(vedlegg.getType() + "|" + vedlegg.getTilleggsinfo()))
//                .findFirst()
//                .orElseThrow(() -> new IllegalStateException("Vedlegget finnes ikke"))
//                .setStatus(vedleggFrontend.vedleggStatus);
//    }
//
//    @SuppressWarnings("WeakerAccess")
//    @XmlAccessorType(XmlAccessType.FIELD)
//    public static final class VedleggFrontends {
//        public List<VedleggFrontend> okonomiskeOpplysninger;
//        public List<VedleggFrontend> slettedeVedlegg;
//        public boolean isOkonomiskeOpplysningerBekreftet;
//
//        public VedleggFrontends withOkonomiskeOpplysninger(List<VedleggFrontend> okonomiskeOpplysninger) {
//            this.okonomiskeOpplysninger = okonomiskeOpplysninger;
//            return this;
//        }
//
//        public VedleggFrontends withSlettedeVedlegg(List<VedleggFrontend> slettedeVedlegg) {
//            this.slettedeVedlegg = slettedeVedlegg;
//            return this;
//        }
//
//        public VedleggFrontends withIsOkonomiskeOpplysningerBekreftet(boolean isOkonomiskeOpplysningerBekreftet) {
//            this.isOkonomiskeOpplysningerBekreftet = isOkonomiskeOpplysningerBekreftet;
//            return this;
//        }
//    }
//}
