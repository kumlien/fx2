package hoggaster.domain.prices;

import hoggaster.domain.CurrencyPair;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PriceRepo extends PagingAndSortingRepository<Price, Long> {

    Price findByCurrencyPairOrderByTimeDesc(CurrencyPair currencyPair);

}
