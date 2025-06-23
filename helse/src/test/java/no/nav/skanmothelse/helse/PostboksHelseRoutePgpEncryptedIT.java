package no.nav.skanmothelse.helse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class PostboksHelseRoutePgpEncryptedIT extends AbstractIt {

	private static final String INNGAAENDE = "inngaaende";
	private static final String FEILMAPPE = "feilmappe";

	@Autowired
	private Path sshdPath;

	@BeforeEach
	void beforeEach() {
		super.setUpStubs();
		super.stubSlack();

		final Path inngaaende = sshdPath.resolve(INNGAAENDE);
		final Path processed = inngaaende.resolve("processed");
		final Path feilmappe = sshdPath.resolve(FEILMAPPE);
		try {
			preparePath(inngaaende);
			preparePath(processed);
			preparePath(feilmappe);
		} catch (Exception e) {
			// noop
		}
	}

	private void preparePath(Path path) throws IOException {
		if (!Files.exists(path)) {
			Files.createDirectory(path);
		} else {
			FileUtils.cleanDirectory(path.toFile());
		}
	}

	@Test
	public void shouldBehandlePostboksHelsePgpEncryptedZip() throws IOException {
		// BHELSE-20200529-4.zip.pgp
		// OK   - BHELSE-20200529-4-1x xml, pdf
		// OK   - BHELSE-20200529-4-2x xml, pdf, ocr
		// FEIL - BHELSE-20200529-4-3x xml, pdf, ocr (valideringsfeil xml)
		// FEIL - BHELSE-20200529-4-4x xml, ocr (mangler pdf)
		// FEIL - BHELSE-20200529-4-5x pdf, ocr (mangler xml)

		final String BATCHNAME = "BHELSE-20200529-4";
		copyFileFromClasspathToInngaaende(BATCHNAME + ".zip.pgp");

		await().atMost(15, SECONDS).untilAsserted(() -> {
			try {
				assertThat(Files.list(sshdPath.resolve(FEILMAPPE).resolve(BATCHNAME))
						.toList())
						.hasSize(3);

				verify(exactly(2), postRequestedFor(urlMatching(URL_DOKARKIV_JOURNALPOST_GEN)));
				verify(exactly(1), postRequestedFor(urlPathEqualTo(SLACK_POST_MESSAGE_PATH))
						.withRequestBody(containing("no.nav.skanmothelse.exceptions.functional.SkanningmetadataValidationException")));
				verify(exactly(2), postRequestedFor(urlPathEqualTo(SLACK_POST_MESSAGE_PATH))
						.withRequestBody(containing("no.nav.skanmothelse.exceptions.functional.ForsendelseNotCompleteException")));
			} catch (NoSuchFileException e) {
				fail();
			}
		});

		final List<String> feilmappeContents = Files.list(sshdPath.resolve(FEILMAPPE).resolve(BATCHNAME))
				.map(p -> FilenameUtils.getName(p.toAbsolutePath().toString()))
				.toList();
		assertThat(feilmappeContents).containsExactlyInAnyOrder(
				"BHELSE-20200529-4-3x.zip",
				"BHELSE-20200529-4-4x.zip",
				"BHELSE-20200529-4-5x.zip");
	}

	@Test
	public void shouldBehandlePostboksHelsePgpEncryptedZipWithMultipleDotsInFilenames() throws IOException {
		// BHELSE.20200529-4.zip.pgp
		// OK   - BHELSE.20200529-4-1x xml, pdf
		// OK   - BHELSE.20200529-4-2x xml, pdf, ocr
		// FEIL - BHELSE.20200529-4-3x xml, pdf, ocr (valideringsfeil xml)
		// FEIL - BHELSE.20200529-4-4x xml, ocr (mangler pdf)
		// FEIL - BHELSE.20200529-4-5x pdf, ocr (mangler xml)

		final String BATCHNAME = "BHELSE.20200529-4";
		copyFileFromClasspathToInngaaende(BATCHNAME + ".zip.pgp");

		await().atMost(15, SECONDS).untilAsserted(() -> {
			try {
				assertThat(Files.list(sshdPath.resolve(FEILMAPPE).resolve(BATCHNAME))
						.toList())
						.hasSize(3);

				verify(exactly(2), postRequestedFor(urlMatching(URL_DOKARKIV_JOURNALPOST_GEN)));
				verify(exactly(1), postRequestedFor(urlPathEqualTo(SLACK_POST_MESSAGE_PATH))
						.withRequestBody(containing("no.nav.skanmothelse.exceptions.functional.SkanningmetadataValidationException")));
				verify(exactly(2), postRequestedFor(urlPathEqualTo(SLACK_POST_MESSAGE_PATH))
						.withRequestBody(containing("no.nav.skanmothelse.exceptions.functional.ForsendelseNotCompleteException")));
			} catch (NoSuchFileException e) {
				fail();
			}
		});

		final List<String> feilmappeContents = Files.list(sshdPath.resolve(FEILMAPPE).resolve(BATCHNAME))
				.map(p -> FilenameUtils.getName(p.toAbsolutePath().toString()))
				.toList();
		assertThat(feilmappeContents).containsExactlyInAnyOrder(
				"BHELSE.20200529-4-3x.zip",
				"BHELSE.20200529-4-4x.zip",
				"BHELSE.20200529-4-5x.zip");
	}

	@Test
	public void shouldFailWhenPrivateKeyDoesNotMatchPublicKey() throws IOException {
		// BHELSE-XML-ORDERED-FIRST-1.zip.pgp er kryptert med publicKeyElGamal (i stedet for publicKeyRSA)
		// Korresponderende RSA-private key vil da feile i forsøket på dekryptering

		final String ZIP_FILE_NAME_NO_EXTENSION = "BHELSE-XML-ORDERED-FIRST-1";
		copyFileFromClasspathToInngaaende(ZIP_FILE_NAME_NO_EXTENSION + ".zip.pgp");

		assertTrue(Files.exists(sshdPath.resolve(INNGAAENDE).resolve(ZIP_FILE_NAME_NO_EXTENSION + ".zip.pgp")));

		await().atMost(15, SECONDS).untilAsserted(() -> {
			assertTrue(Files.exists(sshdPath.resolve(FEILMAPPE).resolve(ZIP_FILE_NAME_NO_EXTENSION + ".zip.pgp")));
			verify(exactly(1), postRequestedFor(urlPathEqualTo(SLACK_POST_MESSAGE_PATH))
					.withRequestBody(containing("org.bouncycastle.openpgp.PGPException")));
		});
	}

	private void copyFileFromClasspathToInngaaende(final String zipfilename) throws IOException {
		Files.copy(new ClassPathResource(zipfilename).getInputStream(), sshdPath.resolve(INNGAAENDE).resolve(zipfilename));
	}
}