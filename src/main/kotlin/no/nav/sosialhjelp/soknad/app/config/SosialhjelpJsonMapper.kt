package no.nav.sosialhjelp.soknad.app.config

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

fun sosialhjelpJsonMapperBuilder(): JsonMapper.Builder =
    JsonSosialhjelpObjectMapper
        .createJsonMapperBuilder()
        .addModule(kotlinModule())
