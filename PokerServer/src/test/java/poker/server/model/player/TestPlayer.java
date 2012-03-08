package poker.server.model.player;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import poker.server.model.exception.GameException;
import poker.server.model.exception.PlayerException;
import poker.server.model.game.Game;
import poker.server.model.game.GameFactory;
import poker.server.model.game.GameFactoryLocal;
import poker.server.model.game.parameters.Parameters;
import poker.server.model.game.parameters.SitAndGo;

public class TestPlayer {

	private PlayerFactoryLocal playerFactory = new PlayerFactory();
	private GameFactoryLocal gameFactory = new GameFactory();

	private Player player;
	private Game game;

	@SuppressWarnings("unused")
	private int gameTotalPot = 0;
	private int gameCurrentPot = 0;
	private int gameCurrentBet = 0;

	private int playerBet = 0;
	private int playerTokens = 0;

	private int quantity = 0;

	private void saveTestValues() {

		gameTotalPot = game.getTotalPot();
		gameCurrentPot = game.getCurrentPot();
		gameCurrentBet = game.getCurrentBet();
		playerBet = player.getCurrentBet();
		playerTokens = player.getCurrentTokens();
	}

	// BEFORE / AFTER
	@Before
	public void beforeTest() {

		Parameters param = new SitAndGo();
		param.setPlayerNumber(4);
		game = gameFactory.newGame(param);
		player = playerFactory.newPlayer("Lucas", "1234");

		Player player2 = playerFactory.newPlayer("rafik", "dsd");
		Player player3 = playerFactory.newPlayer("youga", "cvcx");
		Player player4 = playerFactory.newPlayer("balla", "vcvx");

		game.add(player2);
		game.add(player3);
		game.add(player4);
		game.add(player); // end add, to have the turn to play

		game.start();
		saveTestValues();
	}

	@After
	public void afterTest() {
		game = null;
		player = null;
	}

	// NAME / PWD
	@Test
	public void testName() {
		assertEquals("Lucas", player.getName());
	}

	@Test
	public void testPwd() {
		assertEquals("1234", player.getPwd());
	}

	// RAISE TESTS
	@Test
	public void testRaiseCurrentPot() {

		quantity = 20;
		player.setCurrentTokens(50);
		saveTestValues();
		player.raise(quantity);
		
		int tokensRaised = quantity + (gameCurrentBet - playerBet);
		assertEquals(gameCurrentPot + tokensRaised, player.getGame()
				.getCurrentPot());
	}

	@Test
	public void testRaiseCurrentBet() {
		quantity = 20;
		player.setCurrentTokens(50);
		saveTestValues();
		player.raise(quantity);
		assertEquals(gameCurrentBet + quantity, player.getGame()
				.getCurrentBet());
	}

	@Test
	public void testRaisePlayerBet() {
		quantity = 20;
		player.setCurrentTokens(50);
		saveTestValues();
		player.raise(quantity);
		
		int tokensRaised = quantity + (gameCurrentBet - playerBet);
		assertEquals(playerBet + tokensRaised, player.getCurrentBet());
	}

	@Test
	public void testRaisePlayerTokens() {
		quantity = 20;
		player.setCurrentTokens(50);
		saveTestValues();
		player.raise(quantity);
		
		int tokensRaised = quantity + (gameCurrentBet - playerBet);
		assertEquals(playerTokens - tokensRaised, player.getCurrentTokens());
	}

	@Test(expected = PlayerException.class)
	public void testRaiseNotEnough() {
		quantity = 80;
		player.setCurrentTokens(50);
		saveTestValues();
		player.raise(quantity);
	}

	// CALL TESTS
	@Test
	public void testCallCurrentPot() {
		player.setCurrentTokens(50);
		saveTestValues();
		player.call();
		
		int nbTokenToCall = (gameCurrentBet - playerBet);
		assertEquals(gameCurrentPot + nbTokenToCall, player.getGame()
				.getCurrentPot());
	}

	@Test
	public void testCallCurrentBet() {
		player.setCurrentTokens(50);
		saveTestValues();
		player.call();
		assertEquals(gameCurrentBet + quantity, player.getGame()
				.getCurrentBet());
	}

	@Test
	public void testCallPlayerBet() {
		player.setCurrentTokens(50);
		saveTestValues();
		player.call();
		
		int nbTokenToCall = (gameCurrentBet - playerBet);
		assertEquals(playerBet + nbTokenToCall, player.getCurrentBet());
	}

	@Test
	public void testCallPlayerTokens() {
		player.setCurrentTokens(50);
		saveTestValues();
		player.call();
		
		int nbTokenToCall = (gameCurrentBet - playerBet);
		assertEquals(playerTokens - nbTokenToCall, player.getCurrentTokens());
	}

	@Test(expected = PlayerException.class)
	public void testCallNotEnough() {
		player.setCurrentTokens(50);
		game.setCurrentBet(60);
		saveTestValues();
		player.call();
	}

	// ALL IN
	@Test
	public void testAllInCurrentPot() {
		saveTestValues();
		player.allIn();
		assertEquals(gameCurrentPot + playerTokens, player.getGame()
				.getCurrentPot());
	}

	@Test
	public void testAllInPlayerBet() {
		saveTestValues();
		player.allIn();
		assertEquals(playerBet + playerTokens, player.getCurrentBet());
	}

	@Test
	public void testAllInPlayerTokens() {
		saveTestValues();
		player.allIn();
		assertEquals(0, player.getCurrentTokens());
	}

	// CHECK
	@Test
	public void testCheckEnough() {
		game.updateCurrentBet(50);
		player.setCurrentBet(70);
		saveTestValues();
		player.check();
	}

	@Test(expected = PlayerException.class)
	public void testCheckNotEnough() {
		game.updateCurrentBet(60);
		player.setCurrentBet(0);
		saveTestValues();
		player.check();
	}
	
	// BUY IN
	@Test
	public void testBuyIn() {
		Player playerBuyIn = new Player("test", "test");
		int expectedMoney = 50;
		assertEquals(expectedMoney, playerBuyIn.getMoney());
		
		game.add(playerBuyIn);
		
		expectedMoney = 50 - game.getGameType().getBuyIn();
		assertEquals(expectedMoney, playerBuyIn.getMoney());
	}
	
	@Test(expected = PlayerException.class)
	public void testBuyInNotEnough() {
		Player playerBuyIn = new Player("test", "test");
		playerBuyIn.setMoney(5);
		int expectedMoney = playerBuyIn.getMoney();
		assertEquals(expectedMoney, playerBuyIn.getMoney());
		
		game.add(playerBuyIn);
	}

	// CONNECT/DISCONNECT (IN SERVICE ==> TO BE REMOVED IN THE MODEL)
	// @Test
	// public void testConnect() {
	//
	// player.connect(game);
	// int expected = 1;
	// assertEquals(expected, game.getPlayers().size());
	// }
	//
	// @Test(expected = PlayerException.class)
	// public void testFailConnect() {
	// player.setInGame();
	// player.connect(game);
	// }
	//
	// @Test
	// public void testDisconnect() {
	//
	// player.connect(game);
	// int expected = 1;
	// assertEquals(expected, game.getPlayers().size());
	//
	// player.disconnect();
	//
	// expected = 0;
	// assertEquals(expected, game.getPlayers().size());
	// }
	//
	// @Test(expected = PlayerException.class)
	// public void testFailDisconnect() {
	// player.disconnect();
	// }
}
