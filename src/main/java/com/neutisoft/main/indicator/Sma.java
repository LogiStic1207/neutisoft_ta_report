package com.neutisoft.main.indicator;

import java.util.List;

import com.neutisoft.main.entity.Kline;

public class Sma {

	public static double[] calculate(List<Kline> klines, int length) {
		int dataSize = klines.size();
		
		double[] close = new double[dataSize];
		
		for (int i = 0; i < dataSize; i++) {
			close[i] = klines.get(i).getClose().doubleValue(); 
		}
		
		double[] result = new double[dataSize];
		
		for (int i = 0; i < length - 1; i++) {
			result[i] = Double.NaN;
		}
		
		// TODO calculate sma
		// result[i] = ???
		
		// return
		return result;
	}
}
