package poker.server.model.game.timer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import poker.server.model.exception.GameException;

public class Stopwatch {

	private static final String FORBIDDEN_DURATION = "Can't launch chrono other than 30 or 180 seconds";

	private static int delay = 1000;
	private int totalTime;
	private Timer timer;

	public static Stopwatch chrono(int duration) {

		Stopwatch stopwatch = new Stopwatch(duration);
		stopwatch.start();
		return stopwatch;
	}

	ActionListener timerTask = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e1) {

			if (timer.isRunning())
				totalTime--;

			if (totalTime == 0) {
				timer.stop();
				return;
			}
		}
	};

	public Stopwatch(int duration) {

		if (duration != 10 && duration != 180)
			throw new GameException(FORBIDDEN_DURATION);

		totalTime = duration;
		this.timer = new Timer(delay, this.timerTask);
	}

	public void start() {
		this.timer.start();
	}

	public void stop() {
		this.timer.stop();
	}

	public boolean isRunning() {
		return this.timer.isRunning();
	}

	public int getTotalTime() {
		return totalTime;
	}
}