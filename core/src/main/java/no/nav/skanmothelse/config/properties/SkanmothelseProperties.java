package no.nav.skanmothelse.config.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Data
@Validated
@ConfigurationProperties("skanmothelse")
public class SkanmothelseProperties {

	private final ServiceUserProperties serviceuser = new ServiceUserProperties();
	private final FilomraadeProperties filomraade = new FilomraadeProperties();
	private final Helse helse = new Helse();
	private final SftpProperties sftp = new SftpProperties();
	private final Endpoints endpoints = new Endpoints();

	@Data
	@Validated
	public static class ServiceUserProperties {
		@ToString.Exclude
		@NotEmpty
		private String username;

		@ToString.Exclude
		@NotEmpty
		private String password;
	}

	@Data
	@Validated
	public static class FilomraadeProperties {
		@NotEmpty
		private String inngaaendemappe;

		@NotEmpty
		private String feilmappe;
	}

	@Data
	@Validated
	public static class Helse {
		@NotEmpty
		private String endpointuri;

		@NotEmpty
		private String endpointconfig;

		@NotEmpty
		private String schedule;

		@NotNull
		private Duration completiontimeout;

		@NotNull
		private final FilomraadeProperties filomraade = new FilomraadeProperties();
	}

	@Data
	@Validated
	public static class SftpProperties {
		@ToString.Exclude
		@NotEmpty
		private String host;

		@ToString.Exclude
		@NotEmpty
		private String privateKey;

		@ToString.Exclude
		@NotEmpty
		private String hostKey;

		@ToString.Exclude
		@NotEmpty
		private String username;

		@ToString.Exclude
		@NotEmpty
		private String port;
	}

	@Data
	@Validated
	public static class Endpoints {
		@NotNull
		private AzureEndpoint dokarkiv;
	}

	@Data
	@Validated
	public static class AzureEndpoint {
		/**
		 * Url til tjeneste som har azure autorisasjon
		 */
		@NotEmpty
		private String url;
		/**
		 * Scope til azure client credential flow
		 */
		@NotEmpty
		private String scope;
	}

}


