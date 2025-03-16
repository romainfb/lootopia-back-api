package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.DeleteUserUseCase;
import com.lootopia.lootopia_app.application.port.in.GetUserUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateUserUseCase;
import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.domain.model.UserInventory;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UpdatePasswordRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserKeycloakUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    @Operation(summary = "Get user by ID", description = "Endpoint to Get user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/detail/{id_user}")
    public ResponseEntity<User> getUserById(@PathVariable @Valid Long id_user) {
        log.info("Retrieving user by id : {}", id_user);
        return getUserUseCase.getUserById(id_user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get user inventory", description = "Endpoint to Get user inventory by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/inventory/{id_user}")
    public Optional<UserInventory> getUserInventory(@PathVariable @Valid Long id_user) {
        log.info("Retrieving user inventory with id : {}", id_user);
        return getUserUseCase.getUserInventory(id_user);
    }

    @Operation(summary = "Update a user by id", description = "Endpoint to update an existing user by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Hunt.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/{id}/update")
    public UserKeycloakUpdateDto updateUser(@PathVariable Long id,
                                            @RequestBody @Valid UserUpdateDto userDto) {
        log.info("Updating user : {}", userDto);
        return updateUserUseCase.updateUser(userDto, id);
    }

    @Operation(summary = "Update user password", description = "Endpoint to update user password in Keycloak.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "update successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Hunt.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{id}/key/update")
    public ResponseEntity<Void> updateUserPassword(@PathVariable Long id,
                                                   @RequestBody @Valid UpdatePasswordRequestDto dto) {
        boolean isUpdated = updateUserUseCase.updatePassword(dto.getPassword(), id);
        if (isUpdated) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "Delete user by ID", description = "Endpoint to Delete user by ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "User deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid input",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @DeleteMapping("/{id_user}")
    public ResponseEntity<Void> deleteUserById(@PathVariable @Valid Long id_user) {
        log.info("Deleting user by id : {}", id_user);
        deleteUserUseCase.deleteUser(id_user);
        return ResponseEntity.noContent().build();
    }
}

