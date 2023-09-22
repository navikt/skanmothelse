package no.nav.skanmothelse.lagrefildetaljer.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class STSResponse {
    private String access_token;
    private String token_type;
    private String expires_in;
    private String error;
}