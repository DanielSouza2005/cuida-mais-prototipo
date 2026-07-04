package br.com.cuidaplus.api.profile;

import br.com.cuidaplus.api.user.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistedPersonRepository extends JpaRepository<AssistedPerson, UUID> {
  List<AssistedPerson> findByResponsibleUser(User responsibleUser);

  Optional<AssistedPerson> findByIdAndResponsibleUser(UUID id, User responsibleUser);
}
