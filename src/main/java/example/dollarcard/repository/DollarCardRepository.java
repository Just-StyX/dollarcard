package example.dollarcard.repository;

import example.dollarcard.models.DollarCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface DollarCardRepository extends CrudRepository<DollarCard, Long>, PagingAndSortingRepository<DollarCard, Long> {
    Optional<DollarCard> findByIdAndOwner(Long id, String owner);
    Page<DollarCard> findByOwner(String owner, PageRequest pageRequest);
    boolean existsByIdAndOwner(Long id, String owner);
}
