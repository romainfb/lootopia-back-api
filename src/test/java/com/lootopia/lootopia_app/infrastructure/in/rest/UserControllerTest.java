package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lootopia.lootopia_app.application.port.in.DeleteUserUseCase;
import com.lootopia.lootopia_app.application.port.in.GetUserUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateUserUseCase;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UpdatePasswordRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserKeycloakUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetUserUseCase getUserUseCase;

    @Mock
    private UpdateUserUseCase updateUserUseCase;

    @Mock
    private DeleteUserUseCase deleteUserUseCase;

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
    void updateUser_ShouldReturnUpdatedUserWhenSuccessful() throws Exception {
        Long id = 1L;
        UserUpdateDto updateRequest = new UserUpdateDto("email@gmail.com", "firstName", "lastName", "username");

        UserKeycloakUpdateDto updatedUser = UserKeycloakUpdateDto.builder()
                .id("keycloak-id")
                .username("username")
                .email("email@gmail.com")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        when(updateUserUseCase.updateUser(eq(updateRequest), eq(id))).thenReturn(updatedUser);

        mockMvc.perform(patch("/api/users/" + id + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("username"))
                .andExpect(jsonPath("$.email").value("email@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("firstName"))
                .andExpect(jsonPath("$.lastName").value("lastName"));


        verify(updateUserUseCase, times(1)).updateUser(eq(updateRequest), eq(id));
    }

    @Test
    void updateUserPassword_ShouldReturnOkWhenSuccessful() throws Exception {
        Long id = 1L;
        UpdatePasswordRequestDto dto = new UpdatePasswordRequestDto("newPassword");

        when(updateUserUseCase.updatePassword(eq(dto.getPassword()), eq(id))).thenReturn(true);

        mockMvc.perform(put("/api/users/" + id + "/key/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(updateUserUseCase, times(1)).updatePassword(eq(dto.getPassword()), eq(id));
    }

    @Test
    void updateUserPassword_ShouldReturnBadRequestWhenUpdateFails() throws Exception {
        Long id = 1L;
        UpdatePasswordRequestDto dto = new UpdatePasswordRequestDto("newPassword");

        when(updateUserUseCase.updatePassword(eq(dto.getPassword()), eq(id))).thenReturn(false);

        mockMvc.perform(put("/api/users/" + id + "/key/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(updateUserUseCase, times(1)).updatePassword(eq(dto.getPassword()), eq(id));
    }

    @Test
    void deleteUser_ShouldReturnNoContentWhenSuccessful() throws Exception {
        Long id = 1L;
        doNothing().when(deleteUserUseCase).deleteUser(id);

        mockMvc.perform(delete("/api/users/" + id))
                .andExpect(status().isNoContent());

        verify(deleteUserUseCase, times(1)).deleteUser(id);
    }
}
