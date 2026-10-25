package in.expensebudget.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "financial_transactions", indexes = {
        @Index(name = "idx_transaction_owner_date", columnList = "ownerId, transactionDate")
})
public class FinancialTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(length = 200)
    private String note;

    @Column(nullable = false)
    private LocalDate transactionDate;

    protected FinancialTransaction() {
    }

    public FinancialTransaction(Long ownerId, BigDecimal amount, TransactionType type,
                                String category, String note, LocalDate transactionDate) {
        this.ownerId = ownerId;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.note = note;
        this.transactionDate = transactionDate;
    }

    public void update(BigDecimal amount, TransactionType type, String category,
                       String note, LocalDate transactionDate) {
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.note = note;
        this.transactionDate = transactionDate;
    }

    public Long getId() {
        return id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getNote() {
        return note;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }
}
