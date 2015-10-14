package hoggaster.prices;

import hoggaster.domain.CurrencyPair;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PriceRepo extends PagingAndSortingRepository<Price, Long> {

    Price findByCurrencyPairOrderByTimeDesc(CurrencyPair currencyPair);

}
