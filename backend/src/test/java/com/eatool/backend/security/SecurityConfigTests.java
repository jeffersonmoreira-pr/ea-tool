package com.eatool.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedRequestToAppRootRedirectsToLoginPage() throws Exception {
        MvcResult result = mockMvc.perform(get("/")).andReturn();

        assertThat(result.getResponse().getStatus()).isIn(302, 401, 403);
        assertThat(result.getResponse().getRedirectedUrl()).contains("/login.html");
    }

    @Test
    void healthEndpointStaysPublic() throws Exception {
        MvcResult result = mockMvc.perform(get("/actuator/health")).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }
}
