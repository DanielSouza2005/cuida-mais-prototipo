package br.com.cuidaplus.api.care_contract;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface CareContractRepository extends JpaRepository<CareContract,UUID>{boolean existsByServiceRequestId(UUID id);}
