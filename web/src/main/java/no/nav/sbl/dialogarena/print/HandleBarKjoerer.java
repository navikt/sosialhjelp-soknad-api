package no.nav.sbl.dialogarena.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class HandleBarKjoerer {
    private static final Logger LOGG = LoggerFactory.getLogger(HandleBarKjoerer.class);

    /**
     *  Tar inn json og html på string-format. Htmlen er en Mustache-mal, med nøkler
     *  som matcher json-nøklene.
     *  @return String Html med verdier fra Json-data
     */
    @SuppressWarnings("unchecked")
    public static String hentHTML(String json, String html) {
        String ut = "";
        try {
            Map<String, Object> hashMap = new ObjectMapper().readValue(json, HashMap.class);
            ut = fyllHtmlStringMedInnhold(html, hashMap);
        } catch (IOException e) {
            LOGG.info("Kunne ikke legge json inn i mal", e);
        }
        return ut;
    }

    /**
     *  Tar inn html i stringformat og data i en Map.
     *  Html-stringen er en Mustache-mal, med nøkler som matcher nøklene i input-Map.
     *  @return String Html med verdier fra input Map
     */
    public static String fyllHtmlStringMedInnhold(String input, Map<String, Object> hash) throws IOException {
        return new Handlebars().compileInline(input).apply(hash);
    }

    /**
     *  Tar inn json på string-format og navnet på en resource hbs-fil.
     *  Hbs-fila er en Mustache-mal, med nøkler
     *  som matcher json-nøklene.
     *  @return String Html med verdier fra Json-data
     */
    @SuppressWarnings("unchecked")
    public static String fyllHtmlMalMedInnhold(String json, String hbsFil) {
        String ut = "";
        try {
            Map<String, Object> map = new ObjectMapper().readValue(json, HashMap.class);
            ut = fyllHtmlMalMedInnhold(hbsFil, map);
        } catch (IOException e) {
            LOGG.info("Kunne ikke legge json inn i mal", e);
        }
        return ut;
    }

    /**
     *  Tar inn navnet på en resource hbs-fil og data i en Map.
     *  Hbs-fila er en Mustache-mal, med nøkler som matcher nøklene i input-Map.
     *  @return String Html med verdier fra input Map
     */
    public static String fyllHtmlMalMedInnhold(String htmlFile, Map<String, Object> hash) throws IOException {
        return new Handlebars().compile(htmlFile).apply(hash);
    }


}
