package no.nav.skanmothelse.helse.domain;

import jakarta.xml.bind.annotation.XmlRegistry;


@XmlRegistry
public class ObjectFactory {
    public ObjectFactory() {
    }

    public Skanningmetadata createSkanningmetadata() {
        return new Skanningmetadata();
    }
}
