package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.DeleteUserUseCase;
import com.lootopia.lootopia_app.application.port.in.FetchAllUsersUseCase;
import com.lootopia.lootopia_app.application.port.in.GetUserUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateUserUseCase;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.domain.model.UserInventory;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UpdatePasswordRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserToUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdatedDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private static final Logger logger = LoggerFactory.getLogger(UserAdminController.class);
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final FetchAllUsersUseCase fetchAllUsersUseCase;

    @Operation(summary = "Get user", description = "Admin endpoint to Get user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/detail/{id_user}")
    public ResponseEntity<User> getUserById(@PathVariable @Valid Long id_user) {
        log.info("Admin retrieving user by id : {}", id_user);
        return getUserUseCase.getUserById(id_user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get user inventory", description = "Admin endpoint to Get user inventory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/inventory/{id_user}")
    public ResponseEntity<UserInventory> getUserInventory(@PathVariable @Valid Long id_user) {
        log.info("Admin retrieving user inventory with id : {}", id_user);
        return getUserUseCase.getUserInventory(id_user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update a user", description = "Admin endpoint to update an existing user.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserUpdatedDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/update")
    public ResponseEntity<UserUpdatedDto> updateUser(@RequestBody @Valid UserToUpdateDto userDto, @AuthenticationPrincipal Jwt jwt) {
        try {
            String userId = jwt.getSubject();
            UserUpdatedDto updatedUser = updateUserUseCase.updateUser(userDto, userId);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Update user password", description = "Admin endpoint to update user password in Keycloak.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "update successful",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/key/update")
    public ResponseEntity<Void> updateUserPassword(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid UpdatePasswordRequestDto dto) {
        try {
            String userId = jwt.getSubject();
            boolean isUpdated = updateUserUseCase.updatePassword(dto.getPassword(), userId);
            logger.debug("updatePassword result: {}", isUpdated);

            if (isUpdated) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException in updateUserPassword: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Exception in updateUserPassword: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Delete user", description = "Admin endpoint to Delete user")
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
    @DeleteMapping("/delete-account")
    public ResponseEntity<Void> deleteUserById(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        deleteUserUseCase.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all users", description = "Admin endpoint to retrieve all users from Keycloak")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all users",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to access this resource",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/all")
    public ResponseEntity<List<UserRepresentation>> getAllUsers() {
        log.info("Admin retrieving all users from Keycloak");
        try {
            List<UserRepresentation> users = fetchAllUsersUseCase.getAllUsers();
            log.info("Successfully retrieved {} users from Keycloak", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error retrieving users from Keycloak", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
