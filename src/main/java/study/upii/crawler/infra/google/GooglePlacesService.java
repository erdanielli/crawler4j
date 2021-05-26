package study.upii.crawler.infra.google;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GooglePlacesService {

    private final String apiKey;
    private final String apiUrl;
    private final ObjectMapper objectMapper;

    public GooglePlacesService(String apiKey,
                               String apiUrl,
                               ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
    }

    public Response search(String term, Point centralPoint, Integer radiusFromCentralPoint) throws IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            String fields = "&fields=photos,formatted_address,name,rating,opening_hours,geometry";
            String locationBias = "&locationbias=circle:" + radiusFromCentralPoint + "@" + centralPoint;
            String inputType = "&inputtype=textquery";
            String uri = apiUrl + "?input=" + URLEncoder.encode(term, StandardCharsets.UTF_8) + inputType + fields + locationBias + "&key=" + apiKey;
            HttpGet get = new HttpGet(uri);
            CloseableHttpResponse response = httpclient.execute(get);
            HttpEntity entity = response.getEntity();
            return objectMapper.readValue(
                    entity.getContent(),
                    Response.class
            );
        }

    }


    @Getter
    @Setter
    public static class Response {
        @JsonProperty("next_page_token")
        private String nextPageToken;
        private List<Place> results;
        @JsonProperty("debug_log")
        private List<String> debugLog;
        private String status;
        @JsonProperty("info_messages")
        private List<String> infoMessages;
    }

    @Getter
    @Setter
    public static class Place {
        @JsonProperty("place_id")
        private String id;
        @JsonProperty("business_status")
        private String status;
        @JsonProperty("formatted_address")
        private String address;
        private Geometry geometry;
        private String name;
        private List<Photo> photos;
        @JsonProperty("opening_hours")
        private OpeningHours openingHours;
        private Float rating;
        @JsonProperty("user_ratings_total")
        private Float userRatingsTotal;
        private List<String> types;
        @JsonProperty("plus_code")
        private PlusCode plusCode;
    }

    @Getter
    @Setter
    public static class Point {
        @JsonProperty("lat")
        private Float latitude;
        @JsonProperty("lng")
        private Float longitude;

        @Override
        public String toString() {
            return this.latitude + "," + this.longitude;
        }
    }

    @Getter
    @Setter
    public static class Geometry {
        private Point location;
        private Viewport viewport;
    }

    @Getter
    @Setter
    public static class Viewport {
        private Point northeast;
        private Point southwest;
    }

    @Getter
    @Setter
    public static class Photo {
        private Integer width;
        private Integer height;
        @JsonProperty("html_attributions")
        private List<String> htmlAttributions;
        @JsonProperty("photo_reference")
        private String reference;
    }

    @Getter
    @Setter
    public static class OpeningHours {
        @JsonProperty("open_now")
        private Boolean openNow;
        @JsonProperty("weekday_text")
        private List<String> weekdayText;
    }

    @Getter
    @Setter
    public static class PlusCode {
        @JsonProperty("compound_code")
        private String compoundCode;
        @JsonProperty("global_code")
        private String globalCode;
    }

}
