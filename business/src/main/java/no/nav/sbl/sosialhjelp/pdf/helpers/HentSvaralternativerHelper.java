package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static no.nav.sbl.sosialhjelp.pdf.HandlebarContext.SPRAK;

@Component
public class HentSvaralternativerHelper extends RegistryAwareHelper<String> {


    @Inject
    private NavMessageSource navMessageSource;

    @Override
    public String getNavn() {
        return "hentSvaralternativer";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter svaralternativer, dersom det er flere valgmuligheter. Prøver med søknadens prefix + key, før den prøver med bare keyen. Kan sende inn parametere.";
    }

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        Set<String> svarAlternativer = findChildPropertyValues(key, SPRAK);
        
        StringBuilder stringBuilder = new StringBuilder();
        createHtmlLayout(svarAlternativer, stringBuilder);
        
        return stringBuilder.toString();
    }

    private Set<String> findChildPropertyValues(String parentKey, Locale locale) {
        Set<String> result = new HashSet<>();

        Pattern tekstfilNavnStruktur = directChildPattern(parentKey);
        for (Entry<String, String> tekstfil : allProperties(locale)) {
            findValueForMatchingSubKey(tekstfilNavnStruktur, tekstfil, HentSvaralternativerHelper::shouldIncludeSubKey)
                .ifPresent((v) -> result.add(v));
        }
        
        return result;
    }

    public Set<String> findChildPropertySubkeys(String parentKey, Locale locale) {
        Set<String> result = new HashSet<>();

        Pattern tekstfilNavnStruktur = directChildPattern(parentKey);
        for (Entry<String, String> tekstfil : allProperties(locale)) {
            findMatchingSubKey(tekstfilNavnStruktur, tekstfil, HentSvaralternativerHelper::shouldIncludeSubKey)
                    .ifPresent((v) -> result.add(v));
        }

        return result;
    }
    
    private Optional<String> findValueForMatchingSubKey(Pattern tekstfilNavnStruktur, Entry<String, String> tekstfil, Predicate<String> shouldInclude) {
        Matcher matcher = tekstfilNavnStruktur.matcher(tekstfil.getKey());
        if (matcher.matches()) {
            String subKey = matcher.group(1);
            if (shouldInclude.test(subKey)) {
                return Optional.of(tekstfil.getValue());
            }
        }
        return Optional.empty();
    }

    private Optional<String> findMatchingSubKey(Pattern tekstfilNavnStruktur, Entry<String, String> tekstfil, Predicate<String> shouldInclude) {
        Matcher matcher = tekstfilNavnStruktur.matcher(tekstfil.getKey());
        if (matcher.matches()) {
            String subKey = matcher.group(1);
            if (shouldInclude.test(subKey)) {
                return Optional.of(subKey);
            }
        }
        return Optional.empty();
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Set<Entry<String, String>> allProperties(Locale locale) {
        Properties bundle = navMessageSource.getBundleFor("soknadsosialhjelp", locale);
        return (Set) bundle.entrySet();
    }

    private Pattern directChildPattern(String parentKey) {
        return Pattern.compile("^" + Pattern.quote(parentKey) + ".([^.]*)$");
    }

    private static boolean shouldIncludeSubKey(String subKey) {
        return !subKey.equals("sporsmal") &&
                !subKey.equals("infotekst") &&
                !subKey.equals("hjelpetekst") &&
                !subKey.equals("label") &&
                !subKey.equals("feilmelding");
    }
    
    private void createHtmlLayout(Set<String> svarAlternativer, StringBuilder stringBuilder) {
        stringBuilder.append("<h4>Svaralternativer:</h4>" +
                "<li>\n" + 
                "    <ul class=\"svar-liste\">\n" + 
                "        ");
        
        for (String alternativ : svarAlternativer) {
            stringBuilder.append("<li>\n" + 
                    "    <span class=\"verdi verdi--radio\">\n" + 
                    "        " + alternativ + "\n" + 
                    "    </span>\n" + 
                    "</li>");
        }

        stringBuilder.append("    </ul>\n" + 
                "</li>");
    }
}

