package mariia.wicopt.paymentservice.infrastructure.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateClient {

    private final RestTemplate restTemplate;

    @Value("${exchange.api-url}")
    private String apiUrl;

    @Value("${exchange.base-currency}")
    private String baseCurrency;

    /**
     * Возвращает курс: сколько baseCurrency (USD) стоит 1 единица fromCurrency.
     * Кешируется на 1 час (настройка в application.yml).
     *
     * Например: fromCurrency=THB → ~0.028 (1 THB = 0.028 USD)
     */
    @Cacheable(value = "exchangeRates", key = "#fromCurrency")
    public BigDecimal getRate(String fromCurrency) {
        if (fromCurrency.equalsIgnoreCase(baseCurrency)) {
            return BigDecimal.ONE;
        }

        String url = String.format("%s/latest?from=%s&to=%s", apiUrl, fromCurrency, baseCurrency);
        log.info("Fetching exchange rate: {} → {} from {}", fromCurrency, baseCurrency, url);

        try {
            FrankfurterResponse response = restTemplate.getForObject(url, FrankfurterResponse.class);

            if (response == null || response.getRates() == null) {
                throw new IllegalStateException("Empty response from frankfurter.app");
            }

            BigDecimal rate = response.getRates().get(baseCurrency.toUpperCase());
            if (rate == null) {
                throw new IllegalStateException("Rate not found for currency: " + baseCurrency);
            }

            log.info("Rate fetched: 1 {} = {} {}", fromCurrency, rate, baseCurrency);
            return rate;

        } catch (Exception e) {
            log.error("Failed to fetch exchange rate for {}: {}", fromCurrency, e.getMessage());
            throw new RuntimeException("Exchange rate unavailable for currency: " + fromCurrency, e);
        }
    }

    /**
     * Конвертирует сумму из fromCurrency в USD.
     * Сохраняет знак (расход остаётся отрицательным).
     */
    public BigDecimal convertToUsd(BigDecimal amount, String fromCurrency) {
        BigDecimal rate = getRate(fromCurrency.toUpperCase());
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    // Внутренний класс для десериализации ответа frankfurter.app
    @lombok.Data
    public static class FrankfurterResponse {
        private String base;
        private String date;
        private Map<String, BigDecimal> rates;
    }
}