package com.lootopia.lootopia_app.infrastructure.in;

import com.lootopia.lootopia_app.application.port.in.CreateHuntUseCase;
import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.infrastructure.in.rest.HuntController;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.HuntRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.HuntMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

class HuntControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateHuntUseCase createHuntUseCase;

    @Mock
    private HuntMapper huntMapper;

    @InjectMocks
    private HuntController huntController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(huntController).build();
    }

    @Test
    void createHunt_ShouldReturnCreatedHunt() throws Exception {
        HuntRequest huntRequest = HuntRequest.builder()
                .title("Chasse au trésor")
                .description("Une grande chasse aux trésors")
                .mode("solo")
                .difficulty("moyenne")
                .participationFees(10)
                .chatEnabled(true)
                .organizerId(1)
                .build();

        Hunt hunt = new Hunt();

        when(huntMapper.huntRequestToHunt(huntRequest)).thenReturn(hunt);
        when(createHuntUseCase.createHunt(hunt)).thenReturn(hunt);

        mockMvc.perform(post("/api/admin/hunts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(huntRequest)))
                .andExpect(status().isOk());

        verify(huntMapper, times(1)).huntRequestToHunt(huntRequest);
        verify(createHuntUseCase, times(1)).createHunt(hunt);
    }

    @Test
    void createHunt_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        HuntRequest invalidRequest = HuntRequest.builder()
                .description("Sans titre")
                .mode("solo")
                .difficulty("moyenne")
                .organizerId(1)
                .build();

        mockMvc.perform(post("/api/admin/hunts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createHuntUseCase);
    }
}
