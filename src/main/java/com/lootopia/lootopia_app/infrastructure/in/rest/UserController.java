package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.DeleteUserUseCase;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@Validated
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("Internal server error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
    }

    @Operation(summary = "Get user", description = "Endpoint to Get user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/detail/{id_user}")
    public ResponseEntity<User> getUserById(@PathVariable @Valid Long id_user) {
        log.info("Retrieving user by id : {}", id_user);
        return getUserUseCase.getUserById(id_user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get user inventory", description = "Endpoint to Get user inventory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/inventory/{id_user}")
    public ResponseEntity<UserInventory> getUserInventory(@PathVariable @Valid Long id_user) {
        log.info("Retrieving user inventory with id : {}", id_user);
        return getUserUseCase.getUserInventory(id_user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update a user", description = "Endpoint to update an existing user .")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserUpdatedDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserUpdatedDto> updateUser(@RequestBody @Valid UserToUpdateDto userDto, @AuthenticationPrincipal Jwt jwt) {
        try {
            UserUpdatedDto updatedUser = updateUserUseCase.updateUser(userDto, jwt);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Update user password", description = "Endpoint to update user password in Keycloak.")
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
    @PreAuthorize("isAuthenticated()")

    public ResponseEntity<Void> updateUserPassword(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid UpdatePasswordRequestDto dto) {
        logger.debug("updateUserPassword called with JWT: {}", jwt);
        logger.debug("updateUserPassword called with DTO: {}", dto);

        try {
            boolean isUpdated = updateUserUseCase.updatePassword(dto.getPassword(), jwt);
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

    @Operation(summary = "Delete user", description = "Endpoint to Delete user ")
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteUserById(@AuthenticationPrincipal Jwt jwt) {
        deleteUserUseCase.deleteUser(jwt);
        return ResponseEntity.noContent().build();
    }
}

