package no.nav.skanmothelse.helse;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class PostboksHelseTema {
    private static final Map<String, PostboksHelse> postbokser = new HashMap<>();

    static {
        postbokser.put(PostboksHelse.PB_1411.postboks, PostboksHelse.PB_1411);
    }

    public static PostboksHelse lookup(final String postboks) {
        return postbokser.getOrDefault(postboks, null);
    }

    @Getter
    @AllArgsConstructor
    public enum PostboksHelse
    {
        PB_1411("1411", "NAV 08-07.04", "SYM", "Papirsykmelding", "Papirsykmelding", null);

        final String postboks;
        final String brevkode;
        final String tema;
        final String tittel;
        final String dokumentTittel;
        final String behandlingstema;
    }
}
