package no.nav.sbl.dialogarena.sendsoknad.domain;

/**
 * HendelseTypen AVBRUTT_AUTOMATISK er ikke i bruk første gang denne featuren går i prod
 * Det er fordi den foreslåtte løsningen om å spørre henvendelse om søknaden er avbrutt viser seg å oppdatere 'SIST_OPPDATERT'-datoen og dermed fornye
 * søknaden i ny 56 dager.
 *
 * Grunnen til at vi ønsker å registere denne hendelsestypen var for å finne ut om alle søknader UNDER_ARBEID er migrert fra en versjon til en annen.
 * Når man første eller andre gang tar i bruk migreringsfunksjonaliteten må man ha et forhold til dette , eller man må i mellomtiden implementere en
 * korrekt oppdatering av hendelsestabellen med AVBRUTT_AUTOMATISK-hendelser også.
 *
 * **/

public enum HendelseType {
    OPPRETTET,
    MIGRERT,
    HENTET_FRA_HENVENDELSE,
    LAGRET_I_HENVENDELSE,
    INNSENDT,
    AVBRUTT_AV_BRUKER,
    AVBRUTT_AUTOMATISK;
}
