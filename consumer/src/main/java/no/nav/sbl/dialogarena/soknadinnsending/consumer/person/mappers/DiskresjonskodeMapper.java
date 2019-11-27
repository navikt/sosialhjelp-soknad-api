package no.nav.sbl.dialogarena.soknadinnsending.consumer.person.mappers;


import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.Diskresjonskoder;

class DiskresjonskodeMapper {
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
