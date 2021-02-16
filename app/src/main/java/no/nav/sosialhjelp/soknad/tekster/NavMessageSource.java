package no.nav.sosialhjelp.soknad.tekster;

import org.slf4j.Logger;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

public class NavMessageSource extends ReloadableResourceBundleMessageSource {
    private Map<String, String> basenames = new HashMap<>();
    private String fellesBasename;
    public static final Logger log = getLogger(NavMessageSource.class);

    public Properties getBundleFor(String type, Locale locale) {
        if (basenames.containsKey(type)) {
            Properties properties = new Properties();

            try {
                properties.putAll(hentProperties(fellesBasename, locale));
                properties.putAll(hentProperties(basenames.get(type), locale));
            } catch (Exception ex) {
                log.error(
                        "Kunne ikke hente bundle for type=[{}], locale=[{}], basenames=[{}], fellesbasenames=[{}]",
                        type,
                        locale,
                        basenames,
                        fellesBasename
                );
                throw ex;
            }

            return properties;
        } else {
            return getMergedProperties(locale).getProperties();
        }
    }

    private Properties hentProperties(String propertiesFile, Locale locale) {
        final String localFile = calculateFilenameForLocale(propertiesFile, locale);
        final Properties properties = getProperties(localFile).getProperties();

        if (properties != null) {
            return properties;
        } else {
            log.warn("Finner ikke tekster for {} for språkbundle {} for localefile {}.", propertiesFile, locale.getLanguage(), localFile);
            final Locale noLocale = new Locale("nb", "NO");
            if (locale.equals(noLocale)) {
                throw new IllegalStateException("Kunne ikke laste tekster. Avbryter.");
            } else {
                return hentProperties(propertiesFile, noLocale);
            }
        }
    }

    private String calculateFilenameForLocale(String type, Locale locale) {
        return type + "_" + locale.getLanguage() + ("".equals(locale.getCountry()) ? "" : "_" + locale.getCountry());
    }

    public String finnTekst(String code, Object[] args, Locale locale) {
        return getMessage(code, args, locale);
    }

    public void setBasenames(Bundle fellesBundle, Bundle... soknadBundles) {
        fellesBasename = fellesBundle.propertiesFile;

        List<String> basenameStrings = new ArrayList<>();

        basenameStrings.add(fellesBasename);

        for (Bundle bundle : soknadBundles) {
            basenames.put(bundle.type, bundle.propertiesFile);
            basenameStrings.add(bundle.propertiesFile);
        }

        setBasenames(basenameStrings.toArray(new String[basenameStrings.size()]));
    }

    public Map<String, String> getBasenames() {
        return basenames;
    }

    public static class Bundle {
        public String type;
        public String propertiesFile;

        public Bundle(String type, String propertiesFile) {
            this.type = type;
            this.propertiesFile = propertiesFile;
        }
    }
}
