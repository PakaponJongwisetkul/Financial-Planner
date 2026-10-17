package com.finplanner.controller;

import com.finplanner.model.Category;
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
@RequestMapping("/categories")
public class CategoryController {
    private final FinanceService financeService;
    private final UserService userService;

    public CategoryController(FinanceService financeService, UserService userService) {
        this.financeService = financeService;
        this.userService = userService;
    }

    @GetMapping
    public String categories(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        model.addAttribute("categories", financeService.getCategories(user));
        model.addAttribute("category", new Category());
        return "categories";
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") Category category,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(userDetails.getUsername());

        if (category.getName() == null || category.getName().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Category name is required.");
            return "redirect:/categories";
        }

        financeService.saveCategory(category, user);
        redirectAttributes.addFlashAttribute("success", "Category saved successfully.");
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        financeService.deleteCategory(id, user);
        redirectAttributes.addFlashAttribute("success", "Category deleted.");
        return "redirect:/categories";
    }
}
