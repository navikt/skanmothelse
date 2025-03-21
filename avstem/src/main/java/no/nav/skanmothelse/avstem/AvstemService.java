package no.nav.skanmothelse.avstem;

import lombok.extern.slf4j.Slf4j;
import no.nav.skanmothelse.journalpostapi.JournalpostApiConsumer;
import no.nav.skanmothelse.journalpostapi.data.AvstemmingReferanser;
import no.nav.skanmothelse.journalpostapi.data.FeilendeAvstemmingReferanser;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import java.util.Set;

import static no.nav.skanmothelse.jira.OpprettJiraService.prettifySummary;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component
public class AvstemService {

	public static final String AVSTEMMINGSRAPPORT = "Skanmothelse avstemmingsrapport:";

	private final JournalpostApiConsumer journalpostConsumer;

	public AvstemService(JournalpostApiConsumer journalpostConsumer) {
		this.journalpostConsumer = journalpostConsumer;
	}

	@Handler
	public Set<String> avstemmingsReferanser(Set<String> avstemmingReferanser) {
		if (isEmpty(avstemmingReferanser)) {
			return Set.of();
		}

		FeilendeAvstemmingReferanser feilendeAvstemmingReferanser = journalpostConsumer.feilendeAvstemmingReferanser(new AvstemmingReferanser(avstemmingReferanser));
		if (feilendeAvstemmingReferanser == null || isEmpty(feilendeAvstemmingReferanser.referanserIkkeFunnet())) {
			log.info(prettifySummary("Skanmothelse avstemmingsrapport:", avstemmingReferanser.size(), 0));
			return null;
		}
		Set<String> referanserIkkeFunnet = feilendeAvstemmingReferanser.referanserIkkeFunnet();
		log.info(prettifySummary(AVSTEMMINGSRAPPORT, avstemmingReferanser.size(), referanserIkkeFunnet.size()));
		return feilendeAvstemmingReferanser.referanserIkkeFunnet();
	}
}
