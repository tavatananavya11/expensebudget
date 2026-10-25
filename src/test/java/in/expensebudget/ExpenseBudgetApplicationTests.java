package in.expensebudget;

import in.expensebudget.dto.ApiDtos;
import in.expensebudget.model.FinancialTransaction;
import in.expensebudget.model.TransactionType;
import in.expensebudget.repository.FinancialTransactionRepository;
import in.expensebudget.repository.MonthlyBudgetRepository;
import in.expensebudget.repository.UserAccountRepository;
import in.expensebudget.service.AccountService;
import in.expensebudget.service.FinanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExpenseBudgetApplicationTests {
    @Autowired
    private FinanceService financeService;

    @Autowired
    private FinancialTransactionRepository transactions;

    @Autowired
    private MonthlyBudgetRepository budgets;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserAccountRepository users;

    @Test
    void registrationRejectsEmailThatWasPreviouslyRegistered() {
        accountService.register(new ApiDtos.RegisterRequest(
                "First User", "person@example.com", "StrongPass123"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        accountService.register(new ApiDtos.RegisterRequest(
                                "Same User", "  PERSON@example.com ", "AnotherPass123")))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("409 CONFLICT");
        assertThat(users.count()).isEqualTo(1);
    }

    @Test
    void dashboardAndReportsOnlyIncludeTheRequestedAccount() {
        YearMonth month = YearMonth.of(2026, 10);
        transactions.save(new FinancialTransaction(11L, new BigDecimal("1000.00"),
                TransactionType.INCOME, "Salary", null, month.atDay(5)));
        transactions.save(new FinancialTransaction(11L, new BigDecimal("250.00"),
                TransactionType.EXPENSE, "Food", null, month.atDay(6)));
        transactions.save(new FinancialTransaction(12L, new BigDecimal("9000.00"),
                TransactionType.EXPENSE, "Rent", null, month.atDay(7)));
        financeService.saveBudget(11L, month, new BigDecimal("500.00"));

        ApiDtos.MonthlyReport report = financeService.report(11L, month);
        assertThat(report.income()).isEqualByComparingTo("1000.00");
        assertThat(report.expenses()).isEqualByComparingTo("250.00");
        assertThat(report.budget()).isEqualByComparingTo("500.00");
        assertThat(report.remainingBudget()).isEqualByComparingTo("250.00");
        assertThat(report.savings()).isEqualByComparingTo("750.00");
        assertThat(report.categories()).hasSize(1);
        assertThat(report.categories().getFirst().category()).isEqualTo("Food");
        assertThat(financeService.listTransactions(11L, month)).hasSize(2);
    }

    @Test
    void oneAccountCannotModifyAnotherAccountsTransaction() {
        FinancialTransaction otherAccount = transactions.save(new FinancialTransaction(22L,
                new BigDecimal("10.00"), TransactionType.EXPENSE, "Food", null, LocalDate.of(2026, 10, 1)));
        ApiDtos.TransactionRequest edit = new ApiDtos.TransactionRequest(new BigDecimal("20.00"),
                TransactionType.EXPENSE, "Food", null, LocalDate.of(2026, 10, 2));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> financeService.updateTransaction(11L, otherAccount.getId(), edit))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
    }
}
