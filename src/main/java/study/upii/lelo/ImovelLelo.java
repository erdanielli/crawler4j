package study.upii.lelo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import study.upii.Imovel;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

final class ImovelLelo implements Imovel {
    private final String url;
    private final List<String> imagens;
    private final String anuncio;
    private final String mapsUrl;

    ImovelLelo(String url, String html) {
        this.url = url;
        Document doc = Jsoup.parse(html);
        imagens = parseImagens(doc);
        anuncio = parseAnuncio(doc);
        mapsUrl = parseMapsUrl(doc);
    }

    private List<String> parseImagens(Document page) {
        Elements list = page.getElementsByClass("card__photo-img");
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(e -> "img".equals(e.tagName()))
                .map(e -> e.attr("data-src"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String parseAnuncio(Document page) {
        Elements elements = page.getElementsByClass("card__highlights");
        if (!elements.isEmpty()) {
            return elements.get(0).text();
        }
        elements = page.getElementsByClass("card__header");
        if (!elements.isEmpty()) {
            return elements.get(0).text();
        }
        String title = page.title();
        return title.substring(0, title.indexOf(" - "));
    }

    private String parseMapsUrl(Document doc) {
        Elements list = doc.getElementsByClass("card__map-iframe");
        if (list.isEmpty()) {
            return null;
        }
        Element iframe = list.get(0);
        String src = iframe.attr("src");
        src = src.isBlank() ? iframe.attr("data-src") : src;
        if (!src.isBlank()) {
            String q = src.substring(src.indexOf("?q=") + 3);
            q = q.substring(0, q.indexOf('&'));
            return "https://www.google.com/maps/search/" + URLEncoder.encode(q, StandardCharsets.UTF_8);
        }
        return "";
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public List<String> imagens() {
        return imagens;
    }

    @Override
    public String anuncio() {
        return anuncio;
    }

    @Override
    public String mapsUrl() {
        return mapsUrl;
    }
}
