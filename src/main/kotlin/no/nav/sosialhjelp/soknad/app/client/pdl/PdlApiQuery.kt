package no.nav.sosialhjelp.soknad.app.client.pdl

import no.nav.sosialhjelp.soknad.app.exceptions.PdlApiException
import org.springframework.core.io.ClassPathResource
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

object PdlApiQuery {

    val HENT_PERSON = readGraphQLQueryFromFile("graphql-documents/pdl-person-query.graphql")
    val HENT_BARN = readGraphQLQueryFromFile("graphql-documents/pdl-barn-query.graphql")
    val HENT_EKTEFELLE = readGraphQLQueryFromFile("graphql-documents/pdl-ektefelle-query.graphql")
    val HENT_ADRESSEBESKYTTELSE = readGraphQLQueryFromFile("graphql-documents/pdl-person-adressebeskyttelse-query.graphql")
    val HENT_ADRESSE = readGraphQLQueryFromFile("graphql-documents/pdl-hentadresse.graphql")
    // flere queries?

    private fun readGraphQLQueryFromFile(file: String): String {
        val classPathResource = ClassPathResource(file)
        return try {
            classPathResource.inputStream.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                    reader.lines().collect(Collectors.joining("\n"))
                }
            }
        } catch (e: IOException) {
            throw PdlApiException("Failed to read graphql-file: $file", e)
        }
    }
}
