package in.expensebudget.service;

import in.expensebudget.dto.ApiDtos;
import in.expensebudget.model.FinancialTransaction;
import in.expensebudget.model.MonthlyBudget;
import in.expensebudget.model.TransactionType;
import in.expensebudget.repository.FinancialTransactionRepository;
import in.expensebudget.repository.MonthlyBudgetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class FinanceService {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private final FinancialTransactionRepository transactions;
    private final MonthlyBudgetRepository budgets;

    public FinanceService(FinancialTransactionRepository transactions, MonthlyBudgetRepository budgets) {
        this.transactions = transactions;
        this.budgets = budgets;
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.TransactionResponse> listTransactions(Long ownerId, YearMonth month) {
        return transactions.findAllByOwnerIdAndTransactionDateBetweenOrderByTransactionDateDescIdDesc(
                        ownerId, month.atDay(1), month.atEndOfMonth())
                .stream().map(ApiDtos.TransactionResponse::from).toList();
    }

    @Transactional
    public ApiDtos.TransactionResponse createTransaction(Long ownerId, ApiDtos.TransactionRequest request) {
        FinancialTransaction saved = transactions.save(new FinancialTransaction(ownerId, money(request.amount()),
                request.type(), request.category(), cleanNote(request.note()), request.transactionDate()));
        return ApiDtos.TransactionResponse.from(saved);
    }

    @Transactional
    public ApiDtos.TransactionResponse updateTransaction(Long ownerId, Long id,
                                                         ApiDtos.TransactionRequest request) {
        FinancialTransaction transaction = ownedTransaction(ownerId, id);
        transaction.update(money(request.amount()), request.type(), request.category(),
                cleanNote(request.note()), request.transactionDate());
        return ApiDtos.TransactionResponse.from(transaction);
    }

    @Transactional
    public void deleteTransaction(Long ownerId, Long id) {
        transactions.delete(ownedTransaction(ownerId, id));
    }

    @Transactional
    public ApiDtos.BudgetResponse saveBudget(Long ownerId, YearMonth month, BigDecimal amount) {
        MonthlyBudget budget = budgets.findByOwnerIdAndBudgetMonth(ownerId, month.toString())
                .map(existing -> {
                    existing.updateAmount(money(amount));
                    return existing;
                })
                .orElseGet(() -> new MonthlyBudget(ownerId, month, money(amount)));
        MonthlyBudget saved = budgets.save(budget);
        return new ApiDtos.BudgetResponse(saved.getMonth(), saved.getAmount());
    }

    @Transactional(readOnly = true)
    public ApiDtos.SummaryResponse summary(Long ownerId, YearMonth month) {
        BigDecimal income = total(ownerId, TransactionType.INCOME, month);
        BigDecimal expenses = total(ownerId, TransactionType.EXPENSE, month);
        BigDecimal budget = budgetAmount(ownerId, month);
        return new ApiDtos.SummaryResponse(month, income, expenses, budget,
                budget.subtract(expenses), income.subtract(expenses));
    }

    @Transactional(readOnly = true)
    public ApiDtos.MonthlyReport report(Long ownerId, YearMonth month) {
        ApiDtos.SummaryResponse summary = summary(ownerId, month);
        List<ApiDtos.CategoryTotal> categories = transactions.sumByCategory(ownerId, TransactionType.EXPENSE,
                        month.atDay(1), month.atEndOfMonth()).stream()
                .map(row -> new ApiDtos.CategoryTotal((String) row[0], (BigDecimal) row[1])).toList();
        return new ApiDtos.MonthlyReport(month, summary.income(), summary.expenses(), summary.budget(),
                summary.remainingBudget(), summary.savings(), categories);
    }

    private FinancialTransaction ownedTransaction(Long ownerId, Long id) {
        return transactions.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found."));
    }

    private BigDecimal total(Long ownerId, TransactionType type, YearMonth month) {
        return transactions.sumByOwnerAndTypeAndDateRange(ownerId, type, month.atDay(1), month.atEndOfMonth());
    }

    private BigDecimal budgetAmount(Long ownerId, YearMonth month) {
        return budgets.findByOwnerIdAndBudgetMonth(ownerId, month.toString())
                .map(MonthlyBudget::getAmount).orElse(ZERO);
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String cleanNote(String note) {
        return note == null || note.isBlank() ? null : note.trim();
    }
}
