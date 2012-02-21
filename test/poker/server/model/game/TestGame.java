package poker.server.model.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import poker.server.model.exception.GameException;
import poker.server.model.player.Player;
import poker.server.model.player.PlayerFactory;
import poker.server.model.player.PlayerFactoryLocal;
import poker.server.service.game.GameService;

public class TestGame {

	private Cards cards;
	private PlayerFactoryLocal playerFactory = new PlayerFactory();
	private GameFactoryLocal gameFactory = new GameFactory();
	
	@Before
	public void beforeTest() {
		cards = new Cards();
	}

	@Test(expected = GameException.class)
	public void testBigNumberRandomCards() {

		cards.getRandomCards(53);
	}

	@Test
	public void testGetRandomCards() {

		List<Card> randomCards = cards.getRandomCards(4);
		int expected = 4;
		assertEquals(expected, randomCards.size());
	}

	@Test
	public void testShuffleCards() {

		Card card = cards.getRandomCards(1).get(0);
		Card actual = card;
		Card unexpected = cards.getRandomCards(1).get(0);
		assertNotSame(unexpected, actual);
	}

	@Test(expected = GameException.class)
	public void testNotNullShuffleCards() {

		cards.getRandomCards(52);
		cards.getRandomCards(1);
	}

	@Test
	public void testDealCards() {

		Player player1 = playerFactory.createUser("Rafik", "4533");
		Player player2 = playerFactory.createUser("Lucas", "1234");

		Game game = gameFactory.createGame();
		game.add(player1);
		game.add(player2);

		game.dealCards();

		assertEquals(game.getDeck().getSize(), 48);
		assertEquals(player1.currentHand.getCurrentHand().size(), 2);
		assertEquals(player2.currentHand.getCurrentHand().size(), 2);
	}

	@Test
	public void testCardnum() {

		Card cardClub = Card.ACE_CLUB;
		Card cardSpade = Card.ACE_SPADE;
		assertEquals(cardClub.getValue(), cardSpade.getValue());
	}
}
