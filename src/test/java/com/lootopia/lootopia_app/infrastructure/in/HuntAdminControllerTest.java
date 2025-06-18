package com.lootopia.lootopia_app.infrastructure.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lootopia.lootopia_app.application.port.in.DeleteHuntUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateHuntUseCase;
import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.infrastructure.in.rest.HuntAdminController;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.HuntUpdateRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.HuntMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HuntAdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UpdateHuntUseCase updateHuntUseCase;

    @Mock
    private DeleteHuntUseCase deleteHuntUseCase;

    @Mock
    private HuntMapper huntMapper;

    @InjectMocks
    private HuntAdminController huntAdminController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(huntAdminController).build();
    }

    @Test
    void updateHunt_ShouldReturnUpdatedHunt() throws Exception {
        Long id = 1L;
        HuntUpdateRequestDto updateRequest = HuntUpdateRequestDto.builder()
                .title("Titre mis à jour")
                .build();
        Hunt huntFromMapper = Hunt.builder()
                .title("Titre mis à jour")
                .build();
        Hunt updatedHunt = Hunt.builder()
                .id(id)
                .title("Titre mis à jour")
                .description("Ancienne description")
                .build();

        when(huntMapper.huntUpdateRequestToHunt(updateRequest)).thenReturn(huntFromMapper);
        when(updateHuntUseCase.updateHunt(eq(id), any(Hunt.class))).thenReturn(updatedHunt);

        mockMvc.perform(patch("/api/admin/hunts/" + id + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Titre mis à jour"));

        verify(huntMapper, times(1)).huntUpdateRequestToHunt(updateRequest);
        verify(updateHuntUseCase, times(1)).updateHunt(eq(id), any(Hunt.class));
    }

    @Test
    void deleteHunt_ShouldReturnNoContent() throws Exception {
        Long id = 1L;
        doNothing().when(deleteHuntUseCase).deleteHunt(id);

        mockMvc.perform(delete("/api/admin/hunts/" + id))
                .andExpect(status().isNoContent());

        verify(deleteHuntUseCase, times(1)).deleteHunt(id);
    }
}
