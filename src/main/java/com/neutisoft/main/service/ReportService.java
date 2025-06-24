package com.neutisoft.main.service;

import com.neutisoft.main.dto.ReportResponse;
import com.neutisoft.main.entity.Kline;
import com.neutisoft.main.indicator.Sma;
import com.neutisoft.main.repository.KlineRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ReportService {

	@Autowired
	private KlineRepository klineRepository;

	public void fetchAndStoreBinanceCandles() {
		String url = "https://api.binance.com/api/v3/klines?symbol=BTCUSDT&interval=1h&limit=100";
		RestTemplate restTemplate = new RestTemplate();

		@SuppressWarnings("unchecked")
		ResponseEntity<List<List<Object>>> response = (ResponseEntity<List<List<Object>>>) (ResponseEntity<?>) restTemplate
				.getForEntity(url, List.class);

		List<List<Object>> klines = response.getBody();

		if (klines == null || klines.isEmpty()) {
			System.out.println("❌ No data received from Binance");
			return;
		}

		for (List<Object> k : klines) {
			LocalDateTime openTime = Instant.ofEpochMilli(((Number) k.get(0)).longValue())
					.atZone(ZoneId.systemDefault()).toLocalDateTime();
			BigDecimal high = new BigDecimal((String) k.get(2));
			BigDecimal low = new BigDecimal((String) k.get(3));
			BigDecimal close = new BigDecimal((String) k.get(4));

			Kline kline = new Kline(openTime, high, low, close);
			klineRepository.save(kline);
		}

		System.out.println("✅ Binance candles saved to DB.");
	}

	public ReportResponse simulateSimpleStrategy() {
		List<Kline> klines = klineRepository.findAllByOrderByOpenTimeAsc();
		int period = 5;

		double[] sma = Sma.calculate(klines, period);

		boolean holding = false;
		BigDecimal buyPrice = BigDecimal.ZERO;
		LocalDateTime buyTime = null;
		BigDecimal sellPrice = BigDecimal.ZERO;
		LocalDateTime sellTime = null;

		for (int i = period; i < klines.size(); i++) {
			BigDecimal close = klines.get(i).getClose();
			double smaVal = sma[i];

			if (!holding && close.doubleValue() > smaVal) {
				holding = true;
				buyPrice = close;
				buyTime = klines.get(i).getOpenTime();
			} else if (holding && close.doubleValue() < smaVal) {
				sellPrice = close;
				sellTime = klines.get(i).getOpenTime();
				break;
			}
		}

		if (!holding || sellTime == null || buyTime == null) {
			return new ReportResponse("N/A", "N/A", "0", "0%", "$0");
		}

		Duration duration = Duration.between(buyTime, sellTime);
		BigDecimal profit = sellPrice.subtract(buyPrice);
		BigDecimal profitRate = profit.divide(buyPrice, 4, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100));

		return new ReportResponse(
				buyTime.toString(),
				sellTime.toString(),
				duration.toHours() + " hours",
				profitRate.stripTrailingZeros().toPlainString() + "%",
				"$" + profit.setScale(2, RoundingMode.HALF_UP));
	}
}
