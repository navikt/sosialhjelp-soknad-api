package no.nav.sosialhjelp.soknad.consumer.pdl.common;

import no.nav.sosialhjelp.soknad.consumer.exceptions.PdlApiException;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class PdlApiQuery {

    private PdlApiQuery() {
    }

    public static final String HENT_PERSON = readGraphQLQueryFromFile("graphql/pdl-person-query.graphql");
    public static final String HENT_BARN = readGraphQLQueryFromFile("graphql/pdl-barn-query.graphql");
    public static final String HENT_EKTEFELLE = readGraphQLQueryFromFile("graphql/pdl-ektefelle-query.graphql");
    // flere queries?

    private static String readGraphQLQueryFromFile(String file) {
        ClassPathResource classPathResource = new ClassPathResource(file);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(classPathResource.getInputStream(), UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new PdlApiException("Failed to read graphql-file: " + file, e);
        }
    }
}
