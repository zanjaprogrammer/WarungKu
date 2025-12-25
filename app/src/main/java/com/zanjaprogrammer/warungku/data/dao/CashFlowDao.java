package com.zanjaprogrammer.warungku.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.zanjaprogrammer.warungku.data.entity.CashFlow;

import java.util.List;

@Dao
public interface CashFlowDao {
    @Insert
    void insert(CashFlow cashFlow);

    @Query("SELECT * FROM cash_flow ORDER BY timestamp DESC")
    LiveData<List<CashFlow>> getAllHistory();

    @Query("SELECT SUM(amount) FROM cash_flow WHERE type = 'IN'")
    LiveData<Double> getTotalIncome();

    @Query("SELECT SUM(amount) FROM cash_flow WHERE type = 'OUT'")
    LiveData<Double> getTotalExpense();

    @Query("SELECT SUM(profit) FROM cash_flow WHERE type = 'IN'")
    LiveData<Double> getTotalProfit();

    @Query("SELECT SUM(CASE WHEN type = 'IN' THEN amount ELSE -amount END) FROM cash_flow")
    LiveData<Double> getCurrentBalance();

    @Query("SELECT SUM(amount) FROM cash_flow WHERE type = 'IN' AND timestamp BETWEEN :start AND :end")
    LiveData<Double> getIncomeInRange(long start, long end);

    @Query("SELECT SUM(amount) FROM cash_flow WHERE type = 'OUT' AND timestamp BETWEEN :start AND :end")
    LiveData<Double> getExpenseInRange(long start, long end);

    @Query("SELECT SUM(profit) FROM cash_flow WHERE type = 'IN' AND timestamp BETWEEN :start AND :end")
    LiveData<Double> getProfitInRange(long start, long end);
}
