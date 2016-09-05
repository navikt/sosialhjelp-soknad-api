package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.lagItererbarRespons;

@Component
public class ForSorterteProperties extends RegistryAwareHelper<Faktum> {

    @Override
    public String getNavn() {
        return "forSortertProperties";
    }

    @Override
    public String getBeskrivelse() {
        return "Itererer over alle properties sortert";
    }

    @Override
    public CharSequence apply(Faktum faktum, Options options) throws IOException {

        Map<String, String> properties = faktum.getProperties();
        if (properties.isEmpty()) {
            return options.inverse();
        } else {

            SortedSet<Map.Entry<String, String>> propSet = new TreeSet<>(new Comparator<Map.Entry<String, String>>() {
                @Override
                public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });
            propSet.addAll(properties.entrySet());
            return lagItererbarRespons(options, propSet);
        }
    }
}
