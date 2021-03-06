package hoggaster.rules;

import hoggaster.domain.CurrencyPair;
import hoggaster.rules.indicators.candles.CandleStickGranularity;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class UriTest {


    @Test
    @Ignore
    public void testCreateUri() throws UnsupportedEncodingException {
        String resource = "https://api-fxpractice.oanda.com/v1/candles";
        Instant start = null;
        Instant end = Instant.now();
        Integer periods = new Integer(10);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resource)
                .queryParam("currencyPair", CurrencyPair.EUR_SEK)
                .queryParam("granularity", CandleStickGranularity.END_OF_DAY.oandaStyle);

        if (start != null) {
            String encoded = URLEncoder.encode(start.truncatedTo(ChronoUnit.SECONDS).toString(), "utf-8");
            builder.queryParam("start", encoded);
        }
        if (end != null) {
            String encoded = URLEncoder.encode(end.truncatedTo(ChronoUnit.SECONDS).toString(), "utf-8");
            System.out.println(encoded);
            System.out.println(URLEncoder.encode(end.truncatedTo(ChronoUnit.SECONDS).toString(), "utf-8"));
            builder.queryParam("end", URLEncoder.encode(end.truncatedTo(ChronoUnit.SECONDS).toString(), "utf-8"));
        }
        if (periods != null) {
            builder.queryParam("count", periods);
        }
        String uri = builder.toUriString();
        System.out.println(uri);


    }
}
