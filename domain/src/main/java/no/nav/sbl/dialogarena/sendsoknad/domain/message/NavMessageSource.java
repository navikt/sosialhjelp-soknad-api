package no.nav.sbl.dialogarena.sendsoknad.domain.message;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class NavMessageSource extends ReloadableResourceBundleMessageSource {

    private Map<String, FileTuple> basenames = new HashMap<>();
    private FileTuple fellesBasename;

    public Properties getBundleFor(String type, Locale locale) {
        if (basenames.containsKey(type)) {
            Properties properties = new Properties();

            properties.putAll(hentRemoteEllerLocal(fellesBasename, locale));
            properties.putAll(hentRemoteEllerLocal(basenames.get(type), locale));

            return properties;
        } else {
            return getMergedProperties(locale).getProperties();
        }
    }

    private Properties hentRemoteEllerLocal(FileTuple fileTuple, Locale locale) {
        String remoteFile = calculateFilenameForLocale(fileTuple.remoteFile, locale);
        String localFile = calculateFilenameForLocale(fileTuple.localFile, locale);

        Properties properties = getProperties(localFile).getProperties();
        Properties remoteProperties = getProperties(remoteFile).getProperties();
        if (remoteProperties != null) {
            properties.putAll(remoteProperties);
        }

        return properties;
    }

    private String calculateFilenameForLocale(String type, Locale locale) {
        return type + "_" + locale.getLanguage() + ("".equals(locale.getCountry())?"": "_" + locale.getCountry());
    }

    public String finnTekst(String code, Object[] args, Locale locale) {
        return getMessage(code, args, locale);
    }

    public void setBasenames(Bundle fellesBundle, Bundle... soknadBundles) {
        fellesBasename = fellesBundle.tuple;

        List<String> basenameStrings = new ArrayList<>();

        basenameStrings.add(fellesBasename.remoteFile);
        basenameStrings.add(fellesBasename.localFile);

        List<Bundle> bundlesList = Arrays.asList(soknadBundles);

        for (Bundle bundle : bundlesList) {
            basenames.put(bundle.type, bundle.tuple);
            basenameStrings.add(bundle.tuple.remoteFile);
            basenameStrings.add(bundle.tuple.localFile);
        }

        setBasenames(basenameStrings.toArray(new String[basenameStrings.size()]));
    }

    public Map<String, FileTuple> getBasenames() {
        return basenames;
    }

    public FileTuple getFellesBasename() {
        return fellesBasename;
    }

    public static class FileTuple {
        private String remoteFile;
        private String localFile;
        FileTuple(String remoteFile, String localFile) {
            this.remoteFile = remoteFile;
            this.localFile = localFile;
        }

        public String getRemoteFile() {
            return remoteFile;
        }

        public String getLocalFile() {
            return localFile;
        }
    }

    public static class Bundle {
        public String type;
        public FileTuple tuple;
        public Bundle(String type, String remoteFile, String localFile) {
            this.type = type;
            this.tuple = new FileTuple(remoteFile, localFile);
        }
    }
}
