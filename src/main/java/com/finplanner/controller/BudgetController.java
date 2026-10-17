package com.finplanner.controller;

import com.finplanner.model.Budget;
import com.finplanner.model.User;
import com.finplanner.service.FinanceService;
import com.finplanner.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/budgets")
public class BudgetController {
    private final FinanceService financeService;
    private final UserService userService;

    public BudgetController(FinanceService financeService, UserService userService) {
        this.financeService = financeService;
        this.userService = userService;
    }

    @GetMapping
    public String budgets(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        model.addAttribute("budgets", financeService.getBudgets(user));
        model.addAttribute("budgetUsage", financeService.getBudgetUsage(user));
        model.addAttribute("categories", financeService.getCategories(user));
        model.addAttribute("budget", new Budget());
        return "budgets";
    }

    @GetMapping("/{id}/edit")
    public String editBudget(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        Budget budget = financeService.getBudgets(user).stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Budget not found."));

        model.addAttribute("budgets", financeService.getBudgets(user));
        model.addAttribute("budgetUsage", financeService.getBudgetUsage(user));
        model.addAttribute("categories", financeService.getCategories(user));
        model.addAttribute("budget", budget);
        return "budgets";
    }

    @PostMapping("/save")
    public String saveBudget(@ModelAttribute("budget") Budget budget,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(userDetails.getUsername());

        try {
            financeService.saveBudget(budget, user);
            redirectAttributes.addFlashAttribute("success", "Budget saved successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/budgets";
    }

    @PostMapping("/{id}/delete")
    public String deleteBudget(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        financeService.deleteBudget(id, user);
        redirectAttributes.addFlashAttribute("success", "Budget deleted.");
        return "redirect:/budgets";
    }
}
