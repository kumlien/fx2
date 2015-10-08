package it;

import hoggaster.Application;
import hoggaster.depot.DbDepot;
import hoggaster.depot.DepotService;
import hoggaster.domain.Broker;
import hoggaster.user.User;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class DbDepotServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(DbDepotServiceTest.class);

    @Autowired
    DepotService depotService;


    /**
     */
    @Test
    @Ignore
    public void testCreatePellesDepot() throws InterruptedException {
        User user = Mockito.mock(User.class);
        Mockito.when(user.getId()).thenReturn("aUserId");
        DbDepot dbDepot = depotService.createDepot(user, "Pelles dbDepot", Broker.OANDA, "9678914", DbDepot.Type.DEMO);
        LOG.info("DbDepot created: {}", dbDepot);
    }
}
