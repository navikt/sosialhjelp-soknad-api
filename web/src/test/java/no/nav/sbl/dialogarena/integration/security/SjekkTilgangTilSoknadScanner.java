package no.nav.sbl.dialogarena.integration.security;


import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SjekkTilgangTilSoknadScanner {


    //Denne kan i fremtiden fikses til å hente alle @Path-annotasjoner fra Jersey og anta noe om testene i security-pakka for å sjekke om alle testes

    @Test
    public void sjekkAtAlleKlasserMedSikkerhetsannotasjonHarEnKlasseHer() {
        Reflections restResourceReflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage("no.nav.sbl.dialogarena.rest"))
                        .setScanners(new MethodAnnotationsScanner()));
        Set<Method> annotatedMethods = restResourceReflections.getMethodsAnnotatedWith(SjekkTilgangTilSoknad.class);

        Set<String> classnames = new HashSet<>();

        for (Method method : annotatedMethods){
            classnames.add(method.getDeclaringClass().getSimpleName());
        }

        Reflections testResourceReflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage("no.nav.sbl.dialogarena.integration"))
                        .setScanners(new SubTypesScanner()));

        Set<Class<? extends AbstractSecurityIT>> subTypesOf = testResourceReflections.getSubTypesOf(AbstractSecurityIT.class);

        Set<String> classnamesForTest = new HashSet<>();
        for (Class clazz : subTypesOf){
            classnamesForTest.add(clazz.getSimpleName());
        }
        //Fjern testklasse som er fanget opp med reflection
        classnames.remove("Testclass");

        // Fjern klasser som enda ikke har fått tester
        classnames.remove("AlternativRepresentasjonRessurs");
        classnames.remove("VedleggRessurs");
        classnames.remove("SosialhjelpVedleggRessurs");


        for(String classname : classnames){
            assertThat(classnamesForTest).contains(classname + "EndpointIT");
        }

    }


}
