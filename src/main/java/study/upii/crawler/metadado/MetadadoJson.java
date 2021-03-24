package study.upii.crawler.metadado;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class MetadadoJson implements Consumer<ColetorDeMetadado.Printer> {
    private final ObjectMapper objectMapper;
    private final Path jsonFile;

    public MetadadoJson(ObjectMapper objectMapper, Path jsonFile) {
        this.objectMapper = objectMapper;
        this.jsonFile = jsonFile;
    }

    @Override
    public void accept(ColetorDeMetadado.Printer p) {
        try (BufferedWriter w = Files.newBufferedWriter(jsonFile, UTF_8, CREATE, TRUNCATE_EXISTING)) {
            ObjectNode node = buildJson(p);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(w, node);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ObjectNode buildJson(ColetorDeMetadado.Printer p) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("anuncios", p.anuncios());
        ArrayNode tipos = root.putArray("tipos");
        for (String tipo : p.tipos()) {
            tipos.add(tipo);
        }
        p.metadado((nome, valores) -> {
            ArrayNode k = root.putArray(nome);
            for (String v : valores) {
                k.add(v);
            }
        });
        return root;
    }
}
