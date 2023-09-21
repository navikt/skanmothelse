package no.nav.skanmothelse.helse.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Skanninginfo {

    @XmlElement(required = true, name = "fysiskPostboks")
    private String fysiskPostboks;

    @XmlElement(required = true, name = "strekkodePostboks")
    private String strekkodePostboks;
}
