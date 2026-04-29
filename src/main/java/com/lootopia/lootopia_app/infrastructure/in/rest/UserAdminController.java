package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.DeleteUserUseCase;
import com.lootopia.lootopia_app.application.port.in.FetchAllUsersUseCase;
import com.lootopia.lootopia_app.application.port.in.GetUserUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateUserUseCase;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.domain.model.UserInventory;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final FetchAllUsersUseCase fetchAllUsersUseCase;

    @Operation(summary = "Get user (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/detail/{id_user}")
    public ResponseEntity<User> getUserById(@PathVariable @Valid Long id_user) {
        return getUserUseCase.getUserById(id_user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get user inventory (admin)")
    @GetMapping("/inventory/{id_user}")
    public ResponseEntity<UserInventory> getUserInventory(@PathVariable @Valid Long id_user) {
        return getUserUseCase.getUserInventory(id_user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update a user (admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserUpdatedDto.class)))
    })
    @PatchMapping("/update/{id_user}")
    public ResponseEntity<UserUpdatedDto> updateUser(@PathVariable Long id_user,
                                                     @RequestBody @Valid UserToUpdateDto userDto) {
        return ResponseEntity.ok(updateUserUseCase.updateUser(userDto, id_user));
    }

    @Operation(summary = "Update a user with image (admin)")
    @PatchMapping(value = "/update-with-image/{id_user}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserUpdatedDto> updateUserWithImage(@PathVariable Long id_user,
                                                              @ModelAttribute @Valid UserToUpdateDto userDto) {
        return ResponseEntity.ok(updateUserUseCase.updateUser(userDto, id_user));
    }

    @Operation(summary = "Delete a user (admin)")
    @DeleteMapping("/delete-account/{id_user}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id_user) {
        deleteUserUseCase.deleteUser(id_user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all users (admin)")
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(fetchAllUsersUseCase.getAllUsers());
    }
}
