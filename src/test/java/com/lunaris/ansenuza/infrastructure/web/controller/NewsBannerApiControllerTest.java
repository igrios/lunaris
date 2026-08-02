package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.application.usecase.NewsBannerService;
import com.lunaris.ansenuza.domain.model.NewsBanner;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class NewsBannerApiControllerTest {

    @Test
    void returnsActiveBannerContract() throws Exception {
        NewsBannerService service = mock(NewsBannerService.class);
        NewsBanner banner = new NewsBanner();
        banner.setId(UUID.randomUUID());
        banner.setTitle("Promo agosto");
        banner.setImageUrl("https://res.cloudinary.com/flyer.jpg");
        banner.setActive(true);
        banner.setValidUntil(LocalDate.of(2026, 8, 31));
        when(service.findActive()).thenReturn(List.of(banner));
        var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var mockMvc = MockMvcBuilders.standaloneSetup(new NewsBannerApiController(service))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        mockMvc.perform(get("/api/v1/news-banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Promo agosto"))
                .andExpect(jsonPath("$[0].imageUrl").value("https://res.cloudinary.com/flyer.jpg"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[0].validUntil").value("2026-08-31"));
    }
}
