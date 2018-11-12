package no.nav.sbl.dialogarena.service.helpers;

import static org.apache.commons.lang3.LocaleUtils.toLocale;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.github.jknack.handlebars.Options;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.dialogarena.service.HandlebarsUtils;
import no.nav.sbl.dialogarena.service.oppsummering.OppsummeringsFaktum;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;

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
        String sprak = "nb_NO";
        final Set<String> svarAlternativer = findChildPropertyValues(key, toLocale(sprak));
        
        StringBuilder stringBuilder = new StringBuilder();
        createHtmlLayout(svarAlternativer, stringBuilder);
        
        return stringBuilder.toString();
    }

    private Set<String> findChildPropertyValues(final String parentKey, final Locale locale) {
        final Set<String> result = new HashSet<>();

        final Pattern pattern = directChildPattern(parentKey);
        for (Entry<String, String> entry : allProperties(locale)) {
            findMatchingSubKey(pattern, entry, HentSvaralternativerHelper::shouldIncludeSubKey)
                .ifPresent((v) -> result.add(v));
        }
        
        return result;
    }
    
    private Optional<String> findMatchingSubKey(final Pattern pattern, final Entry<String, String> entry, final Predicate<String> shouldInclude) {
        final Matcher matcher = pattern.matcher(entry.getKey());
        if (matcher.matches()) {
            final String subKey = matcher.group(1);
            if (shouldInclude.test(subKey)) {
                return Optional.of(entry.getValue());
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
                !subKey.equals("label");
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

