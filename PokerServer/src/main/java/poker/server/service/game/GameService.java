package poker.server.service.game;

/**
 * Service class : GameService
 *         
 * @author <b> Rafik Ferroukh </b> <br>
 *         <b> Lucas Kerdoncuff </b> <br>
 *         <b> Xan Lucu </b> <br>
 *         <b> Youga Mbaye </b> <br>
 *         <b> Balla Seck </b> <br>
 * <br>
 *         University Bordeaux 1, Software Engineering, Master 2 <br>
 *
 * @see Game
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import poker.server.infrastructure.RepositoryAccessToken;
import poker.server.infrastructure.RepositoryConsumer;
import poker.server.infrastructure.RepositoryGame;
import poker.server.infrastructure.RepositoryGameType;
import poker.server.infrastructure.RepositoryPlayer;
import poker.server.infrastructure.auth.Consumer;
import poker.server.model.exception.ErrorMessage;
import poker.server.model.exception.GameException;
import poker.server.model.exception.SignatureException;
import poker.server.model.game.Game;
import poker.server.model.game.GameFactoryLocal;
import poker.server.model.game.Pot;
import poker.server.model.game.card.Card;
import poker.server.model.game.parameters.GameType;
import poker.server.model.player.Hand;
import poker.server.model.player.Player;
import poker.server.model.player.PlayerFactoryLocal;
import poker.server.service.AbstractPokerService;
import poker.server.service.game.timer.TimerUpdateBlinds;
import poker.server.service.player.PlayerService;
import poker.server.service.sign.SignatureService;

@Stateless
@Path("/game")
public class GameService extends AbstractPokerService {

	@EJB
	private RepositoryGame repositoryGame;

	@EJB
	private RepositoryPlayer repositoryPlayer;

	@EJB
	private RepositoryGameType repositoryParameters;

	@EJB
	private RepositoryAccessToken repositoryAccessToken;

	@EJB
	private GameFactoryLocal gameFactory;

	@EJB
	private PlayerFactoryLocal playerFactory;

	@EJB
	private RepositoryConsumer repositoryConsumer;

	/**
	 * Insure the connection of a player in a game, the result is a
	 * {@code JSONObject} that will contains the informations about the
	 * connection's state, if the connection of the player is correct then
	 * {@code stat = true} else {@code stat = false} and with {@code message}
	 * 
	 * @param name
	 *            the name of the player to connect
	 * 
	 * @param pwd
	 *            the password of the player to connect
	 * 
	 * @return {@code JSONObject} contains the information related to the
	 *         connection's state
	 * 
	 */
	@GET
	@Path("/authenticate/{consumerKey}/{signature}")
	public Response authenticate(@PathParam("consumerKey") String consumerKey,
			@PathParam("signature") String signature) {

		String[] infos = null;
		try {
			infos = verifySignature(SignatureService.AUTHENTICATE, consumerKey,
					signature);
		} catch (SignatureException e) {
			return error(e.getError());
		}

		String name = infos[6];
		String pwd = infos[8];

		JSONObject json = new JSONObject();
		Player player = repositoryPlayer.load(name);

		if (player == null) {

			player = playerFactory.newPlayer(name, pwd);
			repositoryPlayer.save(player);

		} else {

			if (!player.getPwd().equals(pwd))
				return error(ErrorMessage.NOT_CORRECT_PASSWORD);
			else if (player.isInGame()) {
				updateJSON(json, "alreadyConnected", true);
				updateJSON(json, "tableName", player.getGame().getName());
			} else
				updateJSON(json, "alreadyConnected", false);
		}

		updateJSON(json, STAT, OK);
		return buildResponse(json);
	}

	/**
	 * Connects a player's name given as parameter a the game given also as
	 * parameter
	 * 
	 * @return
	 */
	@GET
	@Path("/connectGame/{consumerKey}/{signature}")
	public Response connect(@PathParam("consumerKey") String consumerKey,
			@PathParam("signature") String signature) {

		String[] infos = null;
		try {
			infos = verifySignature(SignatureService.CONNECT, consumerKey,
					signature);
		} catch (SignatureException e) {
			return error(e.getError());
		}

		String tableName = infos[6];
		String playerName = infos[8];

		Game game = repositoryGame.load(tableName);

		if (game == null)
			return error(ErrorMessage.GAME_NOT_EXIST);
		else if (game.isStarted())
			return error(ErrorMessage.GAME_ALREADY_STARTED);

		Player player = repositoryPlayer.load(playerName);

		if (player == null)
			return error(ErrorMessage.PLAYER_NOT_EXIST);
		else if (player.isInGame())
			return error(ErrorMessage.PLAYER_INGAME);

		game.add(player);
		repositoryGame.update(game);

		JSONObject json = new JSONObject();
		updateJSON(json, STAT, OK);
		updateJSON(json, "tableName", game.getName());
		return buildResponse(json);
	}

	/**
	 * Returns the status of all games (types) that is not ready to start
	 * 
	 * @return
	 */
	@GET
	@Path("/getWaitingTablesList/{consumerKey}")
	public Response getWaitingTablesList(
			@PathParam("consumerKey") String consumerKey) {

		Consumer consumer = repositoryConsumer.load(consumerKey);
		if (consumer == null)
			return error(ErrorMessage.UNKNOWN_CONSUMER_KEY);

		List<Game> currentGames = new ArrayList<Game>();
		List<GameType> parameters = new ArrayList<GameType>();

		currentGames = repositoryGame.getNotReadyGames();
		parameters = repositoryParameters.loadAll();

		if (parameters.size() == 0) {
			// by default if there is not a default parameter, create manually a
			// game with this default parameter
			Game newGame = gameFactory.newGame();
			newGame = repositoryGame.save(newGame);
			currentGames.add(newGame);

		} else if (currentGames.size() < parameters.size()) {

			for (GameType param : parameters) {

				if (!repositoryGame.exist(param)) {
					Game newGame = gameFactory.newGame(param);
					newGame = repositoryGame.save(newGame);
					currentGames.add(newGame);
				}
			}
		}

		currentGames = repositoryGame.getReadyOrNotGames();
		JSONArray gamesStatus = new JSONArray();

		for (Game game : currentGames)
			gamesStatus.put(getGameStatus(game));

		JSONObject json = new JSONObject();
		updateJSON(json, STAT, OK);

		updateJSON(json, "gamesStatus", gamesStatus);

		return buildResponse(json);
	}

	/**
	 * Returns the status of all games (types) that is not ready to start
	 * 
	 * @return
	 */
	@GET
	@Path("/getWaitingGameData/{consumerKey}/{tableName}")
	public Response getWaitingGameData(
			@PathParam("consumerKey") String consumerKey,
			@PathParam("tableName") String tableName) {

		Consumer consumer = repositoryConsumer.load(consumerKey);
		if (consumer == null)
			return error(ErrorMessage.UNKNOWN_CONSUMER_KEY);

		Response resp = null;
		Game currentGame = repositoryGame.load(tableName);

		if (currentGame == null)
			resp = error(ErrorMessage.GAME_NOT_EXIST);

		else if (currentGame != null) {
			if (currentGame.isStarted())
				resp = error(ErrorMessage.GAME_ALREADY_STARTED);
			else
				resp = buildResponse(getGameStatus(currentGame));
		}

		return resp;
	}

	/**
	 * Returns all the informations about the current game {@code tableName}
	 * 
	 * @return
	 */
	@GET
	@Path("/getCurrentGameData/{consumerKey}/{tableName}/{playerName}")
	public Response getCurrentGameData(
			@PathParam("consumerKey") String consumerKey,
			@PathParam("tableName") String tableName,
			@PathParam("playerName") String playerName) {

		JSONObject json = new JSONObject();
		Consumer consumer = repositoryConsumer.load(consumerKey);
		if (consumer == null)
			return error(ErrorMessage.UNKNOWN_CONSUMER_KEY);

		Response resp = null;

		Player player = repositoryPlayer.load(playerName);

		if (player == null)
			return error(ErrorMessage.PLAYER_NOT_EXIST);
		else if (!player.isInGame())
			return error(ErrorMessage.PLAYER_NOT_CONNECTED);

		Game currentGame = repositoryGame.load(tableName);
		if (currentGame == null)
			resp = error(ErrorMessage.GAME_NOT_EXIST);

		else if (currentGame != null) {

			if (!currentGame.isStarted())
				resp = error(ErrorMessage.GAME_NOT_READY_TO_START);
			else {
				json = getGameData(currentGame, player);
				resp = buildResponse(json);
			}
		}
		return resp;
	}

	/**
	 * Returns the winners and the list of hand's players, after a showDown
	 */
	@GET
	@Path("/showDown/{consumerKey}/{signature}")
	public Response showDown(@PathParam("consumerKey") String consumerKey,
			@PathParam("signature") String signature) {

		String[] infos = null;
		try {
			infos = verifySignature(SignatureService.SHOWDOWN, consumerKey,
					signature);
		} catch (SignatureException e) {
			return error(e.getError());
		}

		String tableName = infos[6];

		JSONObject json = new JSONObject();
		Game game = repositoryGame.load(tableName);

		if (game == null)
			return error(ErrorMessage.GAME_NOT_EXIST);
		else if (!game.isStarted())
			return error(ErrorMessage.GAME_NOT_READY_TO_START);

		List<Pot> winners = null;

		try {
			winners = game.showDown();
			repositoryGame.update(game);

		} catch (GameException e) {
			return error(e.getError());
		}

		JSONArray jsonWinnersPot = new JSONArray();

		for (Pot pot : winners) {

			for (Player player : pot.getPlayers()) {

				JSONObject jsonWinner = new JSONObject();
				updateJSON(jsonWinner, "winner", player.getName());
				updateJSON(jsonWinner, "cards", getCards(player.getBestHand()));
				updateJSON(jsonWinner, "pot", pot.getValueReward());
				updateJSON(jsonWinner, "idPot", pot.getId());

				jsonWinnersPot.put(jsonWinner);
			}
		}

		updateJSON(json, STAT, OK);
		updateJSON(json, "winners", jsonWinnersPot);

		return buildResponse(json);
	}

	/***********************
	 * END OF THE SERVICES *
	 ***********************/

	/**
	 * Returns the list of the cards that represent the best hand
	 */
	private JSONArray getCards(Hand bestHand) {

		JSONArray jsonCards = new JSONArray();
		for (Card card : bestHand.getCards()) {
			jsonCards.put(card.getId());
		}
		return jsonCards;
	}

	/**
	 * Returns the status of the game
	 */
	private JSONObject getGameStatus(Game currentGame) {

		JSONObject json = new JSONObject();

		if (currentGame.isReady()) {

			currentGame.start();
			// startTimerUpdateBlinds(currentGame);
			repositoryGame.update(currentGame);

			System.out.println("GAME SERVICE AFTER TIMER");

			updateJSON(json, "startGame", true);

		} else if (currentGame.isStarted())
			updateJSON(json, "startGame", true);

		else
			updateJSON(json, "startGame", false);

		updateJSON(json, "playersNames", getPlayerNames(currentGame));
		updateJSON(json, "tableName", currentGame.getName());
		updateJSON(json, "gameTypeName", currentGame.getGameType().getName());
		updateJSON(json, "buyIn", currentGame.getGameType().getBuyIn());
		updateJSON(json, "playerBudget", currentGame.getGameType().getTokens());
		updateJSON(json, "bigBlind", currentGame.getBigBlind());
		updateJSON(json, "smallBlind", currentGame.getSmallBlind());
		updateJSON(json, "prizePool", currentGame.getPrizePool());
		updateJSON(json, STAT, OK);

		return json;
	}

	/**
	 * build the data of the game (all informations)
	 * 
	 * @return
	 * 
	 */
	private JSONObject getGameData(Game currentGame, Player selectedPlayer) {

		JSONObject json = new JSONObject();
		updateJSON(json, STAT, OK);

		updateJSON(json, "tableName", currentGame.getName());
		updateJSON(json, "bigBlind", currentGame.getBigBlind());
		updateJSON(json, "smallBlind", currentGame.getSmallBlind());
		updateJSON(json, "prizePool", currentGame.getPrizePool());

		updateJSON(json, "dealer", currentGame.getDealerPlayer().getName());
		updateJSON(json, "smallBlindPlayer", currentGame.getSmallBlindPlayer()
				.getName());
		updateJSON(json, "bigBlindPlayer", currentGame.getBigBlindPlayer()
				.getName());
		updateJSON(json, "currentPlayer", currentGame.getCurrentPlayer()
				.getName());

		JSONArray playersInfos = new JSONArray();

		for (Player player : currentGame.getPlayers()) {

			JSONObject jsonPlayer = new JSONObject();
			updateJSON(jsonPlayer, "name", player.getName());
			updateJSON(jsonPlayer, "tokens", player.getCurrentTokens());
			updateJSON(jsonPlayer, "action", player.getLastAction());
			updateJSON(jsonPlayer, "value", 0);
			updateJSON(jsonPlayer, "status", player.getStatus());

			playersInfos.put(jsonPlayer);
		}

		updateJSON(json, "players", playersInfos);

		JSONObject flippedCardsJson = new JSONObject();
		updateJSON(flippedCardsJson, "cards", getCards(currentGame));
		updateJSON(flippedCardsJson, "state", currentGame.getCurrentRound());
		updateJSON(json, "flippedCards", flippedCardsJson);

		List<Integer> cards = new ArrayList<Integer>();
		for (Card card : selectedPlayer.getCurrentHand().getCards())
			cards.add(card.getId());

		updateJSON(json, "userCards", cards);

		updateJSON(json, "pots", currentGame.getPots());
		updateJSON(json, "totalPot", currentGame.getTotalPot());

		List<Player> playersRank = currentGame.getPlayersRank();

		JSONArray playersRanks = new JSONArray();

		for (Player player : playersRank) {

			JSONObject jsonPlayer = new JSONObject();
			updateJSON(jsonPlayer, "position", playersRank.indexOf(player));
			updateJSON(jsonPlayer, "name", player.getName());

			playersRanks.put(jsonPlayer);
		}

		updateJSON(json, "playerRanks", playersRanks);

		JSONArray possibleActions = new JSONArray();

		for (Player player : currentGame.getPlayers()) {

			JSONObject possibleAction = new JSONObject();

			if (player.isInGame() && !player.isfolded()) {

				Map<String, Integer> possActions = player.getPossibleActions();

				if (possActions.containsKey("check"))
					updateJSON(possibleAction, "action", PlayerService.CHECK);
				if (possActions.containsKey("allIn"))
					updateJSON(possibleAction, "action", PlayerService.ALLIN);
				if (possActions.containsKey("raise")) {
					updateJSON(possibleAction, "action", PlayerService.RAISE);
					updateJSON(possibleAction, "value",
							possActions.get("raise"));
				}
				if (possActions.containsKey("fold"))
					updateJSON(possibleAction, "action", PlayerService.FOLD);
				if (possActions.containsKey("call"))
					updateJSON(possibleAction, "action", PlayerService.CALL);
			}

			JSONObject jsonActionsPlayer = new JSONObject();
			updateJSON(jsonActionsPlayer, "playerName", player.getName());
			updateJSON(jsonActionsPlayer, "actions", possibleAction);

			possibleActions.put(jsonActionsPlayer);
		}

		updateJSON(json, "possibleActions", possibleActions);
		
		return json;
	}

	/**
	 * Returns the list of players of the game {@code game}
	 */
	private List<String> getPlayerNames(Game game) {

		List<Player> players = game.getPlayers();
		List<String> playerInfos = new ArrayList<String>();

		for (Player p : players)
			playerInfos.add(p.getName());

		return playerInfos;
	}

	/**
	 * Return the list of cards of the game {@code game}
	 */
	private List<Integer> getCards(Game game) {

		List<Card> cards = game.getFlipedCards();
		List<Integer> flipedCards = new ArrayList<Integer>();

		for (Card card : cards)
			flipedCards.add(card.getId());

		return flipedCards;
	}

	/**
	 * Call the verify method from signatureService conformed on type given as
	 * parameter
	 */
	private String[] verifySignature(int type, String consumerKey,
			String signature) {

		return SignatureService.getInstance().verifySignature(type,
				consumerKey, signature, repositoryConsumer,
				repositoryAccessToken);
	}

	/**
	 * Updates Blinds
	 */
	@SuppressWarnings("unused")
	private void startTimerUpdateBlinds(Game currentGame) {
		new Thread(new TimerUpdateBlinds(currentGame, repositoryGame)).start();
	}
}
