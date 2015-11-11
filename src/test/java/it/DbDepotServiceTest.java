package it;

import hoggaster.Application;
import hoggaster.JSR310Config;
import hoggaster.MongoConfig;
import hoggaster.depot.DbDepot;
import hoggaster.depot.Depot;
import hoggaster.depot.DepotRepo;
import hoggaster.depot.DepotService;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.brokers.BrokerDepot;
import hoggaster.oanda.OandaApi;
import hoggaster.user.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {DbDepotServiceTest.class, MongoConfig.class})
@Configuration
@ComponentScan(basePackageClasses = {DepotService.class})
@EnableMongoRepositories(basePackageClasses = DepotRepo.class)
public class DbDepotServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(DbDepotServiceTest.class);

    @Autowired
    DepotService depotService;

    @Autowired
    private BrokerConnection brokerConnection;


    @Bean(name = "OandaBrokerConnection")
    public BrokerConnection brokerConnection() {
        return mock(BrokerConnection.class);
    }


    /**
     */
    @Test
    @Ignore
    public void testCreatePellesDepot() throws Exception {
        User user = mock(User.class);
        Mockito.when(user.getId()).thenReturn("aUserId");
        try {
            DbDepot dbDepot = depotService.createDepot(user, "Pelles dbDepot", Broker.OANDA, "9678914", DbDepot.Type.DEMO);
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
        BrokerDepot brokerDepot = new BrokerDepot(externalDepotId,"fake depot", Currency.getInstance("USD"), BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO, BigDecimal.ZERO,0,0);
        when(brokerConnection.getDepot(eq(externalDepotId))).thenReturn(brokerDepot);
        try {
            DbDepot dbDepot = depotService.createDepot(user, "DummyDepot", Broker.OANDA, externalDepotId, DbDepot.Type.DEMO);
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
        CurrencyPair cp = CurrencyPair.AUD_JPY;
        BrokerDepot brokerDepot = new BrokerDepot(externalDepotId,"fake depot", Currency.getInstance("USD"), BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO, BigDecimal.ZERO,0,0);
        when(brokerConnection.getDepot(eq(externalDepotId))).thenReturn(brokerDepot);
        try {
            DbDepot dbDepot = depotService.createDepot(user, "DummyDepot", Broker.OANDA, externalDepotId, DbDepot.Type.DEMO);
            LOG.info("DbDepot created: {}", dbDepot);
            dbDepot.bought(cp, BigDecimal.TEN, BigDecimal.ONE);
            depotService.save(dbDepot);
            dbDepot = depotService.findDepotById(dbDepot.getId());
            assertEquals(1, dbDepot.getPositions().size());
            dbDepot.bought(cp, BigDecimal.TEN, new BigDecimal("3"));
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
        }
    }
}
