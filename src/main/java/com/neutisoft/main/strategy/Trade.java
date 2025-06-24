package com.neutisoft.main.strategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Trade {
	public LocalDateTime entryTime;
	public LocalDateTime exitTime;
	public BigDecimal entryPrice;
	public BigDecimal exitPrice;
	public String position; // "LONG" or "SHORT"

	public Trade(LocalDateTime entryTime, LocalDateTime exitTime, BigDecimal entryPrice, BigDecimal exitPrice,
			String position) {
		this.entryTime = entryTime;
		this.exitTime = exitTime;
		this.entryPrice = entryPrice;
		this.exitPrice = exitPrice;
		this.position = position;
	}

	public BigDecimal getProfit() {
		return position.equals("LONG")
				? exitPrice.subtract(entryPrice)
						: entryPrice.subtract(exitPrice);
	}

	@Override
	public String toString() {
		return position + ": Buy @ " + entryPrice + " (" + entryTime + "), Sell @ " + exitPrice + " (" + exitTime
				+ "), Profit: " + getProfit();
	}
}
