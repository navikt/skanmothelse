package no.nav.skanmothelse.lagrefildetaljer.data;

import lombok.Builder;
import lombok.Value;

import jakarta.validation.constraints.NotNull;


@Value
@Builder
public class Bruker {
    @NotNull(message = "id kan ikke være null")
    private String id;
    @NotNull(message = "idType kan ikke være null")
    private String idType;
}
