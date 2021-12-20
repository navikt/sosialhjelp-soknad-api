package no.nav.sosialhjelp.soknad.web.oidc;

import com.nimbusds.jose.util.IOUtils;
import com.nimbusds.jose.util.Resource;
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

import static no.nav.sosialhjelp.soknad.common.filedetection.MimeTypes.APPLICATION_JSON;

public class FileResourceRetriever extends ProxyAwareResourceRetriever {

    private final String metadataFile;
    private final String metadataTokenxFile;
    private final String jwksFile;

    public FileResourceRetriever(String metadataFile, String metadataTokenxFile, String jwksFile) {
        this.metadataFile = metadataFile;
        this.metadataTokenxFile = metadataTokenxFile;
        this.jwksFile = jwksFile;
    }

    @Override
    public Resource retrieveResource(URL url) {
        String content = getContentFromFile(url);
        return new Resource(content, APPLICATION_JSON);
    }

    private String getContentFromFile(URL url){
        try {
            if (url.toString().contains("metadata-tokenx")) {
                return IOUtils.readInputStreamToString( getInputStream(metadataTokenxFile), Charset.forName("UTF-8"));
            }
            if (url.toString().contains("metadata")) {
                return IOUtils.readInputStreamToString( getInputStream(metadataFile), Charset.forName("UTF-8"));
            }
            if (url.toString().contains("jwks")) {
                return IOUtils.readInputStreamToString(getInputStream(jwksFile), Charset.forName("UTF-8"));
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getInputStream(String file) throws IOException {
        return FileResourceRetriever.class.getResourceAsStream(file);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [metadataFile=" + metadataFile + " [metadataTokenxFile=" + metadataTokenxFile + ", jwksFile=" + jwksFile + "]";
    }

}
