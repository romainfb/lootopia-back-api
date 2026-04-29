package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.GetUserUseCase;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.domain.model.UserInventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {
    //TODO: Implement tests for UserController

    private MockMvc mockMvc;

    @Mock
    private GetUserUseCase getUserUseCase;

    @InjectMocks
    private UserController userController;

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

}
