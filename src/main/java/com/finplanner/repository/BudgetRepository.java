package com.finplanner.repository;

import com.finplanner.model.Budget;
import com.finplanner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserOrderByMonthDesc(User user);
}
