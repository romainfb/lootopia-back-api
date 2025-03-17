package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lootopia.lootopia_app.application.port.in.DeleteUserUseCase;
import com.lootopia.lootopia_app.application.port.in.GetUserUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateUserUseCase;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserToUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdatedDto;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(UserController.class)
@TestPropertySource(properties = {
        "KEYCLOAK_REALM_URI=none"
})
@Import(UserController.class)  // Importer juste votre contrôleur
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})  // Exclure la sécurité
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private GetUserUseCase getUserUseCase;

    @Mock
    private UpdateUserUseCase updateUserUseCase;

    @Mock
    private DeleteUserUseCase deleteUserUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    Jwt fakeJwt = new Jwt(
            "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4STYyVFlSR2tmYWMwVU15dlk1djVhaVJtaU1lYmpWTjlqQTZHTDB4ODg0In0.eyJleHAiOjE3NDIyMzQ2MTEsImlhdCI6MTc0MjIzNDU1MSwiYXV0aF90aW1lIjoxNzQyMjM0NTMxLCJqdGkiOiI5MTk4ZTI3Yy1mYTZhLTQ2Y2ItYjY2ZS1kNDM2OWU4NjIzMTMiLCJpc3MiOiJodHRwczovL2tleWNsb2FrLWxvb3RvcGlhLnJvbWFpbmZiLmZyL3JlYWxtcy9tYXN0ZXIiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiYjM5OWVhM2MtMDkwMi00Y2Q4LTgzODMtYTliZGFiNmQyMmE5IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibG9vdG9waWFfd2ViIiwic2lkIjoiNGU3MjQ5ZTMtNjUwYy00OGExLWFjMjMtZWI4NjQ2OTY5M2IyIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLW1hc3RlciIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoidGVzdCB0ZXN0IiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGhpYmF1bHQiLCJnaXZlbl9uYW1lIjoidGVzdCIsImZhbWlseV9uYW1lIjoidGVzdCIsImVtYWlsIjoidGVzdEBnbWFpbC5jb20ifQ.E9-36VUAW_gsJFoGv_LC6_ywuXCDnB1Gn2leWeoSeYbdUBrv6442tVQQGtIcJgps-1cD2Q6dNoG6TpRh0Yrywg0fZ_OuKtltdNCcFMKJQJBYJTbaNrvuALVjiO35zpMRsdjX2X9X3wbvgKB_I8iS8hA-4jqVlpSPGqiKlTVtVWasTYheqL5RSB6K2QK48Urrwvo3mk3EsRbaQGzAOg-OcPvCG9EYjvoEXZzpbn7PSsZZISGaBh_qsXHJPkKCsYaXCGm-FAxbB4aOPBWyPUstEVfCI-NFfXCuWkebtgsUDXE_oDWTFEzr3ut-1fVS5611HQ61996uQaY_W6VDCRVTmA"
            , Instant.ofEpochSecond(1710642400), Instant.ofEpochSecond(1710646000),
            Map.of("alg", "HS256", "typ", "JWT"), Map.of("sub", "keycloak-123", "email", "testUser@example.com"));

    @Test
    void getUserById_ShouldReturnUserWhenExists() throws Exception {
        Long id = 1L;
        User user = new User();
        when(getUserUseCase.getUserById(id)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/detail/" + id)
                        .with(jwt().jwt(fakeJwt)))
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
        UserToUpdateDto updateRequest = new UserToUpdateDto("email@gmail.com", "firstName", "lastName", "username");

        UserUpdatedDto updatedUser = UserUpdatedDto.builder()
                .id("keycloak-id")
                .username("username")
                .email("email@gmail.com")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        when(updateUserUseCase.updateUser(eq(updateRequest), eq(fakeJwt))).thenReturn(updatedUser);

        mockMvc.perform(patch("/api/users/update")
                        .with(jwt().jwt(fakeJwt)) // Injection correcte du JWT
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("username"))
                .andExpect(jsonPath("$.email").value("email@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("firstName"))
                .andExpect(jsonPath("$.lastName").value("lastName"));

        verify(updateUserUseCase, times(1)).updateUser(eq(updateRequest), eq(fakeJwt));
    }
}

