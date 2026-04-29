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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

    @Operation(summary = "Get user", description = "Endpoint to get a user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json"))
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/detail/{id_user}")
    public ResponseEntity<User> getUserById(@PathVariable @Valid Long id_user) {
        return getUserUseCase.getUserById(id_user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get user inventory")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/inventory/{id_user}")
    public ResponseEntity<UserInventory> getUserInventory(@PathVariable @Valid Long id_user) {
        return getUserUseCase.getUserInventory(id_user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserUpdatedDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PatchMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserUpdatedDto> updateUser(@RequestBody @Valid UserToUpdateDto userDto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(updateUserUseCase.updateUser(userDto, currentUserId(jwt)));
    }

    @Operation(summary = "Update current user with profile image")
    @PatchMapping(value = "/update-with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserUpdatedDto> updateUserWithImage(@ModelAttribute @Valid UserToUpdateDto userDto,
                                                              @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(updateUserUseCase.updateUser(userDto, currentUserId(jwt)));
    }

    @Operation(summary = "Update current user password")
    @PutMapping("/key/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateUserPassword(@AuthenticationPrincipal Jwt jwt,
                                                   @RequestBody @Valid UpdatePasswordRequestDto dto) {
        return updateUserUseCase.updatePassword(dto.getPassword(), currentUserId(jwt))
                ? ResponseEntity.ok().build()
                :ResponseEntity.badRequest().build();
    }

    @Operation(summary = "Delete current user account")
    @DeleteMapping("/delete-account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteUserById(@AuthenticationPrincipal Jwt jwt) {
        deleteUserUseCase.deleteUser(currentUserId(jwt));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get current user")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return getUserUseCase.getUserById(currentUserId(jwt))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private Long currentUserId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
