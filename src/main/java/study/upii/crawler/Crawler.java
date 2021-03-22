package study.upii.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import java.io.File;
import java.util.function.Supplier;

public class Crawler implements Runnable {
    private final Supplier<WebCrawler> fn;
    private final String[] seeds;

    public Crawler(Supplier<WebCrawler> fn, String... seeds) {
        this.fn = fn;
        this.seeds = seeds;
    }

    @Override
    public void run() {
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(new File(".data").getAbsolutePath());
        config.setIncludeHttpsPages(true);

        // Instantiate the controller for this crawl.
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

        try {
            CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
            for (String s : seeds) {
                controller.addSeed(s);
            }
            controller.startNonBlocking(fn::get, 2);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
