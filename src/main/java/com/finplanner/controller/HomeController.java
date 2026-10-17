package com.finplanner.controller;

import com.finplanner.model.Transaction;
import com.finplanner.model.User;
import com.finplanner.service.FinanceService;
import com.finplanner.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {
    private final FinanceService financeService;
    private final UserService userService;

    public HomeController(FinanceService financeService, UserService userService) {
        this.financeService = financeService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails.getUsername());

        // Limit dashboard recent transactions to the latest 5
        List<Transaction> recentTransactions = financeService.getTransactions(user)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("summary", financeService.getDashboardSummary(user));
        model.addAttribute("transactions", recentTransactions);
        model.addAttribute("budgets", financeService.getBudgets(user));
        model.addAttribute("categories", financeService.getCategories(user));
        model.addAttribute("budgetUsage", financeService.getBudgetUsage(user));

        // Chart data
        model.addAttribute("expensesByCategory", financeService.getExpensesByCategory(user));
        model.addAttribute("monthlyTotals", financeService.getMonthlyTotals(user));

        // Current user for nav display
        model.addAttribute("currentUser", user);

        return "index";
    }
}
