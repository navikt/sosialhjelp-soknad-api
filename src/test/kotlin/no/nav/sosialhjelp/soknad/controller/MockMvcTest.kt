package no.nav.sosialhjelp.soknad.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc

@AutoConfigureMockMvc
@ActiveProfiles("no-interceptor","no-redis")
abstract class MockMvcTest {

    @Autowired
    protected lateinit var mockMvc: MockMvc

}