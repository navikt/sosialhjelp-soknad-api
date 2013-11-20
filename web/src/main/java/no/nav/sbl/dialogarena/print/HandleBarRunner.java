package no.nav.sbl.dialogarena.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class HandleBarRunner {
    private static final Logger LOG = LoggerFactory.getLogger(HandleBarRunner.class);

    public static String getHTML(String jsonSource, String html) {
        String out = "";
        try {
            Map<String, Object> map = new ObjectMapper().readValue(jsonSource, HashMap.class);
            out = applyInline(html, map);
        } catch (IOException e) {
            LOG.info("Kunne ikke legge json inn i mal", e);
        }
        return out;
    }

    public static String applyInline(String input, Map<String, Object> hash) throws IOException {
        return new Handlebars().compileInline(input).apply(hash);
    }

    public static String applyTemplate(String jsonSource, String htmlFile) {
        String out = "";
        try {
            Map<String, Object> map = new ObjectMapper().readValue(jsonSource, HashMap.class);
            out = apply(htmlFile, map);
        } catch (IOException e) {
            LOG.info("Kunne ikke legge json inn i mal", e);
        }
        return out;
    }

    public static String apply(String htmlFile, Map<String, Object> hash) throws IOException {
        return new Handlebars().compile(htmlFile).apply(hash);
    }


}
