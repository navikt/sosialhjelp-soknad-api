package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static no.nav.sosialhjelp.soknad.business.pdf.HandlebarContext.SPRAK;

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
    public CharSequence apply(String key, Options options) {
        final Set<String> svarAlternativer = findChildPropertyValues(key);
        
        StringBuilder stringBuilder = new StringBuilder();
        createHtmlLayout(svarAlternativer, stringBuilder);
        
        return stringBuilder.toString();
    }

    private Set<String> findChildPropertyValues(final String parentKey) {
        final Set<String> result = new HashSet<>();

        final Pattern tekstfilNavnStruktur = directChildPattern(parentKey);
        for (Entry<String, String> tekstfil : allProperties(SPRAK)) {
            findValueForMatchingSubKey(tekstfilNavnStruktur, tekstfil, HentSvaralternativerHelper::shouldIncludeSubKey)
                .ifPresent(result::add);
        }
        
        return result;
    }

    public Set<String> findChildPropertySubkeys(final String parentKey, final Locale locale) {
        final Set<String> result = new HashSet<>();

        final Pattern tekstfilNavnStruktur = directChildPattern(parentKey);
        for (Entry<String, String> tekstfil : allProperties(locale)) {
            findMatchingSubKey(tekstfilNavnStruktur, tekstfil, HentSvaralternativerHelper::shouldIncludeSubKey)
                    .ifPresent(result::add);
        }

        return result;
    }
    
    private Optional<String> findValueForMatchingSubKey(final Pattern tekstfilNavnStruktur, final Entry<String, String> tekstfil, final Predicate<String> shouldInclude) {
        final Matcher matcher = tekstfilNavnStruktur.matcher(tekstfil.getKey());
        if (matcher.matches()) {
            final String subKey = matcher.group(1);
            if (shouldInclude.test(subKey)) {
                return Optional.of(tekstfil.getValue());
            }
        }
        return Optional.empty();
    }

    private Optional<String> findMatchingSubKey(final Pattern tekstfilNavnStruktur, final Entry<String, String> tekstfil, final Predicate<String> shouldInclude) {
        final Matcher matcher = tekstfilNavnStruktur.matcher(tekstfil.getKey());
        if (matcher.matches()) {
            final String subKey = matcher.group(1);
            if (shouldInclude.test(subKey)) {
                return Optional.of(subKey);
            }
        }
        return Optional.empty();
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Set<Entry<String, String>> allProperties(final Locale locale) {
        final Properties bundle = navMessageSource.getBundleFor("soknadsosialhjelp", locale);
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
    
    private void createHtmlLayout(final Set<String> svarAlternativer, StringBuilder stringBuilder) {
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

