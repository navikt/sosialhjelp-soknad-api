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

    /**
     *  Tar inn json og html på string-format. Htmlen er en Mustache-mal, med nøkler
     *  som matcher json-nøklene.
     *  @return String Html med verdier fra Json-data
     */
    @SuppressWarnings("unchecked")
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

    /**
     *  Tar inn html i stringformat og data i en Map.
     *  Html-stringen er en Mustache-mal, med nøkler som matcher nøklene i input-Map.
     *  @return String Html med verdier fra input Map
     */
    public static String applyInline(String input, Map<String, Object> hash) throws IOException {
        return new Handlebars().compileInline(input).apply(hash);
    }

    /**
     *  Tar inn json på string-format og navnet på en resource hbs-fil.
     *  Hbs-fila er en Mustache-mal, med nøkler
     *  som matcher json-nøklene.
     *  @return String Html med verdier fra Json-data
     */
    @SuppressWarnings("unchecked")
    public static String applyTemplate(String json, String hbsFil) {
        String out = "";
        try {
            Map<String, Object> map = new ObjectMapper().readValue(json, HashMap.class);
            out = apply(hbsFil, map);
        } catch (IOException e) {
            LOG.info("Kunne ikke legge json inn i mal", e);
        }
        return out;
    }

    /**
     *  Tar inn navnet på en resource hbs-fil og data i en Map.
     *  Hbs-fila er en Mustache-mal, med nøkler som matcher nøklene i input-Map.
     *  @return String Html med verdier fra input Map
     */
    public static String apply(String htmlFile, Map<String, Object> hash) throws IOException {
        return new Handlebars().compile(htmlFile).apply(hash);
    }


}
