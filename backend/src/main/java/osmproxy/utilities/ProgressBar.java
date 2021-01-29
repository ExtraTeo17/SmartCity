package osmproxy.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressBar {

    private static final Logger logger = LoggerFactory.getLogger(ProgressBar.class);
	private static final int PERCENT_DISPLAY_STEP = 1;

	private int barCapacity;
	private int lastBarPosition = 0;

	public ProgressBar(int capacity) {
		barCapacity = capacity;
	}

	public void displayProgress(int barPosition) {
		int percentGrowth = barPosition * 100 / barCapacity;
		if (percentGrowth >= lastBarPosition) {
			lastBarPosition = percentGrowth + PERCENT_DISPLAY_STEP;
			logger.info("Operation progress: " + lastBarPosition + "%");
		}
	}
}
