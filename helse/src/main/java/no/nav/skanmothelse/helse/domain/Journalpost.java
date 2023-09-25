package no.nav.skanmothelse.helse.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.skanmothelse.utils.LocalDateAdapter;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Journalpost {

    @XmlElement(name = "bruker")
    private Bruker bruker;

    @XmlElement(required = true, name = "mottakskanal")
    private String mottakskanal;

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlElement(required = true, name = "datoMottatt")
    private LocalDate datoMottatt;

    @XmlElement(required = true, name = "batchnavn")
    private String batchnavn;

    @XmlElement(name = "filnavn")
    private String filNavn;

    @XmlElement(name = "endorsernr")
    private String endorsernr;

    @XmlElement(name = "antallSider")
    private String antallSider;
}
