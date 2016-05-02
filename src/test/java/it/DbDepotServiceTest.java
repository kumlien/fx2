package it;

import hoggaster.MongoConfig;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.brokers.BrokerDepot;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.depots.DepotService;
import hoggaster.domain.trades.Trade;
import hoggaster.domain.users.User;
import hoggaster.oanda.OandaProperties;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.bus.EventBus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import static hoggaster.domain.brokers.Broker.OANDA;
import static hoggaster.domain.orders.OrderSide.buy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableConfigurationProperties(value = {OandaProperties.class})
@SpringApplicationConfiguration(classes = {DbDepotServiceTest.class, MongoConfig.class})
@Configuration
@ComponentScan(basePackageClasses = {DepotService.class})
public class DbDepotServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(DbDepotServiceTest.class);

    @Autowired
    DepotService depotService;

    @Autowired
    private BrokerConnection brokerConnection;

    @Autowired
    OandaProperties properties;


    @Bean(name = "OandaBrokerConnection")
    public BrokerConnection brokerConnection() {
       return mock(BrokerConnection.class);
    }

    @Bean
    public EventBus depotEventBus() {
        return EventBus.create();
    }


    /**
     */
    @Test
    @Ignore
    public void testCreatePellesDepot() throws Exception {
        User user = mock(User.class);
        Mockito.when(user.getId()).thenReturn("aUserId");
        String brokerId = "9678914";
        BrokerDepot brokerDepot = new BrokerDepot(brokerId, "fake positions", Currency.getInstance("USD"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0);
        when(brokerConnection.getDepot(eq(brokerId))).thenReturn(brokerDepot);
        try {
            DbDepot dbDepot = depotService.createDepot(user, "Pelles dbDepot", OANDA, brokerId, DbDepot.Type.DEMO);
            LOG.info("DbDepot created: {}", dbDepot);
        } catch (Exception e) {
            LOG.info("error...", e);
            throw e;
        }
    }


    @Test
    public void testCreateDeleteDepot() throws Exception {
        User user = mock(User.class);
        Mockito.when(user.getId()).thenReturn("aUserId");
        String externalDepotId = "123123123";
        BrokerDepot brokerDepot = new BrokerDepot(externalDepotId, "fake external depot", Currency.getInstance("USD"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0);
        when(brokerConnection.getDepot(eq(externalDepotId))).thenReturn(brokerDepot);
        try {
            DbDepot dbDepot = depotService.createDepot(user, "DummyDepot", OANDA, externalDepotId, DbDepot.Type.DEMO);
            LOG.info("DbDepot created: {}", dbDepot);
            depotService.deleteDepot(dbDepot);
            assertNull(depotService.findDepotById(dbDepot.getId()));
        } catch (Exception e) {
            LOG.info("error...", e);
            throw e;
        }

    }

    @Test
    public void testAddPositions() throws Exception {
        User user = mock(User.class);
        Mockito.when(user.getId()).thenReturn("aUserId");
        String externalDepotId = "123123123";
        DbDepot dbDepot = null;
        CurrencyPair cp = CurrencyPair.AUD_JPY;
        BrokerDepot brokerDepot = new BrokerDepot(externalDepotId, "fake positions", Currency.getInstance("USD"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0);
        when(brokerConnection.getDepot(eq(externalDepotId))).thenReturn(brokerDepot);
        try {
            dbDepot = depotService.createDepot(user, "DummyDepot", OANDA, externalDepotId, DbDepot.Type.DEMO);
            LOG.info("DbDepot created: {}", dbDepot);
            Trade trade = new Trade(dbDepot.getId(),"robotId", OANDA, 1L, new BigDecimal(17),  buy, cp, Instant.now(), BigDecimal.TEN, null, null, null);
            dbDepot.tradeOpened(trade);
            depotService.save(dbDepot);
            dbDepot = depotService.findDepotById(dbDepot.getId());
            assertEquals(1, dbDepot.getPositions().size());
            trade = new Trade(dbDepot.getId(),"robotId", OANDA, 2L, new BigDecimal(3),  buy, cp, Instant.now(), BigDecimal.TEN, null, null, null);
            dbDepot.tradeOpened(trade);
            depotService.save(dbDepot);
            dbDepot = depotService.findDepotById(dbDepot.getId());
            assertEquals(1, dbDepot.getPositions().size());
            assertEquals(new BigDecimal("20"), dbDepot.getPositionByInstrument(cp).getQuantity());
            assertEquals(new BigDecimal("2"), dbDepot.getPositionByInstrument(cp).getAveragePricePerShare());
            depotService.deleteDepot(dbDepot);
            assertNull(depotService.findDepotById(dbDepot.getId()));
        } catch (Exception e) {
            LOG.info("error...", e);
            throw e;
        } finally {
            if (dbDepot != null) {
                depotService.deleteDepot(dbDepot);
            }
        }
    }
}
