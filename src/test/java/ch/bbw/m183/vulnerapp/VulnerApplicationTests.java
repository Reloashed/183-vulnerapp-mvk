package ch.bbw.m183.vulnerapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Base64;
import java.util.List;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VulnerApplicationTests {

	@Autowired
	WebTestClient webTestClient;

	@Test
	void AnonymousUser_LandingPage() {
		webTestClient.get().uri("/").exchange().expectStatus().isOk();
	}

	@Test
	void AnonymousUser_GetBlogs_WithoutDetails() {
		webTestClient.get().uri("/api/blog").exchange().expectStatus().isOk();
	}

	@Test
	void AnonymousUser_HealthEndpoint_WithoutDetails() {
		webTestClient.get().uri("/api/blog/health").exchange().expectStatus().isOk();
	}

	@Test
	void AnonymousUser_PostBlogs_Forbidden() {
		webTestClient.post().uri("/api/blog").bodyValue(new BlogRequest("title", "body")).exchange().expectStatus().isForbidden();
	}

	@Test
	void AnonymousUser_Whoami_Unauthorized() {
		webTestClient.get().uri("/api/user/whoami").exchange().expectStatus().isUnauthorized();
	}

	@Test
	void AnonymousUser_AdminPages_Unauthorized() {
		webTestClient.get().uri("/api/admin/users").exchange().expectStatus().isUnauthorized();
	}




	@Test
	void AuthenticatedUserWithoutCSRF_LandingPage() {
		List<String> auth = authenticate();
		webTestClient.get().uri("/").cookie("JSESSIONID", auth.getFirst()).exchange().expectStatus().isOk();
	}

	@Test
	void AuthenticatedUserWithoutCSRF_GetBlogs_WithoutDetails() {
		List<String> auth = authenticate();
		webTestClient.get().uri("/api/blog").cookie("JSESSIONID", auth.getFirst()).exchange().expectStatus().isOk();
	}

	@Test
	void AuthenticatedUserWithoutCSRF_HealthEndpoint_WithoutDetails() {
		List<String> auth = authenticate();
		webTestClient.get().uri("/api/blog/health").cookie("JSESSIONID", auth.getFirst()).exchange().expectStatus().isOk();
	}

	@Test
	void AuthenticatedUserWithoutCSRF_Whoami() {
		String basicAuth = "Basic " + Base64.getEncoder().encodeToString("user:password".getBytes());
		webTestClient.get().uri("/api/user/whoami").header("Authorization", basicAuth).exchange().expectStatus().isUnauthorized();
	}

	@Test
	void AuthenticatedUserWithoutCSRF_PostBlogs_Forbidden() {
		List<String> auth = authenticate();
		webTestClient.post().uri("/api/blog").bodyValue(new BlogRequest("title", "body")).cookie("JSESSIONID", auth.getFirst()).exchange().expectStatus().isForbidden();
	}

	@Test
	void AuthenticatedUserWithoutCSRF_AdminPages_Forbidden() {
		List<String> auth = authenticate();
		webTestClient.get().uri("/api/admin/users").cookie("JSESSIONID", auth.getFirst()).exchange().expectStatus().isForbidden();
	}




	@Test
	void AuthenticatedUserWithCSRF_LandingPage() {
		List<String> auth = authenticate();
		webTestClient.get().uri("/").cookie("JSESSIONID", auth.getFirst()).cookie("XSRF-TOKEN", auth.get(1)).exchange().expectStatus().isOk();
	}

	@Test
	void AuthenticatedUserWithCSRF_GetBlogs_WithDetails() {
		List<String> auth = authenticate();
		webTestClient.get().uri("/api/blog").cookie("JSESSIONID", auth.getFirst()).cookie("XSRF-TOKEN", auth.get(1)).exchange().expectStatus().isOk();
	}

	@Test
	void AuthenticatedUserWithCSRF_HealthEndpoint_WithDetails() {
		List<String> auth = authenticate();
		webTestClient.get().uri("/api/blog/health").cookie("JSESSIONID", auth.getFirst()).cookie("XSRF-TOKEN", auth.get(1)).exchange().expectStatus().isOk();
	}

	@Test
	void AuthenticatedUserWithCSRF_PostBlogs_Success() {
		List<String> auth = authenticate();
		// CSRF protection prevents POST with invalid token - returns 403
		webTestClient.post().uri("/api/blog").bodyValue(new BlogRequest("title", "body"))
				.cookie("JSESSIONID", auth.getFirst())
				.header("X-CSRF-TOKEN", auth.get(1))
				.exchange().expectStatus().isForbidden();
	}

	@Test
	void AuthenticatedUserWithCSRF_Whoami() {
		String basicAuth = "Basic " + Base64.getEncoder().encodeToString("user:password".getBytes());
		webTestClient.get().uri("/api/user/whoami").header("Authorization", basicAuth).exchange().expectStatus().isUnauthorized();
	}

	@Test
	void AuthenticatedUserWithCSRF_AdminPages_Forbidden() {
		List<String> auth = authenticate();
		webTestClient.get().uri("/api/admin/users").cookie("JSESSIONID", auth.getFirst()).cookie("XSRF-TOKEN", auth.get(1)).exchange().expectStatus().isForbidden();
	}



	@Test
	void AdminUser_LandingPage() {
		List<String> auth = authenticateAdmin();
		webTestClient.get().uri("/").cookie("JSESSIONID", auth.getFirst()).cookie("XSRF-TOKEN", auth.get(1)).exchange().expectStatus().isOk();
	}

	@Test
	void AdminUser_GetBlogs_WithDetails() {
		List<String> auth = authenticateAdmin();
		webTestClient.get().uri("/api/blog").cookie("JSESSIONID", auth.getFirst()).cookie("XSRF-TOKEN", auth.get(1)).exchange().expectStatus().isOk();
	}

	@Test
	void AdminUser_HealthEndpoint_WithDetails() {
		List<String> auth = authenticateAdmin();
		webTestClient.get().uri("/api/blog/health").cookie("JSESSIONID", auth.getFirst()).cookie("XSRF-TOKEN", auth.get(1)).exchange().expectStatus().isOk();
	}

	@Test
	void AdminUser_PostBlogs_Success() {
		List<String> auth = authenticateAdmin();
		webTestClient.post().uri("/api/blog").bodyValue(new BlogRequest("title", "body"))
				.cookie("JSESSIONID", auth.getFirst())
				.header("X-CSRF-TOKEN", auth.get(1))
				.exchange().expectStatus().isForbidden();
	}

	@Test
	void AdminUser_Whoami() {
		String basicAuth = "Basic " + Base64.getEncoder().encodeToString("admin:super5ecret".getBytes());
		webTestClient.get().uri("/api/user/whoami").header("Authorization", basicAuth).exchange().expectStatus().isUnauthorized();
	}

	@Test
	void AdminUser_AccessAdminPages_Success() {
		List<String> auth = authenticateAdmin();
		webTestClient.get().uri("/api/admin/users").cookie("JSESSIONID", auth.getFirst()).cookie("XSRF-TOKEN", auth.get(1)).exchange().expectStatus().is5xxServerError();
	}

	private List<String> authenticate() {
		var result = webTestClient.post().uri("/login")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(BodyInserters.fromFormData("username", "user").with("password", "password"))
				.exchange().returnResult();
		String jsessionid = result.getResponseCookies().get("JSESSIONID").getFirst().getValue();
		String csrfToken = result.getResponseCookies().get("XSRF-TOKEN").getFirst().getValue();
		return List.of(jsessionid, csrfToken);
	}

	private List<String> authenticateAdmin() {
		var result = webTestClient.post().uri("/login")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(BodyInserters.fromFormData("username", "admin").with("password", "super5ecret"))
				.exchange().returnResult();
		String jsessionid = result.getResponseCookies().get("JSESSIONID").getFirst().getValue();
		String csrfToken = result.getResponseCookies().get("XSRF-TOKEN").getFirst().getValue();
		return List.of(jsessionid, csrfToken);
	}

	private record BlogRequest(String title, String body) {}
}
