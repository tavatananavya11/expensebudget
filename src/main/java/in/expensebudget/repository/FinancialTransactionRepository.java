package in.expensebudget.repository;

import in.expensebudget.model.FinancialTransaction;
import in.expensebudget.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {
    List<FinancialTransaction> findAllByOwnerIdAndTransactionDateBetweenOrderByTransactionDateDescIdDesc(
            Long ownerId, LocalDate start, LocalDate end);

    Optional<FinancialTransaction> findByIdAndOwnerId(Long id, Long ownerId);

    @Query("select coalesce(sum(t.amount), 0) from FinancialTransaction t " +
            "where t.ownerId = :ownerId and t.type = :type and t.transactionDate between :start and :end")
    BigDecimal sumByOwnerAndTypeAndDateRange(@Param("ownerId") Long ownerId,
                                             @Param("type") TransactionType type,
                                             @Param("start") LocalDate start,
                                             @Param("end") LocalDate end);

    @Query("select t.category, sum(t.amount) from FinancialTransaction t " +
            "where t.ownerId = :ownerId and t.type = :type and t.transactionDate between :start and :end " +
            "group by t.category order by sum(t.amount) desc")
    List<Object[]> sumByCategory(@Param("ownerId") Long ownerId,
                                 @Param("type") TransactionType type,
                                 @Param("start") LocalDate start,
                                 @Param("end") LocalDate end);
}
