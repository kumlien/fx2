package it;

import hoggaster.Application;
import hoggaster.candles.CandleService;
import hoggaster.domain.BrokerConnection;
import hoggaster.domain.Instrument;
import hoggaster.oanda.responses.OandaBidAskCandlesResponse;
import hoggaster.rules.indicators.CandleStickGranularity;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import reactor.core.processor.RingBufferWorkProcessor;
import reactor.fn.Consumer;
import reactor.rx.Stream;
import reactor.rx.Streams;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class OandaApiTest {

    private static final Logger LOG = LoggerFactory.getLogger(OandaApiTest.class);

    @Value("${local.server.port}")
    int port;

    @Autowired
    @Qualifier("OandaBrokerConnection")
    BrokerConnection oanda;

    @Autowired
    CandleService candleService;

    @Test
    @Ignore
    public void testGetBidAskCandles() throws InterruptedException, UnsupportedEncodingException {
        Instant end = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        OandaBidAskCandlesResponse midPointCandles = oanda.getBidAskCandles(Instrument.EUR_USD, CandleStickGranularity.END_OF_DAY, new Integer(10), null, end, false);
        LOG.info("Got a few candles: {}", midPointCandles);
    }

    @Test
    @Ignore
    public void testHistoricCandles() throws InterruptedException, UnsupportedEncodingException {
        Arrays.asList(Instrument.values()).forEach(instrument -> {
            Instant start = Instant.now().truncatedTo(ChronoUnit.SECONDS).minus(Duration.ofDays(365 * 20));
            int midPointCandles = candleService.fetchAndSaveHistoricCandles(instrument, CandleStickGranularity.MINUTE, start, null);
            LOG.info("Got a few candles: {}", midPointCandles);
        });
    }

    @Test
    @Ignore
    public void testGetBidAskCandlesWithReactor() throws InterruptedException {
        Instant now = Instant.now();
        RingBufferWorkProcessor<Instrument> publisher = RingBufferWorkProcessor.create("instrument work processor", 32);
        Stream<Instrument> instrumentStream = Streams.wrap(publisher);

        Consumer<Instrument> ic = instrument -> {
            Arrays.asList(CandleStickGranularity.values()).forEach(granularity -> {
                try {
                    OandaBidAskCandlesResponse bidAskCandles = oanda.getBidAskCandles(instrument, granularity, 200, null, now, false);
                    LOG.info("Candle saved: {}", bidAskCandles);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        };

        instrumentStream.consume(ic);
        instrumentStream.consume(ic);
        instrumentStream.consume(ic);
        instrumentStream.consume(ic);
        instrumentStream.consume(ic);

        Arrays.asList(Instrument.values()).forEach(i -> publisher.onNext(i));
        Thread.sleep(60000);

        // Instant now = Instant.now();
        // Arrays.asList(Instrument.values()).stream().forEach(instrument -> {
        // Arrays.asList(CandleStickGranularity.values()).forEach(granularity -> {
        // try {
        // OandaBidAskCandlesResponse bidAskCandles = oanda.getBidAskCandles(instrument, granularity, 200, null, now);
        // bidAskCandles.getCandles().forEach(bac -> {
        // BidAskCandle candle = new BidAskCandle(instrument, BrokerID.OANDA, granularity, Instant.parse(bac.getTime()), bac.getOpenBid(), bac.getOpenAsk(), bac.getHighBid(), bac.getHighAsk(), bac.getLowBid(), bac.getLowAsk(), bac.getCloseBid(), bac.getCloseAsk(), bac.getVolume(), bac.getComplete());
        // //candle = bidAskCandleRepo.save(candle);
        // LOG.info("Candle saved: {}", candle);
        // });
        // } catch (Exception e) {
        // LOG.error("Error fetching candles",e);
        // }
        // });
        // });
    }

}
