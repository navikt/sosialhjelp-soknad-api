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

import static junit.framework.TestCase.assertTrue;

public class SjekkTilgangTilSoknadScanner {


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
                        .setUrls(ClasspathHelper.forPackage("no.nav.sbl.dialogarena.integration.security"))
                        .setScanners(new SubTypesScanner()));
        Set<Class<? extends AbstractSecurityIT>> testClasses = testResourceReflections.getSubTypesOf(AbstractSecurityIT.class);

        Set<String> classnamesForTest = new HashSet<>();
        for (Class clazz : testClasses){
            classnamesForTest.add(clazz.getSimpleName());
        }
        //Fjern testklasse som er fanget opp med reflection
        classnames.remove("Testclass");

        // Fjern klasser som enda ikke har fått tester
        classnames.remove("AlternativRepresentasjonRessurs");
        classnames.remove("VedleggRessurs");
        classnames.remove("SoknadActions");
        classnames.remove("FullOppsummeringRessurs");
        classnames.remove("SosialhjelpVedleggRessurs");


        for(String classname : classnames){
            String expectedTestName = classname + "EndpointIT";
            assertTrue("Foventer en klasse "+expectedTestName+ " som extender AbstractSecurityIT",classnamesForTest.contains(expectedTestName));
        }

    }


}
