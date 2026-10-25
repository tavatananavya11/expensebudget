package in.expensebudget.dto;

import in.expensebudget.model.FinancialTransaction;
import in.expensebudget.security.AccountPrincipal;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public final class ApiDtos {
    private ApiDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    public record UserResponse(Long id, String name, String email) {
        public static UserResponse from(AccountPrincipal principal) {
            return new UserResponse(principal.id(), principal.name(), principal.email());
        }
    }

    public record TransactionRequest(
            @NotNull @Positive BigDecimal amount,
            @NotNull in.expensebudget.model.TransactionType type,
            @NotBlank @Pattern(regexp = "Food|Rent|Travel|Shopping|Education|Healthcare|Bills|Salary|Other")
            String category,
            @Size(max = 200) String note,
            @NotNull LocalDate transactionDate) {
    }

    public record TransactionResponse(Long id, BigDecimal amount,
                                       in.expensebudget.model.TransactionType type,
                                       String category, String note, LocalDate transactionDate) {
        public static TransactionResponse from(FinancialTransaction transaction) {
            return new TransactionResponse(transaction.getId(), transaction.getAmount(), transaction.getType(),
                    transaction.getCategory(), transaction.getNote(), transaction.getTransactionDate());
        }
    }

    public record BudgetRequest(@NotNull @Positive BigDecimal amount) {
    }

    public record BudgetResponse(YearMonth month, BigDecimal amount) {
    }

    public record SummaryResponse(YearMonth month, BigDecimal income, BigDecimal expenses,
                                  BigDecimal budget, BigDecimal remainingBudget, BigDecimal savings) {
    }

    public record CategoryTotal(String category, BigDecimal amount) {
    }

    public record MonthlyReport(YearMonth month, BigDecimal income, BigDecimal expenses,
                                BigDecimal budget, BigDecimal remainingBudget, BigDecimal savings,
                                java.util.List<CategoryTotal> categories) {
    }

    public record MessageResponse(String message) {
    }
}
