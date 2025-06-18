package com.lootopia.lootopia_app.infrastructure.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lootopia.lootopia_app.application.port.in.FetchHuntUseCase;
import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.infrastructure.in.rest.HuntController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HuntControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FetchHuntUseCase fetchHuntUseCase;

    @InjectMocks
    private HuntController huntController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(huntController).build();
    }

    @Test
    void testGetHunts_ShouldReturnListOfHunts() throws Exception {
        Hunt hunt1 = Hunt.builder().id(1L).title("Hunt 1").build();
        Hunt hunt2 = Hunt.builder().id(2L).title("Hunt 2").build();
        List<Hunt> hunts = Arrays.asList(hunt1, hunt2);

        when(fetchHuntUseCase.fetchAllHunts()).thenReturn(hunts);

        mockMvc.perform(get("/api/hunts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Hunt 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Hunt 2")));

        verify(fetchHuntUseCase).fetchAllHunts();
    }

    @Test
    void testGetHuntDetail_ShouldReturnHuntDetail() throws Exception {
        Long id = 1L;
        Hunt hunt = Hunt.builder().id(id).title("Detail Hunt").build();

        when(fetchHuntUseCase.fetchHuntDetail(id)).thenReturn(hunt);

        mockMvc.perform(get("/api/hunts/detail/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Detail Hunt")));

        verify(fetchHuntUseCase).fetchHuntDetail(id);
    }

    @Test
    void testGetHuntsByPrice_ShouldReturnHuntsWithinPriceRange() throws Exception {
        Double minPrice = 10.0;
        Double maxPrice = 50.0;
        Hunt hunt = Hunt.builder().id(1L).title("Price Hunt").participationFees(30).build();
        List<Hunt> hunts = Collections.singletonList(hunt);

        when(fetchHuntUseCase.fetchHuntsByPriceRange(minPrice, maxPrice)).thenReturn(hunts);

        mockMvc.perform(get("/api/hunts/price")
                        .param("min_price", minPrice.toString())
                        .param("max_price", maxPrice.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Price Hunt")))
                .andExpect(jsonPath("$[0].participationFees", is(30)));

        verify(fetchHuntUseCase).fetchHuntsByPriceRange(minPrice, maxPrice);
    }

    @Test
    void testGetHuntsByComposition_ShouldReturnHuntsBasedOnComposition() throws Exception {
        boolean full = true;
        Hunt hunt = Hunt.builder().id(1L).title("Composition Hunt").numberOfParticipants(5).build();
        List<Hunt> hunts = Collections.singletonList(hunt);

        when(fetchHuntUseCase.fetchHuntsByComposition(full)).thenReturn(hunts);

        mockMvc.perform(get("/api/hunts/composition")
                        .param("full", String.valueOf(full))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Composition Hunt")));

        verify(fetchHuntUseCase).fetchHuntsByComposition(full);
    }

    @Test
    void testGetHuntsByDuration_ShouldReturnHuntsWithinDuration() throws Exception {
        Integer duration = 120;
        Hunt hunt = Hunt.builder().id(1L).title("Duration Hunt").duration(100).build();
        List<Hunt> hunts = Collections.singletonList(hunt);

        when(fetchHuntUseCase.fetchHuntsByDuration(duration)).thenReturn(hunts);

        mockMvc.perform(get("/api/hunts/duration")
                        .param("duration", duration.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Duration Hunt")))
                .andExpect(jsonPath("$[0].duration", is(100)));

        verify(fetchHuntUseCase).fetchHuntsByDuration(duration);
    }

    @Test
    void testGetHuntsByMode_ShouldReturnHuntsByMode() throws Exception {
        String mode = "Adventure";
        Hunt hunt = Hunt.builder().id(1L).title("Mode Hunt").mode("Adventure").build();
        List<Hunt> hunts = Collections.singletonList(hunt);

        when(fetchHuntUseCase.fetchHuntsByMode(mode)).thenReturn(hunts);

        mockMvc.perform(get("/api/hunts/mode")
                        .param("mode", mode)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Mode Hunt")))
                .andExpect(jsonPath("$[0].mode", is("Adventure")));

        verify(fetchHuntUseCase).fetchHuntsByMode(mode);
    }

    @Test
    void testGetHuntsByWorld_ShouldReturnHuntsByWorld() throws Exception {
        String world = "AR";
        Hunt hunt = Hunt.builder().id(1L).title("World Hunt").world("AR").build();
        List<Hunt> hunts = Collections.singletonList(hunt);

        when(fetchHuntUseCase.fetchHuntsByWorld(world)).thenReturn(hunts);

        mockMvc.perform(get("/api/hunts/world/{world}", world)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("World Hunt")))
                .andExpect(jsonPath("$[0].world", is("AR")));

        verify(fetchHuntUseCase).fetchHuntsByWorld(world);
    }
}
