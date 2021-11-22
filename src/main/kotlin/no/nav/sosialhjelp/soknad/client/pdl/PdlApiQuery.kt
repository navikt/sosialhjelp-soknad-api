package no.nav.sosialhjelp.soknad.client.pdl

import no.nav.sosialhjelp.soknad.consumer.exceptions.PdlApiException
import org.springframework.core.io.ClassPathResource
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

object PdlApiQuery {

    val HENT_PERSON = readGraphQLQueryFromFile("graphql/pdl-person-query.graphql")
    val HENT_BARN = readGraphQLQueryFromFile("graphql/pdl-barn-query.graphql")
    val HENT_EKTEFELLE = readGraphQLQueryFromFile("graphql/pdl-ektefelle-query.graphql")
    val HENT_ADRESSEBESKYTTELSE = readGraphQLQueryFromFile("graphql/pdl-person-adressebeskyttelse-query.graphql")
    val ADRESSE_SOK = readGraphQLQueryFromFile("graphql/pdl-adressesok.graphql")
    val HENT_GEOGRAFISK_TILKNYTNING = readGraphQLQueryFromFile("graphql/pdl-geografisktilknytning-query.graphql")
    // flere queries?

    // flere queries?
    private fun readGraphQLQueryFromFile(file: String): String {
        val classPathResource = ClassPathResource(file)
        try {
            BufferedReader(InputStreamReader(classPathResource.inputStream, StandardCharsets.UTF_8)).use { reader ->
                return reader.lines().collect(Collectors.joining("\n"))
            }
        } catch (e: IOException) {
            throw PdlApiException("Failed to read graphql-file: $file", e)
        }
    }
}
