package no.nav.skanmothelse.helse.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "skanningmetadata")
public class Skanningmetadata {

    @XmlElement(required = true, name = "journalpost")
    private Journalpost journalpost;

    @XmlElement(required = true, name = "skanninginfo")
    private Skanninginfo skanninginfo;
}
