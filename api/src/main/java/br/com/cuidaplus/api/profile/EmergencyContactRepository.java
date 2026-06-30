package br.com.cuidaplus.api.profile;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, UUID> {
  Optional<EmergencyContact> findByAssistedPerson(AssistedPerson assistedPerson);
}
