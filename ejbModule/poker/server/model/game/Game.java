package poker.server.model.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import poker.server.model.game.card.Card;
import poker.server.model.game.card.Cards;
import poker.server.model.game.parameters.Parameters;
import poker.server.model.game.parameters.SitAndGo;
import poker.server.model.player.Player;

@Entity
public class Game implements Serializable, Observer {

	private static final long serialVersionUID = 2687924657560495636L;

	@Id
	@GeneratedValue
	private int id;

	private transient Parameters gameType;

	private transient Cards deck;

	private transient List<Card> flippedCards;

	private ArrayList<Player> players;
	private int currentPlayer = 0;

	private int dealer = 0;
	private int bigBlindPlayer = 1;
	private int smallBlindPlayer = 2;

	private int smallBlind;
	private int bigBlind;

	private int totalPot = 0;
	private int currentPot = 0;
	private int currentBet = 0;

	private int currentRound = 1;

	// to be used...
	public static final int FLOP = 1;
	public static final int TOURNANT = 2;
	public static final int RIVER = 3;

	private boolean Started;

	// CONSTRUCTOR
	protected Game() {
		gameType = new SitAndGo();
		buildGame();
	}

	protected Game(Parameters gameT) {
		gameType = gameT;
		buildGame();
	}

	private void buildGame() {
		deck = new Cards();
		flippedCards = new ArrayList<Card>();
		players = new ArrayList<Player>();
		smallBlind = gameType.getSmallBlind();
		bigBlind = gameType.getBigBlind();
		setStarted(false);
		Event.buildEvents();
	}

	// GETTERS / SETTERS
	public int getId() {
		return id;
	}

	public Parameters getGameType() {
		return gameType;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	public int getCurrentPlayer() {
		return currentPlayer;
	}

	public int getDealer() {
		return dealer;
	}

	public int getBigBlindPlayer() {
		return bigBlindPlayer;
	}

	public int getSmallBlindPlayer() {
		return smallBlindPlayer;
	}

	public int getTotalPot() {
		return totalPot;
	}

	public int getCurrentPot() {
		return currentPot;
	}

	public int getCurrentBet() {
		return currentBet;
	}

	public void setCurrentBet(int currentBet) {
		this.currentBet = currentBet;
	}
	
	public void setCurrentPot(int currentPot) {
		this.currentPot = currentPot;
	}
	
	public void setTotalPot(int totalPot) {
		this.totalPot = totalPot;
	}
	
	public int getCurrentRound() {
		return currentRound;
	}

	public List<Card> getFlipedCards() {
		return flippedCards;
	}

	public void resetPlayerRoles() {
		for (Player p : this.players) {
			p.setAsRegular();
		}
	}
	
	public void nextPlayer() {

		if (currentPlayer == (this.players.size() - 1))
			currentPlayer = 0;
		else
			currentPlayer++;
	}

	public void nextDealer() {

		if (this.dealer == (this.players.size() - 1))
			this.dealer = 0;
		else
			this.dealer++;

		Player dealer = this.players.get(this.dealer);
		dealer.setAsDealer();
		
		Event.addEvent("THE DEALER IS : " + dealer.getName());
	}

	public void nextBigBlind() {

		if (this.bigBlindPlayer == (this.players.size() - 1))
			this.bigBlindPlayer = 0;
		else
			this.bigBlindPlayer++;

		Player bigBlind = this.players.get(this.bigBlindPlayer);
		bigBlind.setAsBigBlind();
		
		Event.addEvent("THE BIG BLIND IS : " + bigBlind.getName());
	}

	public void nextSmallBlind() {

		if (smallBlindPlayer == (this.players.size() - 1))
			smallBlindPlayer = 0;
		else
			smallBlindPlayer++;

		Player smallBlind = this.players.get(this.smallBlindPlayer);
		smallBlind.setAsSmallBlind();
		
		Event.addEvent("THE SMALL BLIND IS : " + smallBlind.getName());
	}
	
	public Cards getDeck() {
		return deck;
	}
	
	public int getSmallBlind() {
		return smallBlind;
	}

	public int getBigBlind() {
		return bigBlind;
	}

	public boolean isStarted() {
		return Started;
	}

	public void setStarted(boolean started) {
		Started = started;
	}
	
	
	// ROUND MANAGEMENT
	public Card flipCard() {
		Card card = deck.getNextCard();
		flippedCards.add(card);
		return card;
	}

	public void flop() {

		String eventFlop = "FLOP : ";
		Card card;
		deck.burnCard();

		for (int i = 0; i < 3; i++) {
			card = flipCard();
			eventFlop += card.getValue() + " " + card.getSuit() + " , ";
		}
		updateTotalPot();
		resetCurrentPot();
		Event.addEvent(eventFlop);
	}

	public void tournant() {

		deck.burnCard();
		Card card = flipCard();
		updateTotalPot();
		resetCurrentPot();
		Event.addEvent("TOURNANT : " + card.getValue() + " " + card.getSuit());
	}

	public void river() {

		deck.burnCard();
		Card card = flipCard();
		updateTotalPot();
		resetCurrentPot();
		Event.addEvent("RIVER : " + card.getValue() + " " + card.getSuit());
	}

	// BLIND / BET / POT MANAGEMENT
	public void updateBlind() {

		int blindMultFactor = gameType.getMultFactor();
		smallBlind = smallBlind * blindMultFactor;
		bigBlind = smallBlind * 2;

		Event.addEvent("SMALL BLIND = " + smallBlind + " , BIG BLIND = "
				+ bigBlind);
	}

	public void resetCurrentPot() {

		this.currentPot = 0;
		this.currentBet = 0;
		
		for (Player player : this.players) {
			player.setCurrentBet(0);
		}
		
		Event.addEvent("RESET BET");
	}

	public void updateCurrentPot(int quantity) {
		this.currentPot += quantity;
		Event.addEvent("BETS = " + currentPot);
	}

	public void updateTotalPot() {
		this.totalPot += this.currentPot;
		Event.addEvent("UPDATE POT, POT = " + totalPot);
	}

	public void updateCurrentBet(int quantity) {
		currentBet += quantity;
		Event.addEvent("CURRENT BET = " + currentBet);
	}
	
	
	// OTHER
	public void dealCards() {

		Card card;
		for (int i = 0; i < 2; i++) {
			for (Player player : players) {
				card = deck.getNextCard();
				player.currentHand.addCard(card);
			}
		}
		Event.addEvent("DEAL CARDS FOR PLAYERS");
	}

	public void start() {
		System.out.println("start() : TODO");
		Event.addEvent("START GAME");
	}

	public void add(Player player) {
		players.add(player);
	}	

	@Override
	public void update(Observable o, Object arg) {
		updateBlind();
	}
}
