package com.lootopia.lootopia_app.infrastructure.in.rest;


import com.lootopia.lootopia_app.application.port.in.FetchHuntUseCase;
import com.lootopia.lootopia_app.domain.model.Hunt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/hunts")
@RequiredArgsConstructor
public class HuntController {

    private final FetchHuntUseCase fetchHuntUseCase;

    @Operation(summary = "Fetch all hunts", description = "Endpoint to fetch a list of hunts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hunts fetched successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Hunt.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public List<Hunt> getHunts() {
        log.info("Fetching all hunts");
        return fetchHuntUseCase.fetchAllHunts();
    }


}
