package study.upii.crawler.infra.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;

import javax.inject.Inject;
import java.io.IOException;

@QuarkusTest
public class GooglePlacesServiceTest {

    @ConfigProperty(name = "google.places.url.textsearch")
    String textSearchApiUrl;
    @ConfigProperty(name = "google.places.api.key")
    String apiKey;
    @Inject
    ObjectMapper objectMapper;


    @Test
    public void shouldSearchAtGooglePlacesApi() throws IOException {
        //given

        GooglePlacesService.Point point = new GooglePlacesService.Point();
        point.setLatitude(-15.77972F);
        point.setLongitude(-47.92972F);
        Integer radius = 10000;
        String term = "Pizzaria";
        GooglePlacesService googlePlacesService = new GooglePlacesService(apiKey, textSearchApiUrl, objectMapper);

        //when

        GooglePlacesService.Response response = googlePlacesService.search(term, point, radius);

        //then

        Assert.assertFalse(response.getResults().isEmpty());
    }

}