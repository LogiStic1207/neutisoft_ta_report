package com.neutisoft.main;

import com.neutisoft.main.entity.Kline;
import com.neutisoft.main.indicator.Sma;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SmaTest {

    @Test
    public void testSmaCalculation() {
        List<Kline> klines = Arrays.asList(
                new Kline(LocalDateTime.now(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(10)),
                new Kline(LocalDateTime.now(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(20)),
                new Kline(LocalDateTime.now(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(30)),
                new Kline(LocalDateTime.now(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(40)),
                new Kline(LocalDateTime.now(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(50)));

        double[] result = Sma.calculate(klines, 5);
        double expected = (10 + 20 + 30 + 40 + 50) / 5.0;

        assertEquals(expected, result[4], 0.0001);
    }
}
