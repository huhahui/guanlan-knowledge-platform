package com.guanlan.notes.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guanlan.notes.model.base.ApiResponse;
import com.guanlan.notes.model.dto.statistic.StatisticQueryParam;
import com.guanlan.notes.model.entity.Statistic;
import com.guanlan.notes.service.StatisticService;

@RestController
@RequestMapping("/api")
public class StatisticController {

    @Autowired
    StatisticService statisticService;

    @GetMapping("/statistic")
    public ApiResponse<List<Statistic>> getStatistic(@Valid StatisticQueryParam queryParam) {
        return statisticService.getStatistic(queryParam);
    }
}
