package com.finplanner;

import com.finplanner.repository.BudgetRepository;
import com.finplanner.repository.CategoryRepository;
import com.finplanner.repository.TransactionRepository;
import com.finplanner.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class FinancialPlannerApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Test
    void contextLoadsAndDefaultDataInitialized() {
        assertTrue(userRepository.findByUsername("admin").isPresent(), "Admin user should exist");
        assertFalse(categoryRepository.findAll().isEmpty(), "Default categories should be seeded");
        assertFalse(transactionRepository.findAll().isEmpty(), "Default transactions should be seeded");
        assertFalse(budgetRepository.findAll().isEmpty(), "Default budgets should be seeded");
    }
}

