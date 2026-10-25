package in.expensebudget.repository;

import in.expensebudget.model.MonthlyBudget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {
    Optional<MonthlyBudget> findByOwnerIdAndBudgetMonth(Long ownerId, String budgetMonth);
}
