package in.expensebudget.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "monthly_budgets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_budget_owner_month", columnNames = {"ownerId", "budgetMonth"})
})
public class MonthlyBudget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 7)
    private String budgetMonth;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    protected MonthlyBudget() {
    }

    public MonthlyBudget(Long ownerId, YearMonth month, BigDecimal amount) {
        this.ownerId = ownerId;
        this.budgetMonth = month.toString();
        this.amount = amount;
    }

    public void updateAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public YearMonth getMonth() {
        return YearMonth.parse(budgetMonth);
    }
}
