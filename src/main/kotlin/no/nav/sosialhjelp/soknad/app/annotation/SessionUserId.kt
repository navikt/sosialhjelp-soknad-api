package no.nav.sosialhjelp.soknad.app.annotation

/**
 * BrukerID fra token via Tilgangskontroll, kaster exception hvis bruker ikke autentisert
 * eller har adressebeskyttelse.
 *
 * @see no.nav.sosialhjelp.soknad.app.resolver.SessionUserIdArgumentResolver
 */
annotation class SessionUserId
