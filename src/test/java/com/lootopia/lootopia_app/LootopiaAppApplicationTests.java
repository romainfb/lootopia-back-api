package com.lootopia.lootopia_app;

import com.lootopia.lootopia_app.application.port.in.CreateHuntUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteHuntUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateHuntUseCase;
import com.lootopia.lootopia_app.config.DotenvTestConfig;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.HuntMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"KEYCLOAK_REALM_URI=http://localhost:8080/realms/lootopia"
})
@Import(DotenvTestConfig.class)
class LootopiaAppApplicationTests {


	@MockitoBean
	private CreateHuntUseCase createHuntUseCase;

	@MockitoBean
	private UpdateHuntUseCase updateHuntUseCase;

	@MockitoBean
	private DeleteHuntUseCase deleteHuntUseCase;

	@MockitoBean
	private HuntMapper huntMapper;

	@Test
	void contextLoads() {
	}

}
