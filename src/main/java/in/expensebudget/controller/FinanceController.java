package in.expensebudget.controller;

import in.expensebudget.dto.ApiDtos;
import in.expensebudget.security.AccountPrincipal;
import in.expensebudget.service.FinanceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api")
public class FinanceController {
    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/transactions")
    public List<ApiDtos.TransactionResponse> transactions(
            @AuthenticationPrincipal AccountPrincipal account,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return financeService.listTransactions(account.id(), month == null ? YearMonth.now() : month);
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDtos.TransactionResponse createTransaction(@AuthenticationPrincipal AccountPrincipal account,
                                                         @Valid @RequestBody ApiDtos.TransactionRequest request) {
        return financeService.createTransaction(account.id(), request);
    }

    @PutMapping("/transactions/{id}")
    public ApiDtos.TransactionResponse updateTransaction(@AuthenticationPrincipal AccountPrincipal account,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody ApiDtos.TransactionRequest request) {
        return financeService.updateTransaction(account.id(), id, request);
    }

    @DeleteMapping("/transactions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@AuthenticationPrincipal AccountPrincipal account, @PathVariable Long id) {
        financeService.deleteTransaction(account.id(), id);
    }

    @GetMapping("/summary")
    public ApiDtos.SummaryResponse summary(@AuthenticationPrincipal AccountPrincipal account,
                                           @RequestParam(required = false)
                                           @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return financeService.summary(account.id(), month == null ? YearMonth.now() : month);
    }

    @GetMapping("/report")
    public ApiDtos.MonthlyReport report(@AuthenticationPrincipal AccountPrincipal account,
                                        @RequestParam(required = false)
                                        @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return financeService.report(account.id(), month == null ? YearMonth.now() : month);
    }

    @PostMapping("/budgets")
    public ApiDtos.BudgetResponse saveBudget(@AuthenticationPrincipal AccountPrincipal account,
                                            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                                            @Valid @RequestBody ApiDtos.BudgetRequest request) {
        return financeService.saveBudget(account.id(), month, request.amount());
    }
}
