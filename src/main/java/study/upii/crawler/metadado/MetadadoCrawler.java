package study.upii.crawler.metadado;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MetadadoCrawler extends WebCrawler {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final ColetorDeMetadado coletorDeMetadado;

    public MetadadoCrawler(ColetorDeMetadado coletorDeMetadado) {
        this.coletorDeMetadado = coletorDeMetadado;
    }

    @Override
    public void visit(Page page) {
        if (page.getParseData() instanceof HtmlParseData) {
            String html = ((HtmlParseData) page.getParseData()).getHtml();
            visitHtml(Jsoup.parse(html), page, coletorDeMetadado);
        } else {
            visitNonHtml(page);
        }
    }

    protected void visitNonHtml(Page page) {
        log.debug("[NOT HTML] {}" + page.getWebURL().getURL());
    }

    protected abstract void visitHtml(Document html, Page page, ColetorDeMetadado coletor);
}
