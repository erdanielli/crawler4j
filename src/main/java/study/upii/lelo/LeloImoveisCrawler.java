package study.upii.lelo;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import study.upii.Imovel;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LeloImoveisCrawler extends WebCrawler {
    private final Pattern skip = Pattern.compile(".*(\\.(css|js|gif|jpg|png|mp3|mp4|zip|gz))$");
    private final Pattern urlPattern = Pattern.compile("https://leloimoveis.com.br/imovel/(.*)");
    private final Consumer<Imovel> imovelConsumer;

    public LeloImoveisCrawler(Consumer<Imovel> imovelConsumer) {
        this.imovelConsumer = imovelConsumer;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !skip.matcher(href).matches() && urlPattern.matcher(href).matches();
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        Matcher m = urlPattern.matcher(url);
        if (m.find() && m.groupCount() == 1 && page.getParseData() instanceof HtmlParseData) {
            Imovel imovel = parseImovelOrNull(url, ((HtmlParseData) page.getParseData()).getHtml());
            if (imovel != null) {
                imovelConsumer.accept(imovel);
            }
        } else {
            System.out.println("IGNORED: " + url);
        }
    }

    private Imovel parseImovelOrNull(String url, String html) {
        try {
            return new ImovelLelo(url, html);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
