package study.upii.crawler.metadado;

import java.util.*;
import java.util.function.BiConsumer;

public interface ColetorDeMetadado {

    ColetorDeMetadado tipo(String tipo);

    ColetorDeMetadado metadado(String nome, String valor);

    Printer printer();

    ColetorDeMetadado concat(ColetorDeMetadado coletor);

    interface Printer {

        Iterable<String> tipos();

        void metadado(BiConsumer<String, Iterable<String>> metadado);

        int anuncios();

        abstract class Wrapper implements Printer {
            private final Printer impl;

            protected Wrapper(Printer impl) {
                this.impl = Objects.requireNonNull(impl);
            }

            @Override
            public Iterable<String> tipos() {
                return impl.tipos();
            }

            @Override
            public void metadado(BiConsumer<String, Iterable<String>> metadado) {
                impl.metadado(metadado);
            }

            @Override
            public int anuncios() {
                return impl.anuncios();
            }
        }
    }

    abstract class Wrapper implements ColetorDeMetadado {
        private final ColetorDeMetadado impl;

        protected Wrapper(ColetorDeMetadado impl) {
            this.impl = Objects.requireNonNull(impl);
        }

        @Override
        public ColetorDeMetadado tipo(String tipo) {
            return impl.tipo(tipo);
        }

        @Override
        public ColetorDeMetadado metadado(String nome, String valor) {
            return impl.metadado(nome, valor);
        }

        @Override
        public Printer printer() {
            return impl.printer();
        }

        @Override
        public ColetorDeMetadado concat(ColetorDeMetadado coletor) {
            return impl.concat(coletor);
        }
    }

}
