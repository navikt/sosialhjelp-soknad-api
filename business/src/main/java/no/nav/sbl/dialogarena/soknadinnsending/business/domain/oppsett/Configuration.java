package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlType(propOrder = {})
public class Configuration {
    private List<ConfigurationEntry> entries = new ArrayList<>();


    @XmlElement(name = "entry")
    public List<ConfigurationEntry> getConfiguration() {
        return entries;
    }

    public void setConfiguration(List<ConfigurationEntry> entries) {
        this.entries = entries;
    }

    public boolean containsKey(String key) {
        return get(key) != null;
    }

    public String get(String key) {
        for (ConfigurationEntry entry : entries) {
            if (entry.key.equals(key)) {
                return entry.value;
            }
        }
        return null;
    }

    public static class ConfigurationEntry {
        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
