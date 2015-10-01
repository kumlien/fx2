package it;

import hoggaster.Application;
import hoggaster.domain.Broker;
import hoggaster.user.User;
import hoggaster.depot.Depot;
import hoggaster.depot.DepotService;
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
public class DepotServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(DepotServiceTest.class);

    @Autowired
    DepotService depotService;


    /**
     */
    @Test
    @Ignore
    public void testCreatePellesDepot() throws InterruptedException {
        User user = Mockito.mock(User.class);
        Mockito.when(user.getId()).thenReturn("aUserId");
        Depot depot = depotService.createDepot(user, "Pelles depot", Broker.OANDA, "9678914");
        LOG.info("Depot created: {}", depot);
    }


}
