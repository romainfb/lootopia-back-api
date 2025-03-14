package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.GetUserByIdUseCase;
import com.lootopia.lootopia_app.application.port.in.GetUserInventoryByIdUseCase;
import com.lootopia.lootopia_app.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final GetUserByIdUseCase getUserByIdUseCase;
    private final GetUserInventoryByIdUseCase getUserInventoryUseCase;

    @Operation(summary = "Get user by ID", description = "Endpoint to Get user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/detail/{id_user}")
    public Optional<User> getUserById(@PathVariable @Valid Long id_user) {
        log.info("Retrieving user by id : {}", id_user);
        return getUserByIdUseCase.getUserById(id_user);
    }

    @Operation(summary = "Get user inventory", description = "Endpoint to Get user inventory by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/inventory/{id_user}")
    public Optional<User> getUserInventory(@PathVariable @Valid Long id_user) {
        log.info("Retrieving user inventory with id : {}", id_user);
        return getUserInventoryUseCase.getUserInventory(id_user);
    }


}
