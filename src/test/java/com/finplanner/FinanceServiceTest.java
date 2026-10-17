package com.finplanner;

import com.finplanner.model.Transaction;
import com.finplanner.model.TransactionType;
import com.finplanner.model.User;
import com.finplanner.repository.BudgetRepository;
import com.finplanner.repository.CategoryRepository;
import com.finplanner.repository.TransactionRepository;
import com.finplanner.service.FinanceService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FinanceServiceTest {

    @Test
    void shouldCalculateDashboardSummary() {
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        BudgetRepository budgetRepository = mock(BudgetRepository.class);

        FinanceService financeService = new FinanceService(categoryRepository, transactionRepository, budgetRepository);

        User user = new User();
        user.setId(1L);

        when(transactionRepository.findByUserOrderByDateDesc(user)).thenReturn(List.of(
                new Transaction(99L, "Bonus", new BigDecimal("1000.00"), TransactionType.INCOME, "Salary", LocalDate.now(), user),
                new Transaction(100L, "Dinner", new BigDecimal("80.00"), TransactionType.EXPENSE, "Food", LocalDate.now(), user)
        ));

        var summary = financeService.getDashboardSummary(user);

        assertEquals(new BigDecimal("1000.00"), summary.getTotalIncome());
        assertEquals(new BigDecimal("80.00"), summary.getTotalExpenses());
        assertEquals(new BigDecimal("920.00"), summary.getBalance());
    }
}
