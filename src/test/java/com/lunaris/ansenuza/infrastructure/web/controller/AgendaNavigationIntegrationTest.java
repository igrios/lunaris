package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.agenda.AgendaDayView;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@ExtendWith(MockitoExtension.class)
class AgendaNavigationIntegrationTest {

    @Mock private ReservationRepository reservations;
    @Mock private WaitingListRepository waitingList;
    @Mock private SystemConfigurationService configuration;
    @InjectMocks private AgendaViewController controller;

    private MockMvc mvc;
    private AnnotationConfigWebApplicationContext context;

    @BeforeEach
    void setUp() {
        var templates = new ClassLoaderTemplateResolver();
        templates.setPrefix("templates/");
        templates.setSuffix(".html");
        templates.setCharacterEncoding("UTF-8");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templates);
        var views = new ThymeleafViewResolver();
        views.setTemplateEngine(engine);
        views.setCharacterEncoding("UTF-8");
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(DelegatingWebMvcConfiguration.class);
        context.addBeanFactoryPostProcessor(beans -> {
            beans.registerSingleton("agendaViewController", controller);
            beans.registerSingleton("thymeleafViewResolver", views);
            views.setApplicationContext(context);
        });
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void followsRenderedNavigationAndSubmitsTheDateSelector() throws Exception {
        LocalDate today = ArgentinaTime.today();
        var home = mvc.perform(get("/agenda")).andExpect(status().isOk()).andReturn();
        assertRange(home, today, 7);
        String homeHtml = home.getResponse().getContentAsString();

        var past = follow(homeHtml, "Semana anterior");
        assertRange(past, today.minusWeeks(1), 7);
        var future = follow(homeHtml, "Día siguiente");
        assertRange(future, today.plusDays(1), 7);
        assertRange(follow(homeHtml, "Día anterior"), today.minusDays(1), 7);
        assertRange(follow(homeHtml, "Semana siguiente"), today.plusWeeks(1), 7);

        String futureHtml = future.getResponse().getContentAsString();
        assertThat(futureHtml).contains("action=\"/agenda\" method=\"get\"",
                "name=\"from\" type=\"date\"", "name=\"days\" value=\"7\"");
        LocalDate december = LocalDate.of(today.getYear() + 1, 12, 18);
        var selected = mvc.perform(get("/agenda").param("from", december.toString()).param("days", "7"))
                .andExpect(status().isOk()).andReturn();
        assertRange(selected, december, 7);
        String selectedHtml = selected.getResponse().getContentAsString();
        assertThat(selectedHtml).contains("value=\"" + december + "\"",
                "href=\"/agenda?from=" + december + "&amp;days=14\"");
        assertRange(mvc.perform(get("/agenda").param("from", december.toString()).param("days", "14"))
                .andExpect(status().isOk()).andReturn(), december, 14);
        assertThat(selectedHtml).contains("href=\"/agenda?days=7\"");
        assertRange(mvc.perform(get("/agenda?days=7")).andExpect(status().isOk()).andReturn(), today, 7);
    }

    @Test
    void includesAllDailyMetricsInAHistoricalRange() throws Exception {
        LocalDate from = ArgentinaTime.today().minusMonths(2);
        LocalDate date = from.plusDays(6);
        var confirmed = Reservation.builder().travelDate(date).reservationCode("CONF")
                .passengerCount(2).status("CONFIRMED").paymentVerified(true).build();
        var pending = Reservation.builder().travelDate(date).reservationCode("PEND")
                .passengerCount(3).paymentVerified(false).build();
        when(reservations.findAgendaBetween(from, date)).thenReturn(List.of(confirmed, pending));
        when(waitingList.sumPassengerCountByTravelDateAndStatus(any(LocalDate.class), eq(WaitingListEntry.WAITING)))
                .thenAnswer(invocation -> date.equals(invocation.getArgument(0)) ? 4L : 0L);

        var result = mvc.perform(get("/agenda").param("from", from.toString()))
                .andExpect(status().isOk()).andReturn();

        var days = (List<?>) result.getModelAndView().getModel().get("agenda");
        var summary = (AgendaDayView) days.getLast();
        assertThat(summary.date()).isEqualTo(date);
        assertThat(summary.totalPassengers()).isEqualTo(5);
        assertThat(summary.confirmedPassengers()).isEqualTo(2);
        assertThat(summary.waitingListPassengers()).isEqualTo(4);
        assertThat(summary.pendingPayments()).isEqualTo(1);
        assertThat(summary.estimatedVehicles()).isEqualTo(2);
        verify(reservations).findAgendaBetween(from, date);
    }

    @Test
    void hidesBackwardControlsAtTheHistoricalLimit() throws Exception {
        var result = mvc.perform(get("/agenda").param("from", "0001-01-01"))
                .andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("Día anterior", "Semana anterior")
                .contains("min=\"0001-01-01\"", "Día siguiente");
    }

    private MvcResult follow(String html, String label) throws Exception {
        var link = Pattern.compile("href=\"([^\"]+)\"[^>]*aria-label=\"" + label + "\"").matcher(html);
        assertThat(link.find()).isTrue();
        return mvc.perform(get(URI.create(link.group(1).replace("&amp;", "&"))))
                .andExpect(status().isOk()).andReturn();
    }

    private void assertRange(MvcResult result, LocalDate from, int days) throws Exception {
        LocalDate through = from.plusDays(days - 1L);
        var model = result.getModelAndView().getModel();
        assertThat(model.get("range")).isEqualTo(new AgendaDateRange(from, days));
        assertThat((List<?>) model.get("agenda")).hasSize(days);
        var format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        assertThat(result.getResponse().getContentAsString())
                .contains("datetime=\"" + from + "\">" + from.format(format),
                        "datetime=\"" + through + "\">" + through.format(format));
    }
}
