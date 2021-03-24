package study.upii.crawler.metadado;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public final class ColetorDeMetadadoSimples implements ColetorDeMetadado {
    private final Set<String> tipos = new HashSet<>();
    private final Map<String, Set<String>> metadados = new HashMap<>();

    @Override
    public ColetorDeMetadado tipo(String tipo) {
        tipos.add(tipo);
        return this;
    }

    @Override
    public ColetorDeMetadado metadado(String nome, String valor) {
        metadados.computeIfAbsent(nome, n -> new HashSet<>()).add(valor);
        return this;
    }

    @Override
    public ColetorDeMetadado concat(ColetorDeMetadado that) {
        ColetorDeMetadadoSimples merge = new ColetorDeMetadadoSimples();
        merge.tipos.addAll(this.tipos);
        merge.metadados.putAll(this.metadados);

        Printer p = that.printer();
        for (String t : p.tipos()) {
            merge.tipo(t);
        }
        p.metadado((k, values) -> {
            for (String v : values) {
                merge.metadado(k, v);
            }
        });
        return merge;
    }

    @Override
    public Printer printer() {
        return new Printer() {
            @Override
            public Iterable<String> tipos() {
                return tipos;
            }

            @Override
            public void metadado(BiConsumer<String, Iterable<String>> metadado) {
                metadados.forEach(metadado);
            }

            @Override
            public int anuncios() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
