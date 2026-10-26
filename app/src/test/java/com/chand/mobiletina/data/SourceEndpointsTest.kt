package com.chand.mobiletina.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SourceEndpointsTest {
    @Test
    fun runtimeCatalogPreservesTheExactPublicRoutes() {
        assertEquals("https://hansha.online/l2", HanshaClient.PUBLIC_RATES_URL)
        assertEquals("https://alanchand.com/", AlanChandClient.PUBLIC_HOME)
        assertEquals("https://alanchand.com/currencies-price/usd", AlanChandClient.PUBLIC_USD_PAGE)
        assertEquals("https://www.tgju.org/profile/price_dollar_rl", TgjuClient.PUBLIC_USD_PAGE)
        assertEquals("https://www.tgju.org/currency", TgjuClient.PUBLIC_CURRENCY_PAGE)
    }
}
