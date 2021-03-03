package no.nav.sosialhjelp.soknad.consumer.pdl.person;

import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.EndringDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.FolkeregistermetadataDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.MetadataDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.NavnDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import static org.slf4j.LoggerFactory.getLogger;

public class PdlPersonMapperHelper {

    private static final Logger log = getLogger(PdlPersonMapperHelper.class);

    private static final String FREG = "FREG";
    private static final String PDL = "PDL";
    private static final Set<String> MASTERS = Set.of(FREG, PDL);
    private static final String BRUKER_SELV = "Bruker selv";

    public SivilstandDto utledGjeldendeSivilstand(List<SivilstandDto> sivilstander) {
        if (sivilstander == null || sivilstander.isEmpty()) {
            return null;
        }
        // sorter sivilstander på synkende endringstidspunkt
        sivilstander.sort(comparing(this::getEndringstidspunktOrNull, nullsLast(reverseOrder())));

        if (sivilstander.size() > 1) {
            log.info("Flere gjeldende sivilstander funnet i PDL: [{}]", sivilstander.stream().map(dto -> dto.getType().toString()).collect(Collectors.joining(",")));
        }

        var sistEndredeSivilstand = sivilstander.get(0);
        if (sistEndredeSivilstand == null
                || flereSivilstanderRegistrertSamtidig(sistEndredeSivilstand, sivilstander)
                || sistEndredeSivilstand.getType() == SivilstandDto.SivilstandType.UOPPGITT
//                Kommentert ut fordi vi ikke er 100% sikre på om vi skal vise sivilstander fra udokumenterte kilder (master == "bruker selv").
//                Hvis disse skal filtreres vekk, kan linjen kommenteres inn igjen.
//                || erKildeUdokumentert(sistEndredeSivilstand)
                || !MASTERS.contains(sistEndredeSivilstand.getMetadata().getMaster().toUpperCase())) {
            return null;
        }
        if (erKildeUdokumentert(sistEndredeSivilstand.getMetadata())) {
            log.info("PDL sivilstand er udokumentert (kilde = {})", sisteEndringOrNull(sistEndredeSivilstand.getMetadata()).getKilde());
        }

        return sistEndredeSivilstand;
    }

    public NavnDto utledGjeldendeNavn(List<NavnDto> navn) {
        if (navn == null || navn.isEmpty()) {
            return null;
        }

        navn.sort(comparing(this::getEndringstidspunktOrNull, nullsLast(reverseOrder())));

        if (navn.size() > 1) {
            log.info("Flere gjeldende navn funnet i PDL");
        }

        var sistEndredeNavn = navn.get(0);
        if (sistEndredeNavn == null
                || flereNavnRegistrertSamtidig(sistEndredeNavn, navn)
//                Kommentert ut fordi vi ikke er 100% sikre på om vi skal vise navn fra udokumenterte kilder (master == "bruker selv").
//                Hvis disse skal filtreres vekk, kan linjen kommenteres inn igjen.
//                || erKildeUdokumentert(sistEndredeNavn)
                || !MASTERS.contains(sistEndredeNavn.getMetadata().getMaster().toUpperCase())) {
            return null;
        }
        if (erKildeUdokumentert(sistEndredeNavn.getMetadata())) {
            log.info("PDL navn er udokumentert (kilde = {})", sisteEndringOrNull(sistEndredeNavn.getMetadata()).getKilde());
        }

        return sistEndredeNavn;
    }

    private LocalDateTime getEndringstidspunktOrNull(SivilstandDto sivilstandDto) {
        return getEndringstidspunktOrNull(sivilstandDto.getMetadata(), sivilstandDto.getFolkeregistermetadata());
    }

    private LocalDateTime getEndringstidspunktOrNull(NavnDto navnDto) {
        return getEndringstidspunktOrNull(navnDto.getMetadata(), navnDto.getFolkeregistermetadata());
    }

    private LocalDateTime getEndringstidspunktOrNull(MetadataDto metadata, FolkeregistermetadataDto folkeregistermetadata) {
        if (metadata.getMaster().equalsIgnoreCase(FREG)) {
            return folkeregistermetadata.getAjourholdstidspunkt();
        } else {
            var endring = sisteEndringOrNull(metadata);
            if (endring == null) {
                log.warn("oops - endring er null?"); // Kan sikkert fjernes på sikt
            }
            return endring == null ? null : endring.getRegistrert();
        }
    }

    private EndringDto sisteEndringOrNull(MetadataDto metadata) {
        return metadata.getEndringer().stream().max(comparing(EndringDto::getRegistrert)).orElse(null);
    }

    private boolean flereSivilstanderRegistrertSamtidig(SivilstandDto first, List<SivilstandDto> list) {
        var filtered = list.stream()
                .filter(dto -> Objects.equals(getEndringstidspunktOrNull(dto), getEndringstidspunktOrNull(first)))
                .collect(Collectors.toList());
        return filtered.size() > 1;
    }

    private boolean flereNavnRegistrertSamtidig(NavnDto first, List<NavnDto> list) {
        var filtered = list.stream()
                .filter(dto -> Objects.equals(getEndringstidspunktOrNull(dto), getEndringstidspunktOrNull(first)))
                .collect(Collectors.toList());
        return filtered.size() > 1;
    }

    private boolean erKildeUdokumentert(MetadataDto metadata) {
        return PDL.equalsIgnoreCase(metadata.getMaster()) && sisteEndringOrNull(metadata) != null && sisteEndringOrNull(metadata).getKilde().equals(BRUKER_SELV);
    }
}
