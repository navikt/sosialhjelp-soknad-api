package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.barn.PdlBarn;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.SivilstandDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.ektefelle.PdlEktefelle;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.SivilstandDto.SivilstandType.GIFT;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.SivilstandDto.SivilstandType.PARTNER;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PdlService {

    private static final Logger log = getLogger(PdlService.class);
    private static final String BARN = "BARN";

    private final PdlConsumer pdlConsumer;
    private final PdlPersonMapper pdlPersonMapper;

    public PdlService(PdlConsumer pdlConsumer, PdlPersonMapper pdlPersonMapper) {
        this.pdlConsumer = pdlConsumer;
        this.pdlPersonMapper = pdlPersonMapper;
    }

    public Person hentPerson(String ident) {
        PdlPerson pdlPerson = pdlConsumer.hentPerson(ident);
        Person person = pdlPersonMapper.mapTilPerson(pdlPerson, ident);

        person.setEktefelle(hentEktefelle(pdlPerson));

        return person;
    }

    public List<Barn> hentBarnForPerson(String ident) {
        PdlPerson pdlPerson = pdlConsumer.hentPerson(ident);

        List<Barn> alleBarn = pdlPerson.getFamilierelasjoner().stream()
                .filter(it -> it.getRelatertPersonsRolle().equalsIgnoreCase(BARN))
                .map(it -> {
                    PdlBarn pdlBarn = pdlConsumer.hentBarn(it.getRelatertPersonsIdent());
                    return pdlPersonMapper.mapTilBarn(pdlBarn, it.getRelatertPersonsIdent(), pdlPerson);
                })
                .collect(Collectors.toList());
        alleBarn.removeIf(Objects::isNull);
        return alleBarn;
    }

    private Ektefelle hentEktefelle(PdlPerson pdlPerson) {
        if (pdlPerson != null && pdlPerson.getSivilstand() != null && !pdlPerson.getSivilstand().isEmpty()) {
            Optional<SivilstandDto> ektefelle = pdlPerson.getSivilstand().stream()
                    .filter(sivilstandDto -> GIFT.equals(sivilstandDto.getType()) || PARTNER.equals(sivilstandDto.getType()))
                    .findFirst();
            if (ektefelle.isPresent()) {
                String ektefelleIdent = ektefelle.get().getRelatertVedSivilstand();
                PdlEktefelle pdlEktefelle = pdlConsumer.hentEktefelle(ektefelleIdent);
                return pdlPersonMapper.mapTilEktefelle(pdlEktefelle, ektefelleIdent, pdlPerson);
            }
        }
        return null;
    }

}
