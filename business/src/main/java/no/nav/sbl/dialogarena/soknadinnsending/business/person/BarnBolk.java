package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import org.apache.commons.collections15.Transformer;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;

@Service
public class BarnBolk implements BolkService {

    @Inject
    private PersonService personService;

    @Override
    public String tilbyrBolk() {
        return "Barn";
    }

    @Override
    public List<Faktum> genererSystemFakta(String fodselsnummer, final Long soknadId) {
        List<Barn> listeAvBarn = personService.hentBarn(fodselsnummer);
        return on(listeAvBarn).map(new Transformer<Barn, Faktum>() {
            @Override
            public Faktum transform(Barn barn) {
                return new Faktum().medSoknadId(soknadId).medKey("barn").medType(SYSTEMREGISTRERT)
                        .medSystemProperty("fornavn", barn.getFornavn())
                        .medSystemProperty("mellomnavn", barn.getMellomnavn())
                        .medSystemProperty("etternavn", barn.getEtternavn())
                        .medSystemProperty("sammensattnavn", barn.getSammensattnavn())
                        .medSystemProperty("fnr", barn.getFnr())
                        .medSystemProperty("kjonn", barn.getKjonn())
                        .medSystemProperty("alder", barn.getAlder().toString())
                        .medSystemProperty("land", barn.getLand())
                        .medUnikProperty("fnr");
            }
        }).collect();
    }

}
