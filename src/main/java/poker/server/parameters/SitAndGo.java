package poker.server.parameters;

import java.util.ArrayList;

public class SitAndGo extends Parameters {
	
	public SitAndGo () { }
	
	public void Init() {
		
		this.playerNumber = 6;
		this.speakTime = 30;
		
		this.buyIn = 10;
		this.buyInIncreasing = 2;
		
		this.setBlinds(10, 2);
		this.setPotAsToken();
		
		ArrayList<Integer> potSplit = new ArrayList<Integer>();
		potSplit.add(50); 
		potSplit.add(35);
		potSplit.add(15);
		
		this.setPotSplit(potSplit);
	}
}