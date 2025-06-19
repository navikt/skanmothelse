package no.nav.skanmothelse.helse;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(
		classes = HelseTestConfig.class,
		webEnvironment = RANDOM_PORT
)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
public class AbstractIt {

	static final String URL_DOKARKIV_JOURNALPOST_GEN = "/rest/journalpostapi/v1/journalpost\\?foersoekFerdigstill=false";
	static final String SLACK_POST_MESSAGE_PATH = "/slack/api/chat.postMessage";
	private static final String SLACK_AUTH_PATH = "/slack/api/auth.test";

	void setUpStubs() {
		stubAzureToken();

		stubFor(post(urlMatching(URL_DOKARKIV_JOURNALPOST_GEN)).willReturn(aResponse()
				.withStatus(OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withHeader("Connection", "close")
				.withBodyFile("journalpostapi/success.json"))
		);
	}

	public void stubAzureToken() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	public void stubSlack() {
		stubFor(post(urlPathEqualTo(SLACK_AUTH_PATH))
				.willReturn(aResponse()
						.withBodyFile("slack/auth_response.json")
						.withStatus(OK.value())));

		stubFor(post(urlPathEqualTo(SLACK_POST_MESSAGE_PATH))
				.willReturn(aResponse()
						.withBodyFile("slack/message_response.json")
						.withStatus(OK.value())));
	}

}