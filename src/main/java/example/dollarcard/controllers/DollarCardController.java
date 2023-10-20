package example.dollarcard.controllers;

import example.dollarcard.repository.DollarCardRepository;
import example.dollarcard.models.DollarCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/dollarcards")
public class DollarCardController {
    private final DollarCardRepository dollarCardRepository;
    @Autowired
    public DollarCardController(DollarCardRepository dollarCardRepository) {
        this.dollarCardRepository = dollarCardRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DollarCard> findDollarCard(@PathVariable Long id, Authentication authentication) {
        Optional<DollarCard> dollarCard = dollarCardRepository.findByIdAndOwner(id, authentication.getName());
        return dollarCard.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> createDollarCard(@RequestBody DollarCard dollarCard, UriComponentsBuilder ucb, Authentication authentication) {
        var newDollarCard = new DollarCard(null, dollarCard.amount(), authentication.getName());
        var savedDollarCard = dollarCardRepository.save(newDollarCard);
        URI locationOfNewDollarCards = ucb.path("/dollarcards/{id}").buildAndExpand(savedDollarCard.id()).toUri();
        return ResponseEntity.created(locationOfNewDollarCards).build();
    }

    @GetMapping
    public ResponseEntity<List<DollarCard>> findAllDollarCards(Pageable pageable, Authentication authentication) {
        Page<DollarCard> page = dollarCardRepository.findByOwner(authentication.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "amount"))
                )
        );
        return ResponseEntity.ok(page.getContent());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateDollarCard(@PathVariable Long id, @RequestBody DollarCard dollarCard, Authentication authentication) {
        var oldDollarCard = dollarCardRepository.findByIdAndOwner(id, authentication.getName());
        if (oldDollarCard.isPresent()) {
            dollarCardRepository.save(new DollarCard(oldDollarCard.get().id(), dollarCard.amount(), authentication.getName()));
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDollarCard(@PathVariable Long id, Authentication authentication) {
        if (!dollarCardRepository.existsByIdAndOwner(id, authentication.getName())) {
            return ResponseEntity.notFound().build();
        }
        dollarCardRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
