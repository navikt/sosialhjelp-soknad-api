package no.nav.sbl.dialogarena.dokumentinnsending.service;


import java.util.List;

/**
 * Interface for to Webservice-tjenester mot henvendelse-applikasjonen
 */
public interface OpprettBrukerBehandlingService {

    /**
     * Oppretter en dokumentbehandling i henvendelse basert på argumentene den får inn.
     *
     * @param hovedskjemaId  Id på skjemaet som er hovedskjema for søknaden
     * @param vedleggsIder   Id på de ulike vedleggene for søknaden
     * @param erEttersending En verdi som forteller om behandlingen som skal opprettes er en vanlig dokumentbehandling eller en ettersending.
     * @return Returnerer id'en på behandlingen som er opprettet i henvendelse
     */
    String opprettDokumentBehandling(String hovedskjemaId, List<String> vedleggsIder, boolean erEttersending);

    /**
     * Metode for å teste at tjenesten er tilgjengelig
     *
     * @return Returnerer en boolean som indikerer på om tjenesten er tilgjengelig eller ikke
     */
    Boolean ping();
}