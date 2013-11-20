package no.nav.sbl.dialogarena.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class HandleBarRunner {

    public static String getHTML(String jsonSource, String html) {
        String out = "";
        try {
            Map<String, Object> map = new ObjectMapper().readValue(jsonSource, HashMap.class);
            out = apply(html, map);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return out;
    }

    public static String apply(String input, Map<String, Object> hash) throws IOException {
        return new Handlebars().compileInline(input).apply(hash);
    }

}
