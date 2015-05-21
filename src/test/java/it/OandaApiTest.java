package it;

import hoggaster.Application;
import hoggaster.domain.Broker;
import hoggaster.domain.Instrument;
import hoggaster.oanda.responses.OandaBidAskCandlesResponse;
import hoggaster.rules.indicators.CandleStickGranularity;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0") 
public class OandaApiTest {

	private static final Logger LOG = LoggerFactory.getLogger(OandaApiTest.class);
	
	@Value("${local.server.port}")   
    int port;
	
	@Autowired
	Broker oanda;
	
	@Test
	public void testGetMidPointCandles() throws InterruptedException, UnsupportedEncodingException {
		Instant end = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		OandaBidAskCandlesResponse midPointCandles = oanda.getBidAskCandles(Instrument.EUR_USD, CandleStickGranularity.DAY, new Integer(10), null, end);
	}
	
}
