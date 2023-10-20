package example.dollarcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import example.dollarcard.models.DollarCard;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DollarCardApplicationTests {
	private final TestRestTemplate testRestTemplate;
	@Autowired
	DollarCardApplicationTests(TestRestTemplate testRestTemplate) {
		this.testRestTemplate = testRestTemplate;
	}

	@Test
	void shouldReturnADollarCardWhenDataIsSaved() {
		ResponseEntity<String> responseEntity = testRestTemplate.withBasicAuth("mich", "12345")
				.getForEntity("/dollarcards/20", String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(responseEntity.getBody());
		var id = documentContext.read("$.id");
		var amount = documentContext.read("$.amount");
		var owner = documentContext.read("$.owner");

		assertThat(id).isEqualTo(20);
		assertThat(amount).isEqualTo(250.25);
		assertThat(owner).isEqualTo("mich");
	}

	@Test
	void shouldReturnNotFoundWhenIdIsUnknown() {
		ResponseEntity<String> responseEntity = testRestTemplate.withBasicAuth("mich", "12345")
				.getForEntity("/dollarcards/99999", String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(responseEntity.getBody()).isBlank();
	}

	@Test
	@DirtiesContext
	void shouldCreateNewDollarCard() {
		var dollarCard = new DollarCard(null, 250.00, null);
		ResponseEntity<Void> responseEntity = testRestTemplate.withBasicAuth("mich", "12345")
				.postForEntity("/dollarcards", dollarCard, Void.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewDollarCard = responseEntity.getHeaders().getLocation();
		ResponseEntity<String> response = testRestTemplate.withBasicAuth("mich", "12345")
				.getForEntity(locationOfNewDollarCard, String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		var id = documentContext.read("$.id");
		var owner = documentContext.read("$.owner");

		assertThat(id).isNotNull();
		assertThat(owner).isEqualTo("mich");
	}

	@Test
	void shouldReturnAllDollarCards() {
		ResponseEntity<String> responseEntity = testRestTemplate.withBasicAuth("mich", "12345")
				.getForEntity("/dollarcards", String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(responseEntity.getBody());
		var totalNumberOfDollarCards = documentContext.read("$.length()");
		assertThat(totalNumberOfDollarCards).isEqualTo(3);

		JSONArray ids = documentContext.read("$..id");
		JSONArray owners = documentContext.read("$..owner");
		JSONArray amounts = documentContext.read("$..amount");
		assertThat(ids).containsExactlyInAnyOrder(20, 21, 22);
		assertThat(owners).containsExactlyInAnyOrder("mich", "mich", "mich");
		assertThat(amounts).containsExactlyInAnyOrder(250.25, 20.55, 150.75);

	}

	@Test
	void shouldReturnPagesOfDollarCards() {
		ResponseEntity<String> responseEntity = testRestTemplate.withBasicAuth("mich", "12345")
				.getForEntity("/dollarcards?page=0&size=1&sort=amount,desc", String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext  documentContext = JsonPath.parse(responseEntity.getBody());
		JSONArray pages = documentContext.read("$[*]");
		assertThat(pages.size()).isEqualTo(1);

		var amount = documentContext.read("$[0].amount");
		assertThat(amount).isEqualTo(250.25);
	}

	@Test
	void shouldReturnPagesOfDollarCardsWithDefaultPageNumbersAndSorting() {
		ResponseEntity<String> responseEntity = testRestTemplate.withBasicAuth("mich", "12345")
				.getForEntity("/dollarcards", String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext  documentContext = JsonPath.parse(responseEntity.getBody());
		JSONArray pages = documentContext.read("$[*]");
		assertThat(pages.size()).isEqualTo(3);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(250.25, 20.55, 150.75);
	}

	@Test
	void shouldNotAuthenticateBadUserCredentials() {
		ResponseEntity<String> responseEntity = testRestTemplate.withBasicAuth("bad-user", "12345")
				.getForEntity("/dollarcards/20", String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		responseEntity = testRestTemplate.withBasicAuth("mich", "bad-pass")
				.getForEntity("/dollarcards/20", String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldNotReturnDollarCardForNonOwnerCard() {
		ResponseEntity<String> responseEntity = testRestTemplate.withBasicAuth("mark", "12345")
				.getForEntity("/dollarcards/23", String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void shouldNotReturnOtherUserDollarCard() {
		ResponseEntity<String> responseEntity = testRestTemplate.withBasicAuth("mich", "12345")
				.getForEntity("/dollarcards/23", String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext
	void shouldUpdateExistingDollarCard() {
		HttpEntity<DollarCard> dollarCardHttpEntity = new HttpEntity<>(new DollarCard(null, 111.12, null));
		ResponseEntity<Void> responseEntity = testRestTemplate.withBasicAuth("mich", "12345")
				.exchange("/dollarcards/20", HttpMethod.PUT, dollarCardHttpEntity, Void.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> response = testRestTemplate.withBasicAuth("mich", "12345")
				.getForEntity("/dollarcards/20", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		var id = documentContext.read("$.id");
		var amount = documentContext.read("$.amount");
		assertThat(id).isEqualTo(20);
		assertThat(amount).isEqualTo(111.12);
	}

	@Test
	@DirtiesContext
	void shouldNotUpdateNonExistingDollarCard() {
		HttpEntity<DollarCard> dollarCardHttpEntity = new HttpEntity<>(new DollarCard(null, 111.12, null));
		ResponseEntity<Void> responseEntity = testRestTemplate.withBasicAuth("mich", "12345")
				.exchange("/dollarcards/24", HttpMethod.PUT, dollarCardHttpEntity, Void.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext
	void shouldDeleteExistingDollarCardByOwner() {
		ResponseEntity<Void> responseEntity = testRestTemplate.withBasicAuth("mich", "12345")
				.exchange("/dollarcards/20", HttpMethod.DELETE, null, Void.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> response = testRestTemplate.withBasicAuth("mich", "12345")
				.getForEntity("/dollarcards/20", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotDeleteExistingDollarCardNotOwned() {
		ResponseEntity<Void> responseEntity = testRestTemplate.withBasicAuth("mich", "12345")
				.exchange("/dollarcards/24", HttpMethod.DELETE, null, Void.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

		ResponseEntity<String> response = testRestTemplate.withBasicAuth("ama", "12345")
				.getForEntity("/dollarcards/24", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void shouldNotDeleteDollarCardThatDoNotExist() {
		ResponseEntity<Void> responseEntity = testRestTemplate.withBasicAuth("mich", "12345")
				.exchange("/dollarcards/244", HttpMethod.DELETE, null, Void.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
}
