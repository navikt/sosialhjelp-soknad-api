package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;
import static no.nav.sbl.dialogarena.service.HandlebarsUtils.lagItererbarRespons;

@Component
public class SosialhjelpVedleggHelper extends RegistryAwareHelper<Object>{

    public static final String NAVN = "sosialhjelpVedlegg";

    @Override
    public CharSequence apply(Object key, Options options) throws IOException {
        System.out.println(NAVN + " : " + getBeskrivelse());

        WebSoknad soknad = finnWebSoknad(options.context);


        List<Vedlegg> vedlegg = soknad.getVedlegg();

        Map<Long, List<Vedlegg>> gruppert = new LinkedHashMap<>();

        for (Vedlegg v : vedlegg) {
            Long proxyFaktumId = v.getFaktumId();
            Long belopFaktumId = soknad.getFaktumMedId(proxyFaktumId + "").getParrentFaktum();

            List<Vedlegg> gruppe = gruppert.get(belopFaktumId);
            if (gruppe == null) {
                gruppe = new ArrayList<>();
                gruppert.put(belopFaktumId, gruppe);
            }
            gruppe.add(v);
        }

        List<List<Vedlegg>> alleGrupper = new ArrayList<>(gruppert.values());

        return lagItererbarRespons(options, alleGrupper);
    }

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Helper for å liste ut vedlegg for sosialhjelp";
    }
}
