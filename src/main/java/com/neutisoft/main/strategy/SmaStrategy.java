package com.neutisoft.main.strategy;

import com.neutisoft.main.entity.Kline;
import com.neutisoft.main.indicator.Sma;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SmaStrategy {

    public static List<Trade> runStrategy(List<Kline> klines, int shortPeriod, int longPeriod) {
        List<Trade> trades = new ArrayList<>();

        if (klines == null || klines.size() <= Math.max(shortPeriod, longPeriod)) {
            return trades;
        }

        double[] shortSma = Sma.calculate(klines, shortPeriod);
        double[] longSma = Sma.calculate(klines, longPeriod);

        boolean holdingLong = false;
        boolean holdingShort = false;

        LocalDateTime entryTime = null;
        BigDecimal entryPrice = BigDecimal.ZERO;

        for (int i = Math.max(shortPeriod, longPeriod); i < klines.size(); i++) {
            double prevShort = shortSma[i - 1];
            double prevLong = longSma[i - 1];
            double currShort = shortSma[i];
            double currLong = longSma[i];
            Kline currentKline = klines.get(i);
            BigDecimal close = currentKline.getClose();

            // Golden Cross
            if (prevShort <= prevLong && currShort > currLong) {
                if (holdingShort) {
                    trades.add(new Trade(entryTime, currentKline.getOpenTime(), entryPrice, close, "SHORT"));
                    holdingShort = false;
                }
                if (!holdingLong) {
                    entryTime = currentKline.getOpenTime();
                    entryPrice = close;
                    holdingLong = true;
                }
            }

            // Death Cross
            else if (prevShort >= prevLong && currShort < currLong) {
                if (holdingLong) {
                    trades.add(new Trade(entryTime, currentKline.getOpenTime(), entryPrice, close, "LONG"));
                    holdingLong = false;
                }
                if (!holdingShort) {
                    entryTime = currentKline.getOpenTime();
                    entryPrice = close;
                    holdingShort = true;
                }
            }
        }

        // Close any remaining open position at the end
        Kline last = klines.get(klines.size() - 1);
        if (holdingLong) {
            trades.add(new Trade(entryTime, last.getOpenTime(), entryPrice, last.getClose(), "LONG"));
        }
        if (holdingShort) {
            trades.add(new Trade(entryTime, last.getOpenTime(), entryPrice, last.getClose(), "SHORT"));
        }

        return trades;
    }
}
