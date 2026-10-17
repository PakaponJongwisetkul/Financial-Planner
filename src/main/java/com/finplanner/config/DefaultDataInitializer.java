package com.finplanner.config;

import com.finplanner.model.Budget;
import com.finplanner.model.Category;
import com.finplanner.model.Transaction;
import com.finplanner.model.TransactionType;
import com.finplanner.model.User;
import com.finplanner.model.UserRole;
import com.finplanner.repository.BudgetRepository;
import com.finplanner.repository.CategoryRepository;
import com.finplanner.repository.TransactionRepository;
import com.finplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
public class DefaultDataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default.admin.username:admin}")
    private String adminUsername;

    @Value("${app.default.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.default.admin.email:admin@finplanner.local}")
    private String adminEmail;

    @Value("${app.default.admin.fullName:System Administrator}")
    private String adminFullName;

    public DefaultDataInitializer(UserRepository userRepository,
                                 CategoryRepository categoryRepository,
                                 TransactionRepository transactionRepository,
                                 BudgetRepository budgetRepository,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User adminUser = userRepository.findByUsername(adminUsername).orElseGet(() -> {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail(adminEmail);
            admin.setFullName(adminFullName);
            admin.setEnabled(true);
            admin.setRole(UserRole.ADMIN);
            return userRepository.save(admin);
        });

        if (categoryRepository.findByUserOrderByNameAsc(adminUser).isEmpty()) {
            Category salary = new Category();
            salary.setName("Salary");
            salary.setType(TransactionType.INCOME);
            salary.setUser(adminUser);

            Category freelance = new Category();
            freelance.setName("Freelance");
            freelance.setType(TransactionType.INCOME);
            freelance.setUser(adminUser);

            Category housing = new Category();
            housing.setName("Housing");
            housing.setType(TransactionType.EXPENSE);
            housing.setUser(adminUser);

            Category food = new Category();
            food.setName("Food");
            food.setType(TransactionType.EXPENSE);
            food.setUser(adminUser);

            Category transport = new Category();
            transport.setName("Transport");
            transport.setType(TransactionType.EXPENSE);
            transport.setUser(adminUser);

            Category utilities = new Category();
            utilities.setName("Utilities");
            utilities.setType(TransactionType.EXPENSE);
            utilities.setUser(adminUser);

            categoryRepository.saveAll(List.of(salary, freelance, housing, food, transport, utilities));
        }

        if (transactionRepository.findByUserOrderByDateDesc(adminUser).isEmpty()) {
            transactionRepository.saveAll(List.of(
                new Transaction(null, "Monthly salary", new BigDecimal("4200.00"), TransactionType.INCOME, "Salary", LocalDate.now().minusDays(12), adminUser),
                new Transaction(null, "Freelance project", new BigDecimal("650.00"), TransactionType.INCOME, "Freelance", LocalDate.now().minusDays(8), adminUser),
                new Transaction(null, "Apartment rent", new BigDecimal("1250.00"), TransactionType.EXPENSE, "Housing", LocalDate.now().minusDays(10), adminUser),
                new Transaction(null, "Groceries", new BigDecimal("240.50"), TransactionType.EXPENSE, "Food", LocalDate.now().minusDays(6), adminUser),
                new Transaction(null, "Fuel and transport", new BigDecimal("140.00"), TransactionType.EXPENSE, "Transport", LocalDate.now().minusDays(4), adminUser),
                new Transaction(null, "Electricity bill", new BigDecimal("85.75"), TransactionType.EXPENSE, "Utilities", LocalDate.now().minusDays(2), adminUser)
            ));
        }

        if (budgetRepository.findByUserOrderByMonthDesc(adminUser).isEmpty()) {
            String currentMonth = YearMonth.now().toString();
            budgetRepository.saveAll(List.of(
                new Budget(null, "Housing", new BigDecimal("1400.00"), currentMonth, adminUser),
                new Budget(null, "Food", new BigDecimal("500.00"), currentMonth, adminUser),
                new Budget(null, "Transport", new BigDecimal("300.00"), currentMonth, adminUser)
            ));
        }
    }
}
