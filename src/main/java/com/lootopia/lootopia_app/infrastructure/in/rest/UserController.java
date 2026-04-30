package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.DeleteUserUseCase;
import com.lootopia.lootopia_app.application.port.in.GetUserUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateUserUseCase;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.domain.model.UserInventory;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UpdatePasswordRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserToUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdatedDto;
import com.lootopia.lootopia_app.infrastructure.security.CurrentUserId;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
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
    @GetMapping("/detail")
    public ResponseEntity<User> getUserById(@CurrentUserId Long userId) {
        return getUserUseCase.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get user inventory")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/inventory")
    public ResponseEntity<UserInventory> getUserInventory(@CurrentUserId Long userId) {
        return getUserUseCase.getUserInventory(userId)
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
    public ResponseEntity<UserUpdatedDto> updateUser(@RequestBody @Valid UserToUpdateDto userDto,
                                                     @CurrentUserId Long userId) {
        return ResponseEntity.ok(updateUserUseCase.updateUser(userDto, userId));
    }

    @Operation(summary = "Update current user with profile image")
    @PatchMapping(value = "/update-with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserUpdatedDto> updateUserWithImage(@ModelAttribute @Valid UserToUpdateDto userDto,
                                                              @CurrentUserId Long userId) {
        return ResponseEntity.ok(updateUserUseCase.updateUser(userDto, userId));
    }

    @Operation(summary = "Update current user password")
    @PutMapping("/key/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateUserPassword(@CurrentUserId Long userId,
                                                   @RequestBody @Valid UpdatePasswordRequestDto dto) {
        return updateUserUseCase.updatePassword(dto.getPassword(), userId)
                ? ResponseEntity.ok().build()
                :ResponseEntity.badRequest().build();
    }

    @Operation(summary = "Delete current user account")
    @DeleteMapping("/delete-account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteUserById(@CurrentUserId Long userId) {
        deleteUserUseCase.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get current user")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getCurrentUser(@CurrentUserId Long userId) {
        return getUserUseCase.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
