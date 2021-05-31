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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class GooglePlacesService {

    private final String apiUrl;
    private final ObjectMapper objectMapper;

    public GooglePlacesService(String apiUrl, ObjectMapper objectMapper) {
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
    }

    public Collection<Place> search(String term, String type) throws IOException {
        Set<Place> results = new LinkedHashSet<>();
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            String uri = apiUrl + "&query=" + term + "&type=" + type + "&region=br&fields=name,geometry,types";
            Response resp = getNextPage(httpclient, uri, results::addAll);
            int max = 5;
            while (resp.nextPageToken != null && max-- > 0) {
                resp = getNextPage(httpclient, apiUrl + "&pagetoken=" + resp.nextPageToken, results::addAll);
            }
        }
        return results;
    }

    private Response getNextPage(CloseableHttpClient client, String uri, Consumer<List<Place>> placesCn) throws IOException {
        System.out.println("Fetching " + uri);
        int maxRetries = 5;
        while (maxRetries-- > 0) {
            try (CloseableHttpResponse response = client.execute(new HttpGet(uri))) {
                HttpEntity entity = response.getEntity();
                Response resp = objectMapper.readValue(entity.getContent(), Response.class);
                if (resp.getStatus().equals("OK")) {
                    placesCn.accept(resp.getResults());
                    System.out.println(resp.getResults().size() + " places loaded");
                    return resp;
                } else {
                    System.out.println("STATUS: " + resp.getStatus());
                    System.out.println("Retrying in 1s....");
                    Thread.sleep(1000L);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException("TIMEOUT REACHED");
    }

    @Getter
    @Setter
    public static class Response {
        @JsonProperty("next_page_token")
        private String nextPageToken;
        private List<Place> results;
        //        @JsonProperty("debug_log")
//        private List<String> debugLog;
        private String status;
//        @JsonProperty("info_messages")
//        private List<String> infoMessages;
    }

    @Getter
    @Setter
    public static class Place {
        @JsonProperty("place_id")
        private String id;
        @JsonProperty("business_status")
        private String status;
//        @JsonProperty("formatted_address")
//        private String address;
        private Geometry geometry;
        private String name;
//        private List<Photo> photos;
//        @JsonProperty("opening_hours")
//        private OpeningHours openingHours;
//        private Float rating;
//        @JsonProperty("user_ratings_total")
//        private Float userRatingsTotal;
        private List<String> types;
//        @JsonProperty("plus_code")
//        private PlusCode plusCode;

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Place place = (Place) o;
            return id.equals(place.id);
        }

        @Override
        public String toString() {
            return "{name=" + name
                    + ",location=" + geometry.getLocation()
                    + ",types=[" + String.join(",", types) + "]"
                    + ",id=" + id + '}';
        }
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
//        private Viewport viewport;
    }

/*
    @Getter
    @Setter
    public static class Viewport {
        private Point northeast;
        private Point southwest;
    }
*/

/*
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
*/

/*
    @Getter
    @Setter
    public static class OpeningHours {
        @JsonProperty("open_now")
        private Boolean openNow;
        @JsonProperty("weekday_text")
        private List<String> weekdayText;
    }
*/

/*
    @Getter
    @Setter
    public static class PlusCode {
        @JsonProperty("compound_code")
        private String compoundCode;
        @JsonProperty("global_code")
        private String globalCode;
    }
*/

}
