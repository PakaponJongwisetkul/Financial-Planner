package com.finplanner.service;

import com.finplanner.model.Budget;
import com.finplanner.model.Category;
import com.finplanner.model.DashboardSummary;
import com.finplanner.model.Transaction;
import com.finplanner.model.TransactionType;
import com.finplanner.model.User;
import com.finplanner.repository.BudgetRepository;
import com.finplanner.repository.CategoryRepository;
import com.finplanner.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class FinanceService {
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    public FinanceService(CategoryRepository categoryRepository,
                          TransactionRepository transactionRepository,
                          BudgetRepository budgetRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
    }

    public List<Category> getCategories(User user) {
        return categoryRepository.findByUserOrderByNameAsc(user);
    }

    public List<Transaction> getTransactions(User user) {
        return transactionRepository.findByUserOrderByDateDesc(user);
    }

    public List<Budget> getBudgets(User user) {
        return budgetRepository.findByUserOrderByMonthDesc(user);
    }

    public Category saveCategory(Category category, User user) {
        if (category == null || category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        category.setUser(user);
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id, User user) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent() && category.get().getUser().getId().equals(user.getId())) {
            categoryRepository.delete(category.get());
        }
    }

    public Transaction saveTransaction(Transaction transaction, User user) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null.");
        }
        if (transaction.getDescription() == null || transaction.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description is required.");
        }
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (transaction.getCategory() == null || transaction.getCategory().isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (transaction.getType() == null) {
            throw new IllegalArgumentException("Transaction type is required.");
        }
        if (transaction.getDate() == null) {
            transaction.setDate(LocalDate.now());
        }

        transaction.setUser(user);
        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(Long id, User user) {
        Optional<Transaction> transaction = transactionRepository.findById(id);
        if (transaction.isPresent() && transaction.get().getUser().getId().equals(user.getId())) {
            transactionRepository.delete(transaction.get());
        }
    }

    public Budget saveBudget(Budget budget, User user) {
        if (budget == null || budget.getCategory() == null || budget.getCategory().isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (budget.getLimitAmount() == null || budget.getLimitAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget limit must be greater than zero.");
        }
        if (budget.getMonth() == null || budget.getMonth().isBlank()) {
            throw new IllegalArgumentException("Month is required.");
        }

        budget.setUser(user);
        return budgetRepository.save(budget);
    }

    public void deleteBudget(Long id, User user) {
        Optional<Budget> budget = budgetRepository.findById(id);
        if (budget.isPresent() && budget.get().getUser().getId().equals(user.getId())) {
            budgetRepository.delete(budget.get());
        }
    }

    public DashboardSummary getDashboardSummary(User user) {
        List<Transaction> transactions = getTransactions(user);

        BigDecimal totalIncome = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Monthly spending: only expenses for the current month
        YearMonth currentMonth = YearMonth.now();
        BigDecimal monthlySpending = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getDate() != null
                        && t.getDate().getYear() == currentMonth.getYear()
                        && t.getDate().getMonthValue() == currentMonth.getMonthValue())
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpenses);
        return new DashboardSummary(totalIncome, totalExpenses, balance, monthlySpending);
    }

    public Map<String, Double> getBudgetUsage(User user) {
        Map<String, Double> usage = new HashMap<>();
        List<Budget> budgets = getBudgets(user);
        List<Transaction> transactions = getTransactions(user);

        for (Budget budget : budgets) {
            if (budget.getLimitAmount() == null || budget.getLimitAmount().compareTo(BigDecimal.ZERO) == 0) {
                usage.put(budget.getCategory(), 0.0);
                continue;
            }

            YearMonth ym = parseYearMonth(budget.getMonth());
            if (ym == null) {
                usage.put(budget.getCategory(), 0.0);
                continue;
            }

            BigDecimal spent = transactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .filter(t -> budget.getCategory().equalsIgnoreCase(t.getCategory()))
                    .filter(t -> t.getDate() != null
                            && t.getDate().getYear() == ym.getYear()
                            && t.getDate().getMonthValue() == ym.getMonthValue())
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double percentage = spent.multiply(BigDecimal.valueOf(100))
                    .divide(budget.getLimitAmount(), 2, RoundingMode.HALF_UP)
                    .doubleValue();
            usage.put(budget.getCategory(), percentage);
        }

        return usage;
    }

    /**
     * Returns a map of category name → total expense amount across all time,
     * used for the dashboard pie chart.
     */
    public Map<String, BigDecimal> getExpensesByCategory(User user) {
        List<Transaction> transactions = getTransactions(user);
        Map<String, BigDecimal> result = new HashMap<>();

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE && t.getCategory() != null) {
                result.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
            }
        }

        return result;
    }

    /**
     * Returns a LinkedHashMap of the last 6 months (ascending, "YYYY-MM" keys),
     * each containing a nested map with "income" and "expense" totals.
     * Used for the dashboard bar chart.
     */
    public Map<String, Map<String, BigDecimal>> getMonthlyTotals(User user) {
        List<Transaction> transactions = getTransactions(user);

        // Build ordered map for the last 6 months (oldest → newest)
        LinkedHashMap<String, Map<String, BigDecimal>> result = new LinkedHashMap<>();
        YearMonth current = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            String key = month.toString(); // "YYYY-MM"
            Map<String, BigDecimal> monthData = new HashMap<>();
            monthData.put("income", BigDecimal.ZERO);
            monthData.put("expense", BigDecimal.ZERO);
            result.put(key, monthData);
        }

        for (Transaction t : transactions) {
            if (t.getDate() == null) continue;
            YearMonth txMonth = YearMonth.from(t.getDate());
            String key = txMonth.toString();

            if (!result.containsKey(key)) continue; // outside the 6-month window

            if (t.getType() == TransactionType.INCOME) {
                result.get(key).merge("income", t.getAmount(), BigDecimal::add);
            } else if (t.getType() == TransactionType.EXPENSE) {
                result.get(key).merge("expense", t.getAmount(), BigDecimal::add);
            }
        }

        return result;
    }

    private YearMonth parseYearMonth(String str) {
        if (str == null || str.isBlank()) return null;
        String trimmed = str.trim();
        try {
            return YearMonth.parse(trimmed);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return YearMonth.parse(trimmed, DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return YearMonth.parse(trimmed, DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return YearMonth.parse(trimmed, DateTimeFormatter.ofPattern("yyyy-M", Locale.ENGLISH));
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }
}
