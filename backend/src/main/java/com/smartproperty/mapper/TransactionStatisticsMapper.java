package com.smartproperty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartproperty.entity.TransactionStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 流程统计Mapper
 */
@Mapper
public interface TransactionStatisticsMapper extends BaseMapper<TransactionStatistics> {
    
    /**
     * 查询指定日期范围的统计数据
     */
    @Select("SELECT * FROM transaction_statistics WHERE stat_date BETWEEN #{startDate} AND #{endDate} ORDER BY stat_date DESC, transaction_type")
    List<TransactionStatistics> selectByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * 查询最近N天的统计数据
     */
    @Select("SELECT * FROM transaction_statistics WHERE stat_date >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) ORDER BY stat_date DESC, transaction_type")
    List<TransactionStatistics> selectRecentDays(int days);
}
