package dev.chojo.simplepos.configuration;

import dev.chojo.simplepos.TestcontainersConfiguration;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class SecurityHeadersTest {
    @Autowired
    WebApplicationContext context;
    @Autowired
    @Qualifier("springSecurityFilterChain")
    Filter springSecurityFilterChain;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
    }

    @Test
    void shouldSendTheContentSecurityPolicy() throws Exception {
        mvc.perform(get("/api/version"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", WebSecurityConfig.CONTENT_SECURITY_POLICY));
    }

    /**
     * Without an {@code Access-Control-Allow-Origin} header the browser refuses the cross-origin request itself.
     */
    @Test
    void shouldRefuseOtherOriginsByDefault() throws Exception {
        mvc.perform(options("/api/version")
                        .header("Origin", "https://elsewhere.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void shouldAnswerTheSameOriginAsUsual() throws Exception {
        mvc.perform(get("/api/version").header("Origin", "http://localhost"))
                .andExpect(status().isOk());
    }
}
