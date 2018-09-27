package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
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
        List<Faktum> fakta = new ArrayList<>();
        List<Barn> barn = personService.hentBarn(fodselsnummer);

        if (barn == null || barn.isEmpty()) {
            fakta.add(new Faktum().medSoknadId(soknadId)
                    .medKey("system.familie.barn")
                    .medType(SYSTEMREGISTRERT).medValue("false"));
        } else {
            fakta.add(new Faktum().medSoknadId(soknadId)
                    .medKey("system.familie.barn")
                    .medType(SYSTEMREGISTRERT).medValue("true"));
            fakta.add(new Faktum().medSoknadId(soknadId)
            .medKey("system.familie.barn.antall")
            .medType(SYSTEMREGISTRERT).medValue(valueOf(barn.size())));
            fakta.addAll(genererSystemFaktaForBarn(barn, soknadId));
        }
        return fakta;
    }

    List<Faktum> genererSystemFaktaForBarn(List<Barn> barnListe, final Long soknadId) {
        AtomicLong lopenummer = new AtomicLong(1);
        return barnListe.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Barn::getFodselsdato, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(barn ->
                        new Faktum().medSoknadId(soknadId).medKey("system.familie.barn.true.barn").medType(SYSTEMREGISTRERT)
                                .medUnikProperty("id")
                                .medSystemProperty("id", valueOf(lopenummer.getAndIncrement()))
                                .medSystemProperty("fnr", barn.getFnr())
                                .medSystemProperty("fornavn", barn.getFornavn())
                                .medSystemProperty("mellomnavn", barn.getMellomnavn())
                                .medSystemProperty("etternavn", barn.getEtternavn())
                                .medSystemProperty("fodselsdato", barn.getFodselsdato() != null ? barn.getFodselsdato().toString() : null)
                                .medSystemProperty("ikketilgangtilbarn", barn.harIkkeTilgang() + "")
                                .medSystemProperty("folkeregistrertsammen", barn.erFolkeregistrertsammen() + "")
                ).collect(Collectors.toList());
    }

}
