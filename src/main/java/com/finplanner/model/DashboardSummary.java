package com.finplanner.model;

import java.math.BigDecimal;

public class DashboardSummary {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal balance;
    private BigDecimal monthlySpending;

    public DashboardSummary() {
        this.totalIncome = BigDecimal.ZERO;
        this.totalExpenses = BigDecimal.ZERO;
        this.balance = BigDecimal.ZERO;
        this.monthlySpending = BigDecimal.ZERO;
    }

    public DashboardSummary(BigDecimal totalIncome, BigDecimal totalExpenses, BigDecimal balance, BigDecimal monthlySpending) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.balance = balance;
        this.monthlySpending = monthlySpending;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getMonthlySpending() {
        return monthlySpending;
    }

    public void setMonthlySpending(BigDecimal monthlySpending) {
        this.monthlySpending = monthlySpending;
    }
}
