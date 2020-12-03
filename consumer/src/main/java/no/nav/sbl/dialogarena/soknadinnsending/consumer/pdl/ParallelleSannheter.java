package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.EndringDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.MetadataDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.SivilstandDto;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static org.slf4j.LoggerFactory.getLogger;

public class ParallelleSannheter {

    private static final Logger log = getLogger(ParallelleSannheter.class);

    private static final String FREG = "FREG";
    private static final String PDL = "PDL";
    private static final Set<String> MASTERS = Set.of(FREG, PDL);

    public SivilstandDto avklareParallelleSannheter(List<SivilstandDto> sivilstander) {
        if (sivilstander.isEmpty()) {
            return null;
        }
        // sorter sivilstander på synkende endringstidspunkt
        sivilstander.sort(Comparator.comparing(this::getEndringstidspunktOrNull, nullsLast(Comparator.reverseOrder())));

        var sistEndredeSivilstand = sivilstander.get(0);
        if (sistEndredeSivilstand == null
                || flereSivilstanderRegistrertSamtidig(sistEndredeSivilstand, sivilstander)
                || sistEndredeSivilstand.getType() == SivilstandDto.SivilstandType.UOPPGITT
//                || erKildeUdokumentert(first)
                || !MASTERS.contains(sistEndredeSivilstand.getMetadata().getMaster().toUpperCase())) {
            return null;
        }

        return sistEndredeSivilstand;
    }

    private LocalDateTime getEndringstidspunktOrNull(SivilstandDto sivilstand) {
        var metadata = sivilstand.getMetadata();
        var fregMetadata = sivilstand.getFolkeregisterMetadata();
        if (metadata.getMaster().toUpperCase().equals(FREG)) {
            return fregMetadata.getAjourholdstidspunkt();
        } else {
            var endring = sisteEndringOrNull(metadata);
            if (endring == null) {
                log.warn("oops - endring er null?");
            }
            return endring == null ? null : endring.getRegistrert();
        }
    }

    private EndringDto sisteEndringOrNull(MetadataDto metadata) {
        return metadata.getEndringer().stream().max(comparing(EndringDto::getRegistrert)).orElse(null);
    }

    private boolean flereSivilstanderRegistrertSamtidig(SivilstandDto first, List<SivilstandDto> list) {
        return list.stream()
                .filter(sivilstandDto -> Objects.equals(getEndringstidspunktOrNull(sivilstandDto), getEndringstidspunktOrNull(first)))
                .count() > 1;
    }

    private boolean erKildeUdokumentert(SivilstandDto sivilstand) {
        var metadata = sivilstand.getMetadata();
        return PDL.equals(metadata.getMaster().toUpperCase()) && sisteEndringOrNull(metadata) != null && sisteEndringOrNull(metadata).getKilde().equals("Bruker selv");
    }
}
