package no.nav.sosialhjelp.soknad.integrationtest.oidc

import com.nimbusds.jose.util.IOUtils
import com.nimbusds.jose.util.Resource
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever
import no.nav.sosialhjelp.soknad.common.filedetection.MimeTypes.APPLICATION_JSON
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset

class FileResourceRetriever(
    private val metadataFile: String,
    private val metadataTokenxFile: String,
    private val jwksFile: String
) : ProxyAwareResourceRetriever() {

    override fun retrieveResource(url: URL): Resource {
        val content = getContentFromFile(url)
        return Resource(content, APPLICATION_JSON)
    }

    private fun getContentFromFile(url: URL): String? {
        return try {
            if (url.toString().contains("metadata-tokenx")) {
                return IOUtils.readInputStreamToString(getInputStream(metadataTokenxFile), Charset.forName("UTF-8"))
            }
            if (url.toString().contains("metadata")) {
                return IOUtils.readInputStreamToString(getInputStream(metadataFile), Charset.forName("UTF-8"))
            }
            if (url.toString().contains("jwks")) {
                IOUtils.readInputStreamToString(getInputStream(jwksFile), Charset.forName("UTF-8"))
            } else null
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun getInputStream(file: String): InputStream? {
        return FileResourceRetriever::class.java.getResourceAsStream(file)
    }

    override fun toString(): String {
        return javaClass.simpleName + " [metadataFile=" + metadataFile + " [metadataTokenxFile=" + metadataTokenxFile + ", jwksFile=" + jwksFile + "]"
    }
}
