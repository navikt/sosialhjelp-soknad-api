package no.nav.sosialhjelp.soknad.client.config

import io.netty.resolver.DefaultAddressResolverGroup
import reactor.netty.http.client.HttpClient

fun unproxiedHttpClient(): HttpClient = HttpClient
    .newConnection()
    .resolver(DefaultAddressResolverGroup.INSTANCE)
