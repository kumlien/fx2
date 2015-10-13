package hoggaster.domain;

import com.google.common.collect.Lists;

import java.util.Currency;

/**
 * TODO This list need to be somehow dynamic (store in db), (but how to handle robots
 * configured for instruments which are not traded anymore?)
 */
public enum CurrencyPair {
    EUR_USD(Currency.getInstance("EUR"), Currency.getInstance("USD")),
    EUR_GBP(Currency.getInstance("EUR"), Currency.getInstance("GBP")),
    EUR_CHF(Currency.getInstance("EUR"), Currency.getInstance("CHF")),
    EUR_CAD(Currency.getInstance("EUR"), Currency.getInstance("CAD")),
    EUR_AUD(Currency.getInstance("EUR"), Currency.getInstance("AUD")),
    EUR_NZD(Currency.getInstance("EUR"), Currency.getInstance("NZD")),
    EUR_JPY(Currency.getInstance("EUR"), Currency.getInstance("JPY")),
    EUR_TRY(Currency.getInstance("EUR"), Currency.getInstance("TRY")),
    EUR_SEK(Currency.getInstance("EUR"), Currency.getInstance("SEK")),

    USD_JPY(Currency.getInstance("USD"), Currency.getInstance("JPY")),
    USD_CHF(Currency.getInstance("USD"), Currency.getInstance("CHF")),
    USD_CAD(Currency.getInstance("USD"), Currency.getInstance("CAD")),
    USD_SEK(Currency.getInstance("USD"), Currency.getInstance("SEK")),
    USD_NOK(Currency.getInstance("USD"), Currency.getInstance("NOK")),
    USD_DKK(Currency.getInstance("USD"), Currency.getInstance("DKK")),
    USD_ZAR(Currency.getInstance("USD"), Currency.getInstance("ZAR")),
    USD_HKD(Currency.getInstance("USD"), Currency.getInstance("HKD")),
    USD_SGD(Currency.getInstance("USD"), Currency.getInstance("SGD")),

    GBP_USD(Currency.getInstance("GBP"), Currency.getInstance("USD")),
    GBP_JPY(Currency.getInstance("GBP"), Currency.getInstance("JPY")),
    GBP_CHF(Currency.getInstance("GBP"), Currency.getInstance("CHF")),
    GBP_AUD(Currency.getInstance("GBP"), Currency.getInstance("AUD")),
    GBP_CAD(Currency.getInstance("GBP"), Currency.getInstance("CAD")),

    AUD_USD(Currency.getInstance("AUD"), Currency.getInstance("USD")),
    AUD_JPY(Currency.getInstance("AUD"), Currency.getInstance("JPY")),

    NZD_USD(Currency.getInstance("NZD"), Currency.getInstance("USD")),
    NZD_JPY(Currency.getInstance("NZD"), Currency.getInstance("JPY")),

    CHF_JPY(Currency.getInstance("CHF"), Currency.getInstance("JPY")),

