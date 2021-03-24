package study.upii.crawler.metadado;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import study.upii.crawler.metadado.imobiliaria.Lelo;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@QuarkusMain
public class MetadadoApp implements QuarkusApplication {

    @Inject
    ObjectMapper objectMapper;

    public static void main(String[] args) {
        Quarkus.run(MetadadoApp.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        ScheduledExecutorService progressLogExecutor = Executors.newSingleThreadScheduledExecutor();
        try {
            ColetorDeMetadado c = new Lelo(progressLogExecutor).get();
            progressLogExecutor.shutdown();
            new MetadadoJson(objectMapper, Path.of("target", "metadados.json"))
                    .accept(c.printer());
            return 0;
        } finally {
            progressLogExecutor.shutdown();
        }
    }
}
