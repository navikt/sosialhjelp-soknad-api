package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class XmlService {

    public static final Pattern INCLUDE_PATTERN = Pattern.compile("<xi:include href=\"(.*?)\" .*?>");
    public static final Pattern SOKNAD_PATTERN = Pattern.compile("<soknad .*?>(.*)</soknad>", Pattern.DOTALL);
    public static final String XML_HEADER_PATTERN = "<\\?xml .*? ?>";

    public StreamSource lastXmlFilMedInclude(String fil) throws IOException {
        String filsti = FilenameUtils.getFullPath(fil);
        String filNavn = FilenameUtils.getName(fil);

        return new StreamSource(new StringReader(lastXmlFilMedInclude(filsti, filNavn)));
    }

    public String lastXmlFilMedInclude(String filsti, String filNavn) throws IOException {
        String mainFile = hentFilInnhold(filsti + filNavn);

        Matcher matcher = INCLUDE_PATTERN.matcher(mainFile);
        StringBuffer fullXml = new StringBuffer();

        while (matcher.find()) {
            String href = matcher.group(1);
            String includePath = filsti + FilenameUtils.getFullPath(href);
            String includeFile = FilenameUtils.getName(href);

            String includeXml = lastXmlFilMedInclude(includePath, includeFile);
            includeXml = fiksInkludertXml(includeXml);

            matcher.appendReplacement(fullXml, includeXml);
        }
        matcher.appendTail(fullXml);

        return fullXml.toString();
    }

    private String hentFilInnhold(String fil) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(fil);
        return IOUtils.toString(stream);
    }

    private String fiksInkludertXml(String innhold) {
        Matcher childMatcher = SOKNAD_PATTERN.matcher(innhold);
        if (childMatcher.find()) {
            return childMatcher.group(1);
        } else {
            return innhold.replaceAll(XML_HEADER_PATTERN, "");
        }
    }
}
