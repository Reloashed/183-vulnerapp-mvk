package ch.bbw.m183.vulnerapp;

import ch.bbw.m183.vulnerapp.datamodel.BlogEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.Cookie;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VulnerApplicationTests {

	@Autowired
	WebTestClient webTestClient;

	HttpClient httpClient = HttpClient.newHttpClient();

	@Test
	void AnonymousUsers_LandingPage() {
		webTestClient.get().uri("/").exchange().expectStatus().isOk();
	}

	@Test
	void AnonymousUsers_GetBlogs() {
		webTestClient.get().uri("/api/blog").exchange().expectStatus().isOk();
	}

	@Test
	void AnonymousUsers_GetHealthActuator() {
		//TODO
		webTestClient.get().uri("/actuator/health").exchange().expectStatus().isOk();
	}

	@Test
	void AuthenticatedUserNoCsrf_PostBlog() throws IOException, InterruptedException {
		var result = webTestClient.post().uri("/login")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(BodyInserters.fromFormData("username", "user").with("password", "password"))
				.exchange().returnResult();
		String jsessionid = result.getResponseCookies().get("JSESSIONID").getFirst().getValue();
		webTestClient.get().uri("/api/user/whoami").header("Authorization", "Basic ").cookie("JSESSIONID", jsessionid).exchange().expectStatus().isOk();
	}

	@Test
	void AuthenticatedUserWithCsrf() {

	}

	@Test
	void AuthenticatedAdminWithCsrf() {

	}



	private record BlogRequest(String title, String body) {}
}