    CAD_JPY(Currency.getInstance("CAD"), Currency.getInstance("JPY"));


//    AUD_CAD(baseCurreny, quoteCurrency), AUD_CHF(baseCurreny, quoteCurrency), AUD_HKD(baseCurreny, quoteCurrency), AUD_JPY(baseCurreny, quoteCurrency), AUD_NZD(baseCurreny, quoteCurrency),
//    AUD_SGD, AUD_USD(baseCurreny, quoteCurrency), BCO_USD(baseCurreny, quoteCurrency), CAD_CHF(baseCurreny, quoteCurrency), CAD_HKD(baseCurreny, quoteCurrency), CAD_JPY(baseCurreny, quoteCurrency), CAD_SGD(baseCurreny, quoteCurrency), CH20_CHF(baseCurreny, quoteCurrency), CHF_HKD(baseCurreny, quoteCurrency), CHF_JPY(baseCurreny, quoteCurrency), CHF_ZAR(baseCurreny, quoteCurrency), CORN_USD(baseCurreny, quoteCurrency), DE10YB_EUR(baseCurreny, quoteCurrency),
//    DE30_EUR(baseCurreny, quoteCurrency), EU50_EUR(baseCurreny, quoteCurrency), EUR_AUD(baseCurreny, quoteCurrency), EUR_CAD(baseCurreny, quoteCurrency), EUR_CHF(baseCurreny, quoteCurrency), EUR_CZK(baseCurreny, quoteCurrency), EUR_DKK(baseCurreny, quoteCurrency), EUR_GBP(baseCurreny, quoteCurrency), EUR_HKD(baseCurreny, quoteCurrency), EUR_HUF(baseCurreny, quoteCurrency), EUR_JPY(baseCurreny, quoteCurrency), EUR_NOK(baseCurreny, quoteCurrency), EUR_NZD(baseCurreny, quoteCurrency), EUR_PLN(baseCurreny, quoteCurrency),
//    EUR_SEK(baseCurreny, quoteCurrency), EUR_SGD(baseCurreny, quoteCurrency), EUR_TRY(baseCurreny, quoteCurrency),  EUR_ZAR(baseCurreny, quoteCurrency), FR40_EUR(baseCurreny, quoteCurrency), GBP_AUD(baseCurreny, quoteCurrency), GBP_CAD(baseCurreny, quoteCurrency), GBP_CHF(baseCurreny, quoteCurrency), GBP_HKD(baseCurreny, quoteCurrency), GBP_JPY(baseCurreny, quoteCurrency), GBP_NZD(baseCurreny, quoteCurrency), GBP_PLN(baseCurreny, quoteCurrency), GBP_SGD(baseCurreny, quoteCurrency), GBP_USD(baseCurreny, quoteCurrency),
//    GBP_ZAR(baseCurreny, quoteCurrency), HK33_HKD(baseCurreny, quoteCurrency), HKD_JPY(baseCurreny, quoteCurrency), JP225_USD(baseCurreny, quoteCurrency), NAS100_USD(baseCurreny, quoteCurrency), NATGAS_USD(baseCurreny, quoteCurrency), NL25_EUR(baseCurreny, quoteCurrency), NZD_CAD(baseCurreny, quoteCurrency), NZD_CHF(baseCurreny, quoteCurrency), NZD_HKD(baseCurreny, quoteCurrency), NZD_JPY(baseCurreny, quoteCurrency), NZD_SGD(baseCurreny, quoteCurrency), NZD_USD(baseCurreny, quoteCurrency), SG30_SGD(baseCurreny, quoteCurrency),
//    SGD_CHF(baseCurreny, quoteCurrency), SGD_HKD(baseCurreny, quoteCurrency), SGD_JPY(baseCurreny, quoteCurrency), SOYBN_USD(baseCurreny, quoteCurrency), SPX500_USD(baseCurreny, quoteCurrency), SUGAR_USD(baseCurreny, quoteCurrency), TRY_JPY(baseCurreny, quoteCurrency), UK100_GBP(baseCurreny, quoteCurrency), UK10YB_GBP(baseCurreny, quoteCurrency), US2000_USD(baseCurreny, quoteCurrency), US30_USD(baseCurreny, quoteCurrency), USB02Y_USD(baseCurreny, quoteCurrency), USB05Y_USD(baseCurreny, quoteCurrency),
//    USB10Y_USD(baseCurreny, quoteCurrency), USB30Y_USD(baseCurreny, quoteCurrency), USD_CAD(baseCurreny, quoteCurrency), USD_CHF(baseCurreny, quoteCurrency), USD_CNH(baseCurreny, quoteCurrency),
//    USD_CNY, USD_CZK(baseCurreny, quoteCurrency), USD_DKK(baseCurreny, quoteCurrency), USD_HKD(baseCurreny, quoteCurrency), USD_HUF(baseCurreny, quoteCurrency), USD_INR(baseCurreny, quoteCurrency), USD_JPY(baseCurreny, quoteCurrency), USD_MXN(baseCurreny, quoteCurrency), USD_NOK(baseCurreny, quoteCurrency), USD_PLN(baseCurreny, quoteCurrency), USD_SAR(baseCurreny, quoteCurrency), USD_SEK(baseCurreny, quoteCurrency), USD_SGD(baseCurreny, quoteCurrency), USD_THB(baseCurreny, quoteCurrency), USD_TRY(baseCurreny, quoteCurrency),
//    USD_TWD, USD_ZAR(baseCurreny, quoteCurrency), WHEAT_USD(baseCurreny, quoteCurrency), WTICO_USD(baseCurreny, quoteCurrency), XAG_AUD(baseCurreny, quoteCurrency), XAG_CAD(baseCurreny, quoteCurrency), XAG_CHF(baseCurreny, quoteCurrency), XAG_EUR(baseCurreny, quoteCurrency), XAG_GBP(baseCurreny, quoteCurrency), XAG_HKD(baseCurreny, quoteCurrency), XAG_JPY(baseCurreny, quoteCurrency), XAG_NZD(baseCurreny, quoteCurrency), XAG_SGD(baseCurreny, quoteCurrency), XAG_USD(baseCurreny, quoteCurrency),
//    XAU_AUD(baseCurreny, quoteCurrency), XAU_CAD(baseCurreny, quoteCurrency), XAU_CHF(baseCurreny, quoteCurrency), XAU_EUR(baseCurreny, quoteCurrency), XAU_GBP(baseCurreny, quoteCurrency), XAU_HKD(baseCurreny, quoteCurrency), XAU_JPY(baseCurreny, quoteCurrency), XAU_NZD(baseCurreny, quoteCurrency), XAU_SGD(baseCurreny, quoteCurrency), XAU_USD(baseCurreny, quoteCurrency), XAU_XAG(baseCurreny, quoteCurrency),
//    XCU_USD, XPD_USD(baseCurreny, quoteCurrency), XPT_USD(baseCurreny, quoteCurrency), ZAR_JPY(baseCurreny, quoteCurrency);

    public static CurrencyPair[] MAJORS = new CurrencyPair[] {EUR_USD, USD_JPY, GBP_USD, AUD_USD, USD_CHF, NZD_USD, USD_CAD};
    public static CurrencyPair[] MINORS = new CurrencyPair[] {EUR_GBP, EUR_CHF, EUR_CAD, EUR_AUD, EUR_NZD, EUR_JPY, GBP_JPY, CHF_JPY, CAD_JPY, AUD_JPY, NZD_JPY, GBP_CHF, GBP_AUD, GBP_CAD};
    public static CurrencyPair[] EXOTICS = new CurrencyPair[] {EUR_TRY, USD_SEK, USD_NOK, USD_DKK, USD_ZAR, USD_HKD, USD_SGD};

    public static CurrencyPair ofBaseAndQuote(Currency base, Currency quote) {
        return Lists.newArrayList(values()).parallelStream().filter(c -> c.baseCurrency == base && c.quoteCurrency == quote).findFirst().orElseThrow(() -> new IllegalArgumentException("No configured currency pair with base currency " + base.getCurrencyCode() + " and quote currency " + quote.getCurrencyCode()));
    }
    public final Currency baseCurrency;
    public final Currency quoteCurrency;

    CurrencyPair(Currency baseCurrency, Currency quoteCurrency) {
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
    }

}
