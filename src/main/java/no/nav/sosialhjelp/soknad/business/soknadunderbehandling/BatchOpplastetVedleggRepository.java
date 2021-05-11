package no.nav.sosialhjelp.soknad.business.soknadunderbehandling;


public interface BatchOpplastetVedleggRepository {

    void slettAlleVedleggForSoknad(Long soknadId);
}
