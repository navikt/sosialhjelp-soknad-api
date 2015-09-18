package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static no.nav.sbl.dialogarena.service.HandlebarsUtils.finnWebSoknad;

@Component
public class HvisKunStudentHelper extends RegistryAwareHelper<Object> {

    private static final String NAVN = "hvisKunStudent";

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public String getBeskrivelse() {
        return "Sjekker om brukeren har en annen status enn student (f.eks sykmeldt, i arbeid osv.)";
    }

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        WebSoknad soknad = finnWebSoknad(options.context);

        Faktum iArbeidFaktum = soknad.getFaktumMedKey("navaerendeSituasjon.iArbeid");
        Faktum sykmeldtFaktum = soknad.getFaktumMedKey("navaerendeSituasjon.sykmeldt");
        Faktum arbeidsledigFaktum = soknad.getFaktumMedKey("navaerendeSituasjon.arbeidsledig");
        Faktum forstegangstjenesteFaktum = soknad.getFaktumMedKey("navaerendeSituasjon.forstegangstjeneste");
        Faktum annetFaktum = soknad.getFaktumMedKey("navaerendeSituasjon.annet");

        Faktum[] fakta = {iArbeidFaktum, sykmeldtFaktum, arbeidsledigFaktum, forstegangstjenesteFaktum, annetFaktum};

        for (Faktum faktum : fakta) {
            if (faktum != null && "true".equals(faktum.getValue())) {
                return options.inverse(this);
            }
        }

        return options.fn(this);
    }
}