package com.neutisoft.main.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neutisoft.main.dto.ReportResponse;
import com.neutisoft.main.entity.Kline;
import com.neutisoft.main.indicator.Sma;
import com.neutisoft.main.repository.KlineRepository;
import com.neutisoft.main.strategy.SmaStrategy;
import com.neutisoft.main.strategy.SmaStrategy.Trade;

import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

	private static final OkHttpClient client = new OkHttpClient();
	
	private final KlineRepository klineRepository;

	public void fetchAndStoreBinanceCandles() {
		try {
			String url = "https://api.binance.com/api/v3/klines?symbol=BTCUSDT&interval=1h&limit=100";
			Request request = new Request.Builder()
					.url(url)
					.build();

			Response response = client.newCall(request).execute();

			if (!response.isSuccessful()) {
				System.out.println("❌ Failed to fetch data: " + response.code());
				return;
			}

			ResponseBody body = response.body();
			if (body == null) {
				System.out.println("❌ Empty response body");
				return;
			}

			ObjectMapper mapper = new ObjectMapper();
			List<List<Object>> klines = mapper.readValue(body.string(), new TypeReference<List<List<Object>>>() {
			});

			if (klines.isEmpty()) {
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

		} catch (Exception e) {
			System.out.println("❌ Exception during Binance fetch: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public List<String> runSmaCrossoverStrategy() {
		List<Kline> klines = klineRepository.findAllByOrderByOpenTimeAsc();

		double[] shortSma = Sma.calculate(klines, 5);
		double[] longSma = Sma.calculate(klines, 20);

		System.out.println("=== SMA Debug Print ===");
		for (int i = 0; i < klines.size(); i++) {
			System.out.printf("Time: %s | Short SMA: %.2f | Long SMA: %.2f | Close: %s%n",
					klines.get(i).getOpenTime(),
					shortSma[i],
					longSma[i],
					klines.get(i).getClose());
		}

		List<Trade> trades = SmaStrategy.runStrategy(klines, 5, 20);
		List<String> results = new ArrayList<>();
		BigDecimal totalProfit = BigDecimal.ZERO;

		for (Trade trade : trades) {
			results.add(trade.toString());
			totalProfit = totalProfit.add(trade.getProfit());
		}

		results.add("📈 Total Profit: $" + totalProfit.setScale(2, RoundingMode.HALF_UP));
		return results;
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

		System.out.println("=== Debug: Close Prices ===");
		for (int i = klines.size() - 5; i < klines.size(); i++) {
			System.out.println("Close[" + i + "] = " + klines.get(i).getClose());
		}

		System.out.println("=== Debug: SMA(5) Values ===");
		for (int i = sma.length - 5; i < sma.length; i++) {
			System.out.println("SMA[" + i + "] = " + sma[i]);
		}

		return new ReportResponse(
				buyTime.toString(),
				sellTime.toString(),
				duration.toHours() + " hours",
				profitRate.stripTrailingZeros().toPlainString() + "%",
				"$" + profit.setScale(2, RoundingMode.HALF_UP));
	}
}
