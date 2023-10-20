package example.dollarcard;

import example.dollarcard.models.DollarCard;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class DollarCardJsonTest {
    private final JacksonTester<DollarCard> json;
    private final JacksonTester<DollarCard[]> jsonList;
    private DollarCard[] dollarCards;
    @Autowired
    public DollarCardJsonTest(JacksonTester<DollarCard> json, JacksonTester<DollarCard[]> jsonList) {
        this.json = json;
        this.jsonList = jsonList;
    }

    @BeforeEach
    void setUp() {
        dollarCards = Arrays.array(
                new DollarCard(20L, 250.55, "mich"),
                new DollarCard(21L, 250.55, "mark"),
                new DollarCard(22L, 250.55, "ama")
        );
    }

    @Test
    public void serializedDollarCard() throws IOException {
        var dollarCard = dollarCards[0];
        assertThat(json.write(dollarCard)).isStrictlyEqualToJson("single.json");
        assertThat(json.write(dollarCard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(dollarCard)).extractingJsonPathNumberValue("@.id").isEqualTo(20);
        assertThat(json.write(dollarCard)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(dollarCard)).extractingJsonPathNumberValue("@.amount").isEqualTo(250.55);
    }

    @Test
    public void deserializedDollarCard() throws IOException {
        var expected = """
                {
                  "id": 20,
                  "amount": 250.55,
                  "owner": "mich"
                }
                """;

        assertThat(json.parse(expected)).isEqualTo(new DollarCard(20L, 250.55, "mich"));
        assertThat(json.parseObject(expected).id()).isEqualTo(20);
        assertThat(json.parseObject(expected).amount()).isEqualTo(250.55);
    }

    @Test
    public void serializedDollarCardList() throws IOException{
        assertThat(jsonList.write(dollarCards)).isStrictlyEqualToJson("list.json");
    }

    @Test
    public void deserializedDollarCardList() throws IOException {
        var expected = """
                [
                  {"id": 20, "amount": 250.55, "owner": "mich"},
                  {"id": 21, "amount": 250.55, "owner": "mark"},
                  {"id": 22, "amount": 250.55, "owner": "ama"}
                ]
                """;
        assertThat(jsonList.parse(expected)).isEqualTo(dollarCards);
    }
}
