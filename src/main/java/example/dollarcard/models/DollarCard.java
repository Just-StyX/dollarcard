package example.dollarcard.models;

import org.springframework.data.annotation.Id;

public record DollarCard(@Id Long id, Double amount, String owner) {
}
