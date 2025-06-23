package com.neutisoft.main.service;

import com.neutisoft.main.dto.ReportResponse;
import com.neutisoft.main.entity.Kline;
import com.neutisoft.main.repository.KlineRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

	@Autowired
	private KlineRepository klineRepository;

	@SuppressWarnings("unchecked")
	public void fetchAndStoreBinanceCandles() {
		String url = "https://api.binance.com/api/v3/klines?symbol=BTCUSDT&interval=1h&limit=100";
		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
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

		List<Double> sma = calculateSMA(klines, period);

		boolean holding = false;
		BigDecimal buyPrice = BigDecimal.ZERO;
		LocalDateTime buyTime = null;
		BigDecimal sellPrice = BigDecimal.ZERO;
		LocalDateTime sellTime = null;

		for (int i = period; i < klines.size(); i++) {
			BigDecimal close = klines.get(i).getClose();
			double smaVal = sma.get(i - period);

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

		if (!holding || sellTime == null) {
			return new ReportResponse("N/A", "N/A", "0", "0%", "$0");
		}

		Duration duration = Duration.between(buyTime, sellTime);
		BigDecimal profit = sellPrice.subtract(buyPrice);
		BigDecimal profitRate = profit.divide(buyPrice, 4, BigDecimal.ROUND_HALF_UP)
				.multiply(BigDecimal.valueOf(100));

		return new ReportResponse(
				buyTime.toString(),
				sellTime.toString(),
				duration.toHours() + " hours",
				profitRate.stripTrailingZeros().toPlainString() + "%",
				"$" + profit.setScale(2, BigDecimal.ROUND_HALF_UP));
	}

	private List<Double> calculateSMA(List<Kline> klines, int period) {
		List<Double> sma = new ArrayList<>();

		for (int i = 0; i <= klines.size() - period; i++) {
			double sum = 0.0;
			for (int j = i; j < i + period; j++) {
				sum += klines.get(j).getClose().doubleValue();
			}
			sma.add(sum / period);
		}

		return sma;
	}
}
