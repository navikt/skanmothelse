package no.nav.skanmothelse.slack;

import com.slack.api.methods.SlackApiException;
import no.nav.skanmothelse.config.properties.SlackProperties;
import no.nav.skanmothelse.exceptions.technical.SlackServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ExceptionMessageBatchingServiceTest {

	private SlackProperties slackProperties;
	private SlackService slackService;
	private ExceptionMessageBatchingService service;

	@BeforeEach
	void setUp() {
		slackProperties = mock(SlackProperties.class);
		slackService = mock(SlackService.class);

		when(slackProperties.alertsEnabled()).thenReturn(true);

		service = new ExceptionMessageBatchingService(slackProperties, slackService);
	}

	@Test
	void destroySenderMeldingerNaarKoenIkkeErTom() throws SlackApiException, IOException {
		service.saveMeldingForBatchedSend("Noe gikk galt");

		service.destroy();

		verify(slackService, times(1)).sendMelding(anyList());
	}

	@Test
	void destroySenderIkkeMeldingerNaarKoenErTom() {
		service.destroy();

		verifyNoInteractions(slackService);
	}

	@Test
	void sendMeldingerToemmerKoen() throws SlackApiException, IOException {
		service.saveMeldingForBatchedSend("Feil 1");
		service.saveMeldingForBatchedSend("Feil 2");

		service.sendMeldinger();

		// Andre kall skal ikke sende fordi koen er tomt
		service.sendMeldinger();

		verify(slackService, times(1)).sendMelding(anyList());
	}

	@Test
	void sendMeldingerGjoerIngentingNaarKoenErTom() throws SlackApiException, IOException {
		service.sendMeldinger();

		verifyNoInteractions(slackService);
	}

	@Test
	void destroyLoggerOgSenderVedFeil() throws SlackApiException, IOException {
		service.saveMeldingForBatchedSend("Viktig feilmelding");

		doThrow(new IOException("Slack nede")).when(slackService).sendMelding(anyList());

		// Skal ikke kaste exception fordi destroy() handterer feil i sendMeldingImmediately()
		service.destroy();

		verify(slackService, times(1)).sendMelding(anyList());
	}

	@Test
	void meldingLagtTilUnderSendingBlirIkkeTapt() throws SlackApiException, IOException {
		service.saveMeldingForBatchedSend("Feil 1");

		// Simuler at en ny melding legges til mens Slack-kallet pagar
		doAnswer(_ -> {
			service.saveMeldingForBatchedSend("Feil lagt til under sending");
			return null;
		}).when(slackService).sendMelding(anyList());

		service.sendMeldinger();

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
		verify(slackService, times(1)).sendMelding(captor.capture());
		assertThat(captor.getValue()).hasSize(1);
		assertThat(captor.getValue().getFirst()).contains("Feil 1");

		Mockito.reset(slackService);
		service.sendMeldinger();

		verify(slackService, times(1)).sendMelding(captor.capture());
		assertThat(captor.getValue()).hasSize(1);
		assertThat(captor.getValue().getFirst()).contains("Feil lagt til under sending");
	}

	@Test
	void meldingerLeggesTilbakeIKoenVedFeil() throws SlackApiException, IOException {
		service.saveMeldingForBatchedSend("Feil som skal overleve");

		doThrow(new SlackServiceException("Slack sending feilet")).when(slackService).sendMelding(anyList());

		assertThatThrownBy(() -> service.sendMeldinger()).isInstanceOf(SlackServiceException.class);

		// Meldingene skal ha blitt lagt tilbake i koen
		Mockito.reset(slackService);
		service.sendMeldinger();

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
		verify(slackService, times(1)).sendMelding(captor.capture());
		assertThat(captor.getValue()).hasSize(1);
		assertThat(captor.getValue().getFirst()).contains("Feil som skal overleve");
	}
}
