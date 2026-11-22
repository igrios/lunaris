package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.PromoVoucher;
import com.lunaris.ansenuza.domain.model.PromoVoucher.Status;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromoVoucherRepository extends JpaRepository<PromoVoucher, UUID> {
    @Query(value = "SELECT nextval('promo_voucher_number_seq')", nativeQuery = true)
    long nextNumber();

    Optional<PromoVoucher> findByNumber(String number);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PromoVoucher p where p.number = :number")
    Optional<PromoVoucher> findByNumberForUpdate(@Param("number") String number);

    @Query("""
            select p from PromoVoucher p
            where (:status is null or p.status = :status)
              and (:search = '' or locate(:search, lower(p.number)) > 0
                  or locate(:search, lower(concat(p.firstName, ' ', p.lastName))) > 0
                  or (:phone <> '' and locate(:phone, p.phone) > 0))
            order by p.createdAt desc, p.number desc
            """)
    Page<PromoVoucher> search(@Param("status") Status status, @Param("search") String search,
            @Param("phone") String phone, Pageable pageable);
}
