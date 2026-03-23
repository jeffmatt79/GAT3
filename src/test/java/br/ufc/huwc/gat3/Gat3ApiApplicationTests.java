package br.ufc.huwc.gat3;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class Gat3ApiApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void testUnauthenticatedEndpoint() {
		String body = this.restTemplate.getForObject("/test", String.class);
		assertEquals("The unauthenticated test worked!", body);
	}

	@Test
	void testAuthenticatedEndpoint() {
		String body = this.restTemplate.getForObject("/authenticatedtest", String.class);
		assertEquals("The authenticated test worked!", body);
	}
}
