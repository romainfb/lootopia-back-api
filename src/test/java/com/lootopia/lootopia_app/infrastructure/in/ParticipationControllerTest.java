package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lootopia.lootopia_app.application.port.in.CreateParticipationUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteParticipationUseCase;
import com.lootopia.lootopia_app.application.port.in.FetchParticipationUseCase;
import com.lootopia.lootopia_app.domain.ParticipationStatut;
import com.lootopia.lootopia_app.domain.model.Participation;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.ParticipationRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.ParticipationMapper;
import com.lootopia.lootopia_app.infrastructure.security.CurrentUserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ParticipationControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CreateParticipationUseCase createParticipationUseCase;
    @Mock
    private DeleteParticipationUseCase deleteParticipationUseCase;
    @Mock
    private FetchParticipationUseCase fetchParticipationUseCase;
    @Mock
    private ParticipationMapper participationMapper;

    @InjectMocks
    private ParticipationController participationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(participationController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(CurrentUserId.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter p, ModelAndViewContainer m,
                                                  NativeWebRequest r, WebDataBinderFactory b) {
                        return 1L;
                    }
                })
                .build();
    }

    @Test
    void testCreateParticipation() throws Exception {
        ParticipationRequestDto requestDto = ParticipationRequestDto.builder()
                .huntId(37)
                .statut(ParticipationStatut.PARTICIPANT)
                .build();

        Participation participation = Participation.builder()
                .userId(1)
                .huntId(37)
                .statut(ParticipationStatut.PARTICIPANT)
                .dateInscription(new Timestamp(System.currentTimeMillis()))
                .build();

        when(createParticipationUseCase.createParticipation(any(ParticipationRequestDto.class), any(Integer.class)))
                .thenReturn(participation);

        mockMvc.perform(post("/api/participation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.huntId", is(37)))
                .andExpect(jsonPath("$.statut", is("PARTICIPANT")));
    }

    @Test
    void testDeleteParticipation() throws Exception {
        Long participationId = 10L;
        doNothing().when(deleteParticipationUseCase).deleteParticipation(participationId);

        mockMvc.perform(delete("/api/participation/{id}", participationId))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetParticipationsByHunt() throws Exception {
        Participation p1 = Participation.builder()
                .userId(1)
                .huntId(37)
                .statut(ParticipationStatut.PARTICIPANT)
                .dateInscription(new Timestamp(System.currentTimeMillis()))
                .build();
        Participation p2 = Participation.builder()
                .userId(2)
                .huntId(37)
                .statut(ParticipationStatut.ORGANISATEUR)
                .dateInscription(new Timestamp(System.currentTimeMillis()))
                .build();

        List<Participation> participations = Arrays.asList(p1, p2);
        when(fetchParticipationUseCase.findByHuntId(37)).thenReturn(participations);

        mockMvc.perform(get("/api/participation/hunt/{huntId}", 37))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId", is(1)))
                .andExpect(jsonPath("$[0].huntId", is(37)))
                .andExpect(jsonPath("$[0].statut", is("PARTICIPANT")))
                .andExpect(jsonPath("$[1].userId", is(2)))
                .andExpect(jsonPath("$[1].statut", is("ORGANISATEUR")));
    }
}
