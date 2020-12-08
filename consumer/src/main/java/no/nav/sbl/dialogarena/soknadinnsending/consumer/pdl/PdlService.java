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

    private final PdlPersonMapperHelper helper = new PdlPersonMapperHelper();

    public PdlService(PdlConsumer pdlConsumer, PdlPersonMapper pdlPersonMapper) {
        this.pdlConsumer = pdlConsumer;
        this.pdlPersonMapper = pdlPersonMapper;
    }

    public Person hentPerson(String ident) {
        PdlPerson pdlPerson = pdlConsumer.hentPerson(ident);

        return pdlPersonMapper.mapTilPerson(pdlPerson, ident)
                .withEktefelle(hentEktefelle(pdlPerson));
    }

    public List<Barn> hentBarnForPerson(String ident) {
        PdlPerson pdlPerson = pdlConsumer.hentPerson(ident);

        List<Barn> alleBarn = pdlPerson.getFamilierelasjoner().stream()
                .filter(familierelasjonDto -> familierelasjonDto.getRelatertPersonsRolle().equalsIgnoreCase(BARN))
                .map(familierelasjonDto -> {
                    var barnIdent = familierelasjonDto.getRelatertPersonsIdent();
                    loggHvisIdentIkkeErFnr(barnIdent);
                    var pdlBarn = pdlConsumer.hentBarn(barnIdent);
                    return pdlPersonMapper.mapTilBarn(pdlBarn, barnIdent, pdlPerson);
                })
                .collect(Collectors.toList());
        alleBarn.removeIf(Objects::isNull);
        return alleBarn;
    }

    private Ektefelle hentEktefelle(PdlPerson pdlPerson) {
        if (pdlPerson != null && pdlPerson.getSivilstand() != null && !pdlPerson.getSivilstand().isEmpty()) {

            var sivilstand = helper.utledGjeldendeSivilstand(pdlPerson.getSivilstand());
            if (sivilstand != null && (GIFT.equals(sivilstand.getType()) || PARTNER.equals(sivilstand.getType()))) {
                String ektefelleIdent = sivilstand.getRelatertVedSivilstand();

                if (ektefelleIdent == null || ektefelleIdent.isEmpty()) {
                    log.info("Sivilstand.relatertVedSivilstand (ektefelleIdent) er null -> vi kaller ikke pdl hentEktefelle");
                    return null;
                }

                loggHvisIdentIkkeErFnr(ektefelleIdent);

                PdlEktefelle pdlEktefelle = pdlConsumer.hentEktefelle(ektefelleIdent);
                return pdlPersonMapper.mapTilEktefelle(pdlEktefelle, ektefelleIdent, pdlPerson);
            }
        }
        return null;
    }

    private void loggHvisIdentIkkeErFnr(String ektefelleIdent) {
        if (ektefelleIdent.length() == 11 && Integer.parseInt(ektefelleIdent.substring(0,2)) > 31) {
            log.info("Ident er DNR");
        }
        if (ektefelleIdent.length() > 11) {
            log.info("Ident er aktørid");
        }
    }

}
