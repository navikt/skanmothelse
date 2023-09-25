package no.nav.skanmothelse.helse.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
public class Bruker {

    @ToString.Exclude
    @XmlElement(required = true, name = "brukerid")
    private String brukerId;

    @XmlElement(required = true, name = "brukertype")
    private String brukerType;
}
