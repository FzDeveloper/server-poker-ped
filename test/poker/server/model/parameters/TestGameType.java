package poker.server.model.parameters;

import java.util.ArrayList;
import java.util.List;

import poker.server.model.game.parameters.Parameters;

public class TestGameType extends Parameters {
	
	private void setDefaultParams() {
		this.playerNumber = 6;
		this.speakTime = 30;

		this.buyIn = 10;
		this.buyInIncreasing = 2;
		this.multFactor = 2;

		this.setBlinds(10);
		this.setPotAsToken();

		this.buyInSplit = new ArrayList<Integer>();
		buyInSplit.add(50);
		buyInSplit.add(35);
		buyInSplit.add(15);

		this.setPotSplit(buyInSplit);
	}
	
	public TestGameType(int PlayerNb, int speakTime) {
		
		this.setDefaultParams();
		this.playerNumber = PlayerNb;
		this.speakTime = speakTime;
	}
	
	public TestGameType(int buyIn, int buyInIncreasing, int multFactor, int smallBlind) {
		
		this.setDefaultParams();
		this.buyIn = buyIn;
		this.buyInIncreasing = buyInIncreasing;
		this.multFactor = multFactor;
		this.setBlinds(smallBlind);
	}
	
	public TestGameType(List<Integer> potSplit) {
		
		this.setDefaultParams();
		this.setPotSplit(potSplit);
	}
}