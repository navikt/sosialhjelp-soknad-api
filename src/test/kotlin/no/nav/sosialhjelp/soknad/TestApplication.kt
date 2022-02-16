package no.nav.sosialhjelp.soknad

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

@SpringBootApplication
open class TestApplication : SpringBootServletInitializer() {

    override fun configure(builder: SpringApplicationBuilder?): SpringApplicationBuilder {
        return super.configure(builder)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            configureApplication(SpringApplicationBuilder()).run(*args)
        }

        private fun configureApplication(builder: SpringApplicationBuilder): SpringApplicationBuilder {
            return builder
                .sources(Application::class.java)
                .bannerMode(Banner.Mode.OFF)
        }
    }
}
