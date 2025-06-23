package com.neutisoft.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.neutisoft.main.entity.Kline;
import java.util.List;

public interface KlineRepository extends JpaRepository<Kline, Long> {

    List<Kline> findAllByOrderByOpenTimeAsc();

}
