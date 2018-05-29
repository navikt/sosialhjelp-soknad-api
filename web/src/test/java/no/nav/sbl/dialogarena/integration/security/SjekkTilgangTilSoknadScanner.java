package no.nav.sbl.dialogarena.integration.security;


import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.rest.ressurser.InternalRessurs;
import no.nav.sbl.dialogarena.rest.ressurser.RedirectRessurs;
import no.nav.sbl.dialogarena.rest.ressurser.SosialhjelpVedleggRessurs;
import no.nav.sbl.dialogarena.rest.ressurser.informasjon.InformasjonRessurs;
import no.nav.sbl.dialogarena.rest.ressurser.informasjon.TjenesterRessurs;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.ws.rs.*;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

public class SjekkTilgangTilSoknadScanner {


    //Denne kan i fremtiden fikses til å hente alle @Path-annotasjoner fra Jersey og anta noe om testene i security-pakka for å sjekke om alle testes

    @Test
    public void sjekkAtAlleKlasserMedSikkerhetsannotasjonHarEnKlasseHer() {
        Reflections restResourceReflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage("no.nav.sbl.dialogarena.rest"))
                        .setScanners(new MethodAnnotationsScanner()));
        List<Method> annotatedMethods = new ArrayList<>();
        annotatedMethods.addAll(restResourceReflections.getMethodsAnnotatedWith(GET.class));
        annotatedMethods.addAll(restResourceReflections.getMethodsAnnotatedWith(PUT.class));
        annotatedMethods.addAll(restResourceReflections.getMethodsAnnotatedWith(POST.class));
        annotatedMethods.addAll(restResourceReflections.getMethodsAnnotatedWith(DELETE.class));

        Set<String> methods = new HashSet<>();

        for (Method method : annotatedMethods) {
            methods.add(getPrefixFromAnnotation(method) + method.getName() + getPostfixFromPath(method));
        }

        Reflections testResourceReflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage("no.nav.sbl.dialogarena.integration"))
                        .setScanners(new SubTypesScanner()));

        Set<Class<? extends AbstractSecurityIT>> subTypesOf = testResourceReflections.getSubTypesOf(AbstractSecurityIT.class);

        Set<String> methodnamesInTestClasses = subTypesOf.stream()
                .map(clazz -> Arrays.asList(clazz.getMethods()).stream()
                        .map(method -> method.getName())
                        .collect(toSet())
                )
                .reduce(new HashSet<>(),(set1, set2) -> {
                    set1.addAll(set2);
                    return set1;
                });

        //Klasser som står uten en EndpointIT testklasse - bør vurdere å kanskje implementere noen av disse?
        methods.removeAll(getMethodnamesForClass(InformasjonRessurs.class));
        methods.removeAll(getMethodnamesForClass(InternalRessurs.class));
        methods.removeAll(getMethodnamesForClass(RedirectRessurs.class));
        methods.removeAll(getMethodnamesForClass(SosialhjelpVedleggRessurs.class));
        methods.removeAll(getMethodnamesForClass(TjenesterRessurs.class));

        //Metoder som mangler tester - klasse: SoknadRessurs
        methods.remove("DELETE_slettSoknad_behandlingsId");
        methods.remove("GET_hentInnsendtSoknad_behandlingsId");
        methods.remove("GET_hentPaakrevdeVedlegg_behandlingsId_vedlegg");
        methods.remove("GET_hentSoknadData_behandlingsId");
        methods.remove("GET_hentSynligSoknadStruktur_behandlingsId_synligsoknadstruktur");
        methods.remove("GET_hentOppsummering_behandlingsId");
        methods.remove("GET_hentOppsummeringMedStandardMediatype_behandlingsId");
        methods.remove("POST_opprettSoknad");
        methods.remove("PUT_lagreFakta_behandlingsId_fakta");
        methods.remove("PUT_oppdaterSoknad_behandlingsId");

        //Metoder som mangler tester - klasse: VedleggRessurs
        methods.remove("POST_lastOppFiler_fil");

        //Metoder som mangler tester - klasse: FaktaRessurs
        methods.remove("DELETE_slettFaktum_faktumId");
        methods.remove("GET_hentPaakrevdeVedlegg");
        methods.remove("GET_hentVedlegg_faktumId_vedlegg");
        methods.remove("POST_lagreFaktumMedPost_faktumId");
        methods.remove("PUT_lagreFaktum_faktumId");

        for (String methodname : methods) {
            assertThat(methodnamesInTestClasses).contains(methodname);
        }

    }

    private String getPostfixFromPath(Method method) {
        Path[] annotations = method.getAnnotationsByType(Path.class);
        String postfix = "";
        if (annotations.length != 0) {
            postfix = annotations[0]
                    .value()
                    .replaceAll("[{}.]", "")
                    .replace("/", "_");
        }
        return postfix;

    }

    private String getPrefixFromAnnotation(Method method) {
        List<Class> annotations = Arrays.asList(GET.class, PUT.class, POST.class, DELETE.class);
        String prefix = "";
        for (Class clazz : annotations) {
            if (method.getAnnotation(clazz) != null) {
                prefix = clazz.getSimpleName() + "_";
            }
        }
        return prefix;
    }

    private Set<String> getMethodnamesForClass(Class clazz) {
        return Arrays.asList(clazz.getMethods())
                .stream()
                .map(method -> getPrefixFromAnnotation(method) + method.getName() + getPostfixFromPath(method))
                .collect(toSet());
    }

}
