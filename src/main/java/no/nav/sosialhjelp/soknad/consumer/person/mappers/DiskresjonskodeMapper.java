package no.nav.sosialhjelp.soknad.consumer.person.mappers;


import no.nav.sosialhjelp.soknad.consumer.person.domain.Diskresjonskoder;

final class DiskresjonskodeMapper {

    private DiskresjonskodeMapper() {
    }

    static String mapTilTallkode(String diskresjonskode) {
        if (Diskresjonskoder.STRENGT_FORTROLIG_ADRESSE.kodeverkVerdi.equals(diskresjonskode)){
            return Diskresjonskoder.STRENGT_FORTROLIG_ADRESSE.tallVerdi;
        } else if (Diskresjonskoder.FORTROLIG_ADRESSE.kodeverkVerdi.equals(diskresjonskode)) {
            return Diskresjonskoder.FORTROLIG_ADRESSE.tallVerdi;
        } else {
            return null;
        }
    }
}
