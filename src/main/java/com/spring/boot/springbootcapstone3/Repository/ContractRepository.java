package com.spring.boot.springbootcapstone3.Repository;

import com.spring.boot.springbootcapstone3.DTO.ContractsStatisticsOutDTO;
import com.spring.boot.springbootcapstone3.Model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Integer> {
    boolean existsByServiceRequest_Id(Integer serviceRequestId);
    Contract findByServiceRequest_Id(Integer serviceRequestId);
    Contract findContractById(Integer id);
    @Query("""
           select c
           from Contract c
           left join fetch c.serviceRequest sr
           left join fetch sr.organization org
           left join fetch c.offer o
           left join fetch o.vendor v
           where c.id = :id
           """)
    Contract fetchGraphById(Integer id);

    @Query("SELECT c FROM Contract c JOIN c.offer o " +
            "WHERE c.startDate BETWEEN ?1 AND ?2 " +
            "OR c.endDate BETWEEN ?1 AND ?2 " +
            "AND o.vendor.id = ?3")
    List<Contract> giveMeContractsByStartDateAndEndDateBetweenAndVendorId(LocalDate startDate
            , LocalDate endDate
            , Integer vendorId);

    @Query("SELECT c FROM Contract c JOIN c.offer o " +
            "WHERE o.vendor.id = ?1 " +
            "AND c.status='UNPAID' " +
            "AND c.createdAt <= current_date - 15 DAY")
    List<Contract> giveMeOverdueContracts(Integer vendorId);

    @Query("SELECT c FROM Contract c JOIN c.offer o " +
            "WHERE o.vendor.id = ?1 " +
            "AND c.endDate >= current_date " +
            "AND c.endDate < current_date + 30 DAY")
    List<Contract> giveMeAlmostExpiredContracts(Integer vendorId);
    
    @Query("SELECT " +
            " NEW com.spring.boot.springbootcapstone3.DTO.ContractsStatisticsOutDTO(" +
            " SUM(CASE WHEN c.endDate >= current_date THEN 1 ELSE 0 END), " +
            " SUM(CASE WHEN c.endDate >= current_date AND c.endDate <= current_date + 30 DAY THEN 1 ELSE 0 END), " +
            " SUM(CASE WHEN c.endDate < current_date THEN 1 ELSE 0 END), " +
            " SUM(CASE WHEN c.status = 'PAID' THEN c.price ELSE 0 END)) " +
            "FROM Contract c JOIN c.offer o " +
            "WHERE o.vendor.id = ?1")
    ContractsStatisticsOutDTO giveMeContractsStatistics(Integer vendorId);

    @Query("SELECT c FROM Contract c WHERE c.transactionId is not null")
    List<Contract> giveMeContractsWithTransactionsNotEmpty();
}
