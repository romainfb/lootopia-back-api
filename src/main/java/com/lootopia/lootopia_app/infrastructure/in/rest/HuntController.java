package com.lootopia.lootopia_app.infrastructure.in.rest;


import com.lootopia.lootopia_app.application.port.in.FetchHuntUseCase;
import com.lootopia.lootopia_app.domain.model.Hunt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/hunts")
@RequiredArgsConstructor
public class HuntController {

    private final FetchHuntUseCase fetchHuntUseCase;

    @Operation(summary = "Fetch all hunts", description = "Endpoint to fetch a list of hunts")
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

    @Operation(summary = "Fetch hunt detail", description = "Returns detailed information about a specific hunt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hunt detail fetched successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Hunt.class))),
            @ApiResponse(responseCode = "404", description = "Hunt not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/detail/{id}")
    public Hunt getHuntDetail(@PathVariable("id") Long id) {
        log.info("Fetching hunt detail for id: {}", id);
        return fetchHuntUseCase.fetchHuntDetail(id);
    }

    @Operation(summary = "Fetch hunts by price range",
            description = "Returns a list of hunts whose participation fees are within the specified interval")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hunts fetched successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Hunt.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid price parameters", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/price")
    public List<Hunt> getHuntsByPrice(@RequestParam("min_price") Double minPrice,
                                      @RequestParam("max_price") Double maxPrice) {
        log.info("Fetching hunts with price between {} and {}", minPrice, maxPrice);
        return fetchHuntUseCase.fetchHuntsByPriceRange(minPrice, maxPrice);
    }

    @Operation(summary = "Fetch hunts by composition",
            description = "Returns a list of hunts filtered by whether they are complete or not")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hunts fetched successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Hunt.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid composition parameter", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/composition")
    public List<Hunt> getHuntsByComposition(@RequestParam("full") boolean full) {
        log.info("Fetching hunts by composition: full={}", full);
        return fetchHuntUseCase.fetchHuntsByComposition(full);
    }

    @Operation(summary = "Fetch hunts by duration",
            description = "Returns a list of hunts whose duration is less than or equal to the specified value")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hunts fetched successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Hunt.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid duration parameter", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/duration")
    public List<Hunt> getHuntsByDuration(@RequestParam("duration") Integer duration) {
        log.info("Fetching hunts with duration <= {}", duration);
        return fetchHuntUseCase.fetchHuntsByDuration(duration);
    }

    @Operation(summary = "Fetch hunts by mode",
            description = "Returns a list of hunts filtered by game mode")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hunts fetched successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Hunt.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid mode parameter", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/mode")
    public List<Hunt> getHuntsByMode(@RequestParam("mode") String mode) {
        log.info("Fetching hunts with mode: {}", mode);
        return fetchHuntUseCase.fetchHuntsByMode(mode);
    }

    @Operation(summary = "Fetch hunts by world",
            description = "Returns a list of hunts filtered by the selected world")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hunts fetched successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Hunt.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid world parameter", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/world/{world}")
    public List<Hunt> getHuntsByWorld(@PathVariable("world") String world) {
        log.info("Fetching hunts with world: {}", world);
        return fetchHuntUseCase.fetchHuntsByWorld(world);
    }

}
