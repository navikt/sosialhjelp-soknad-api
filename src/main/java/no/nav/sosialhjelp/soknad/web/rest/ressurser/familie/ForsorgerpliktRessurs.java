package no.nav.sosialhjelp.soknad.web.rest.ressurser.familie;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBorSammenMed;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarDeltBosted;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSamvarsgrad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.sosialhjelp.soknad.business.service.TextService;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.NavnFrontend;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.addInntektIfNotPresentInOversikt;
import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.addUtgiftIfNotPresentInOversikt;
import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.removeInntektIfPresentInOversikt;
import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.removeUtgiftIfPresentInOpplysninger;
import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.removeUtgiftIfPresentInOversikt;
import static no.nav.sosialhjelp.soknad.web.rest.mappers.PersonMapper.getPersonnummerFromFnr;
import static no.nav.sosialhjelp.soknad.web.rest.mappers.PersonMapper.mapToJsonNavn;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/familie/forsorgerplikt")
@Timed
@Produces(APPLICATION_JSON)
public class ForsorgerpliktRessurs {

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private TextService textService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @GET
    public ForsorgerpliktFrontend hentForsorgerplikt(@PathParam("behandlingsId") String behandlingsId){
        String eier = SubjectHandler.getUserId();
        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        JsonForsorgerplikt jsonForsorgerplikt = soknad.getSoknad().getData().getFamilie().getForsorgerplikt();

        return mapToForsorgerpliktFrontend(jsonForsorgerplikt);
    }

