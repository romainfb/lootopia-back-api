package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lootopia.lootopia_app.application.port.in.GetUserUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateUserUseCase;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.domain.model.UserInventory;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserToUpdateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetUserUseCase getUserUseCase;

    @Mock
    private UpdateUserUseCase updateUserUseCase;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void getUserById_ShouldReturnUserWhenExists() throws Exception {
        Long id = 1L;
        User user = new User();
        when(getUserUseCase.getUserById(id)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/detail/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());

        verify(getUserUseCase, times(1)).getUserById(id);
    }

    @Test
    void getUserById_ShouldReturnNotFoundWhenNotExists() throws Exception {
        Long id = 1L;
        when(getUserUseCase.getUserById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/detail/" + id))
                .andExpect(status().isNotFound());

        verify(getUserUseCase, times(1)).getUserById(id);
    }

    @Test
    void getUserInventory_ShouldReturnOkWhenInventoryExists() throws Exception {
        Long id = 1L;
        UserInventory inventory = new UserInventory();
        when(getUserUseCase.getUserInventory(id)).thenReturn(Optional.of(inventory));

        mockMvc.perform(get("/api/users/inventory/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(getUserUseCase, times(1)).getUserInventory(id);
    }

    @Test
    void getUserInventory_ShouldReturnNotFoundWhenInventoryDoesNotExist() throws Exception {
        Long id = 1L;
        when(getUserUseCase.getUserInventory(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/inventory/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(getUserUseCase, times(1)).getUserInventory(id);
    }

    @Test
    void updateUser_ShouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        UserToUpdateDto userToUpdateDto = UserToUpdateDto.builder()
                .username("username")
                .email("invalid-email") // Email invalide
                .firstName("firstName")
                .lastName("lastName")
                .build();

        mockMvc.perform(patch("/api/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToUpdateDto)))
                .andExpect(status().isBadRequest());

        verify(updateUserUseCase, never()).updateUser(any(), any());
    }


}
