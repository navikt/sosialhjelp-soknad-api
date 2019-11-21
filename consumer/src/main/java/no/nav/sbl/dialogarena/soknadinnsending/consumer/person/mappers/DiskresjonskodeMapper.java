package no.nav.sbl.dialogarena.soknadinnsending.consumer.person.mappers;


import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.Diskresjonskoder;

class DiskresjonskodeMapper {
    static String mapTilTallkode(String diskresjonskoder) {
        if (Diskresjonskoder.STRENGT_FORTROLIG_ADRESSE.kodeverkVerdi.equals(diskresjonskoder)){
            return Diskresjonskoder.STRENGT_FORTROLIG_ADRESSE.tallVerdi;
        } else if (Diskresjonskoder.FORTROLIG_ADRESSE.kodeverkVerdi.equals(diskresjonskoder)) {
            return Diskresjonskoder.FORTROLIG_ADRESSE.tallVerdi;
        } else {
            return null;
        }
    }
}