    @PUT
    public void updateForsorgerplikt(@PathParam("behandlingsId") String behandlingsId, ForsorgerpliktFrontend forsorgerpliktFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = SubjectHandler.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonForsorgerplikt forsorgerplikt = soknad.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();

        updateBarnebidrag(forsorgerpliktFrontend, soknad, forsorgerplikt);
        updateAnsvarAndHarForsorgerplikt(forsorgerpliktFrontend, soknad, forsorgerplikt);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void updateBarnebidrag(ForsorgerpliktFrontend forsorgerpliktFrontend, SoknadUnderArbeid soknad, JsonForsorgerplikt forsorgerplikt) {
        String barnebidragType = "barnebidrag";
        JsonOkonomioversikt oversikt = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt();
        List<JsonOkonomioversiktInntekt> inntekter = oversikt.getInntekt();
        List<JsonOkonomioversiktUtgift> utgifter = oversikt.getUtgift();

        if(forsorgerpliktFrontend.barnebidrag != null) {
            if (forsorgerplikt.getBarnebidrag() == null) {
                forsorgerplikt.setBarnebidrag(new JsonBarnebidrag().withKilde(JsonKildeBruker.BRUKER).withVerdi(forsorgerpliktFrontend.barnebidrag));
            } else {
                forsorgerplikt.getBarnebidrag().setVerdi(forsorgerpliktFrontend.barnebidrag);
            }
            String tittel_mottar = textService.getJsonOkonomiTittel("opplysninger.familiesituasjon.barnebidrag.mottar");
            String tittel_betaler = textService.getJsonOkonomiTittel("opplysninger.familiesituasjon.barnebidrag.betaler");
            switch (forsorgerpliktFrontend.barnebidrag){
                case BEGGE:
                    addInntektIfNotPresentInOversikt(inntekter, barnebidragType, tittel_mottar);
                    addUtgiftIfNotPresentInOversikt(utgifter, barnebidragType, tittel_betaler);
                    break;
                case BETALER:
                    removeInntektIfPresentInOversikt(inntekter, barnebidragType);
                    addUtgiftIfNotPresentInOversikt(utgifter, barnebidragType, tittel_betaler);
                    break;
                case MOTTAR:
                    addInntektIfNotPresentInOversikt(inntekter, barnebidragType, tittel_mottar);
                    removeUtgiftIfPresentInOversikt(utgifter, barnebidragType);
                    break;
                case INGEN:
                    removeInntektIfPresentInOversikt(inntekter, barnebidragType);
                    removeUtgiftIfPresentInOversikt(utgifter, barnebidragType);
                    break;
            }
        } else {
            forsorgerplikt.setBarnebidrag(null);
            removeInntektIfPresentInOversikt(inntekter, barnebidragType);
            removeUtgiftIfPresentInOversikt(utgifter, barnebidragType);
        }
    }

    private void updateAnsvarAndHarForsorgerplikt(ForsorgerpliktFrontend forsorgerpliktFrontend, SoknadUnderArbeid soknad, JsonForsorgerplikt forsorgerplikt) {
        List<JsonAnsvar> systemAnsvar = forsorgerplikt.getAnsvar() == null? new ArrayList<>() : forsorgerplikt.getAnsvar().stream()
                .filter(jsonAnsvar -> jsonAnsvar.getBarn().getKilde().equals(JsonKilde.SYSTEM)).collect(Collectors.toList());
        if (forsorgerpliktFrontend.ansvar != null){
            for (AnsvarFrontend ansvarFrontend : forsorgerpliktFrontend.ansvar){
                for (JsonAnsvar ansvar : systemAnsvar){
                    if (ansvar.getBarn().getPersonIdentifikator().equals(ansvarFrontend.barn.fodselsnummer)){
                        setBorSammenDeltBostedAndSamvarsgrad(ansvarFrontend, ansvar);
                    }
                }
            }
        }

        List<JsonAnsvar> brukerregistrertAnsvar = new ArrayList<>();
        if (forsorgerpliktFrontend.brukerregistrertAnsvar != null && !forsorgerpliktFrontend.brukerregistrertAnsvar.isEmpty()){
            for (AnsvarFrontend ansvarFrontend : forsorgerpliktFrontend.brukerregistrertAnsvar){
                JsonAnsvar ansvar = new JsonAnsvar()
                        .withBarn(new JsonBarn().withKilde(JsonKilde.BRUKER)
                        .withNavn(mapToJsonNavn(ansvarFrontend.barn.navn))
                        .withFodselsdato(ansvarFrontend.barn.fodselsdato));
                setBorSammenDeltBostedAndSamvarsgrad(ansvarFrontend, ansvar);
                if(erAnsvarIkkeTomt(ansvar)) {
                    brukerregistrertAnsvar.add(ansvar);
                }
            }
            if (forsorgerplikt.getHarForsorgerplikt() == null || forsorgerplikt.getHarForsorgerplikt().getVerdi().equals(false)) {
                forsorgerplikt.setHarForsorgerplikt(new JsonHarForsorgerplikt().withKilde(JsonKilde.BRUKER).withVerdi(true));
            }
        } else {
            if (forsorgerplikt.getHarForsorgerplikt() != null && forsorgerplikt.getHarForsorgerplikt().getKilde().equals(JsonKilde.BRUKER)) {
                forsorgerplikt.setHarForsorgerplikt(new JsonHarForsorgerplikt().withKilde(JsonKilde.SYSTEM).withVerdi(false));
                removeBarneutgifterFromSoknad(soknad);
            }
        }

        systemAnsvar.addAll(brukerregistrertAnsvar);
        forsorgerplikt.setAnsvar(systemAnsvar.isEmpty()? null : systemAnsvar);
    }

    private boolean erAnsvarIkkeTomt(JsonAnsvar ansvar) {
        JsonNavn navn = ansvar.getBarn().getNavn();
        if(!navn.getFornavn().isEmpty()) {
            return true;
        }
        if(!navn.getMellomnavn().isEmpty()) {
            return true;
        }
        if(!navn.getEtternavn().isEmpty()) {
            return true;
        }
        if(ansvar.getBarn().getFodselsdato() != null && !ansvar.getBarn().getFodselsdato().isEmpty()) {
            return true;
        }
        return false;
    }

    private void setBorSammenDeltBostedAndSamvarsgrad(AnsvarFrontend ansvarFrontend, JsonAnsvar ansvar) {
        ansvar.setBorSammenMed(ansvarFrontend.borSammenMed == null ? null :
                new JsonBorSammenMed().withKilde(JsonKildeBruker.BRUKER).withVerdi(ansvarFrontend.borSammenMed));
        ansvar.setHarDeltBosted(ansvarFrontend.harDeltBosted == null ? null :
                new JsonHarDeltBosted().withKilde(JsonKildeBruker.BRUKER).withVerdi(ansvarFrontend.harDeltBosted));
        ansvar.setSamvarsgrad(ansvarFrontend.samvarsgrad == null ? null :
                new JsonSamvarsgrad().withKilde(JsonKildeBruker.BRUKER).withVerdi(ansvarFrontend.samvarsgrad));
    }

    private void removeBarneutgifterFromSoknad(SoknadUnderArbeid soknad) {
        JsonOkonomi okonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();
        List<JsonOkonomiOpplysningUtgift> opplysningerBarneutgifter = okonomi.getOpplysninger().getUtgift();
        List<JsonOkonomioversiktUtgift> oversiktBarneutgifter = okonomi.getOversikt().getUtgift();

        okonomi.getOpplysninger().getBekreftelse().removeIf(bekreftelse -> bekreftelse.getType().equals("barneutgifter"));
        removeUtgiftIfPresentInOversikt(oversiktBarneutgifter, "barnehage");
        removeUtgiftIfPresentInOversikt(oversiktBarneutgifter, "sfo");
        removeUtgiftIfPresentInOpplysninger(opplysningerBarneutgifter, "barnFritidsaktiviteter");
        removeUtgiftIfPresentInOpplysninger(opplysningerBarneutgifter, "barnTannregulering");
        removeUtgiftIfPresentInOpplysninger(opplysningerBarneutgifter, "annenBarneutgift");
    }

    private ForsorgerpliktFrontend mapToForsorgerpliktFrontend(JsonForsorgerplikt jsonForsorgerplikt) {
        List<AnsvarFrontend> ansvar = jsonForsorgerplikt.getAnsvar() == null ? null :
                jsonForsorgerplikt.getAnsvar().stream().filter(jsonAnsvar -> jsonAnsvar.getBarn().getKilde().equals(JsonKilde.SYSTEM)).map(this::mapToAnsvarFrontend)
                        .collect(Collectors.toList());
        List<AnsvarFrontend> brukerregistrertAnsvar = jsonForsorgerplikt.getAnsvar() == null ? null :
                jsonForsorgerplikt.getAnsvar().stream().filter(jsonAnsvar -> jsonAnsvar.getBarn().getKilde().equals(JsonKilde.BRUKER)).map(this::mapToAnsvarFrontend)
                        .collect(Collectors.toList());
        return new ForsorgerpliktFrontend()
                .withHarForsorgerplikt(jsonForsorgerplikt.getHarForsorgerplikt() == null ? null :
                        jsonForsorgerplikt.getHarForsorgerplikt().getVerdi())
                .withBarnebidrag(jsonForsorgerplikt.getBarnebidrag() == null ? null :
                        jsonForsorgerplikt.getBarnebidrag().getVerdi())
                .withAnsvar(ansvar)
                .withBrukerregistrertAnsvar(brukerregistrertAnsvar);
    }

    private AnsvarFrontend mapToAnsvarFrontend(JsonAnsvar jsonAnsvar) {
        if (jsonAnsvar == null){
            return null;
        }

        return new AnsvarFrontend().withBarn(mapToBarnFrontend(jsonAnsvar.getBarn()))
                .withErFolkeregistrertSammen(jsonAnsvar.getErFolkeregistrertSammen() == null ? null :
                        jsonAnsvar.getErFolkeregistrertSammen().getVerdi())
                .withBorSammenMed(jsonAnsvar.getBorSammenMed() == null ? null : jsonAnsvar.getBorSammenMed().getVerdi())
                .withHarDeltBosted(jsonAnsvar.getHarDeltBosted() == null ? null : jsonAnsvar.getHarDeltBosted().getVerdi())
                .withSamvarsgrad(jsonAnsvar.getSamvarsgrad() == null ? null : jsonAnsvar.getSamvarsgrad().getVerdi());
    }

    private BarnFrontend mapToBarnFrontend(JsonBarn barn) {
        if (barn == null){
            return null;
        }

        return new BarnFrontend()
                .withNavn(new NavnFrontend(barn.getNavn().getFornavn(), barn.getNavn().getMellomnavn(), barn.getNavn().getEtternavn()))
                .withFodselsdato(barn.getFodselsdato())
                .withPersonnummer(getPersonnummerFromFnr(barn.getPersonIdentifikator()))
                .withFodselsnummer(barn.getPersonIdentifikator());
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class ForsorgerpliktFrontend {
        public Boolean harForsorgerplikt;
        public JsonBarnebidrag.Verdi barnebidrag;
        public List<AnsvarFrontend> ansvar;
        public List<AnsvarFrontend> brukerregistrertAnsvar;

        public ForsorgerpliktFrontend withHarForsorgerplikt(Boolean harForsorgerplikt) {
            this.harForsorgerplikt = harForsorgerplikt;
            return this;
        }

        public ForsorgerpliktFrontend withBarnebidrag(JsonBarnebidrag.Verdi barnebidrag) {
            this.barnebidrag = barnebidrag;
            return this;
        }

        public ForsorgerpliktFrontend withAnsvar(List<AnsvarFrontend> ansvar) {
            this.ansvar = ansvar;
            return this;
        }

        public ForsorgerpliktFrontend withBrukerregistrertAnsvar(List<AnsvarFrontend> brukerregistrertAnsvar) {
            this.brukerregistrertAnsvar = brukerregistrertAnsvar;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class AnsvarFrontend {
        public BarnFrontend barn;
        public Boolean borSammenMed;
        public Boolean erFolkeregistrertSammen;
        public Boolean harDeltBosted;
        public Integer samvarsgrad;

        public AnsvarFrontend withBarn(BarnFrontend barn) {
            this.barn = barn;
            return this;
        }

        public AnsvarFrontend withBorSammenMed(Boolean borSammenMed) {
            this.borSammenMed = borSammenMed;
            return this;
        }

        public AnsvarFrontend withErFolkeregistrertSammen(Boolean erFolkeregistrertSammen) {
            this.erFolkeregistrertSammen = erFolkeregistrertSammen;
            return this;
        }

        public AnsvarFrontend withHarDeltBosted(Boolean harDeltBosted) {
            this.harDeltBosted = harDeltBosted;
            return this;
        }

        public AnsvarFrontend withSamvarsgrad(Integer samvarsgrad) {
            this.samvarsgrad = samvarsgrad;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class BarnFrontend {
        public NavnFrontend navn;
        public String fodselsdato;
        public String personnummer;
        public String fodselsnummer;

        public BarnFrontend withNavn(NavnFrontend navn) {
            this.navn = navn;
            return this;
        }

        public BarnFrontend withFodselsdato(String fodselsdato) {
            this.fodselsdato = fodselsdato;
            return this;
        }

        public BarnFrontend withPersonnummer(String personnummer) {
            this.personnummer = personnummer;
            return this;
        }

        public BarnFrontend withFodselsnummer(String fodselsnummer) {
            this.fodselsnummer = fodselsnummer;
            return this;
        }
    }
}
