package study.upii.crawler.infra.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collection;

@QuarkusTest
public class GooglePlacesServiceTest {

    @ConfigProperty(name = "google.places.url.textsearch")
    String apiUrl;

    @Inject
    ObjectMapper objectMapper;

    GooglePlacesService googlePlacesService;

    @BeforeEach
    void createService() {
        googlePlacesService = new GooglePlacesService(apiUrl, objectMapper);
    }

    @Test
    public void hospitaisEmMaringa() throws IOException {
        print(googlePlacesService.search("Maringa+Parana", "hospital"));
    }

    @Test
    public void restaurantesEmMaringa() throws IOException {
        print(googlePlacesService.search("Maringa+Parana", "restaurant"));
    }

    private void print(Collection<GooglePlacesService.Place> places) {
        System.out.println("TOTAL: " + places.size() + " places");
        places.forEach(System.out::println);
    }

}