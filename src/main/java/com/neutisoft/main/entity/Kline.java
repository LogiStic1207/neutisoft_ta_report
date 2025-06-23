package com.neutisoft.main.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Kline {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDateTime openTime;

	@Column(nullable = false)
	private BigDecimal high;

	@Column(nullable = false)
	private BigDecimal low;

	@Column(nullable = false)
	private BigDecimal close;

	public Kline(LocalDateTime openTime, BigDecimal high, BigDecimal low, BigDecimal close) {
		this.openTime = openTime;
		this.high = high;
		this.low = low;
		this.close = close;
	}
}
