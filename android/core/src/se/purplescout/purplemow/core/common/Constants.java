package se.purplescout.purplemow.core.common;

public class Constants {

	private final int fullSpeed;
	private final int noSpeed;
	private final int rangeLimit;
	private final int bwfLimit;
	private final int batteryLow;
	private final int batteryCharged;
	private final int goHomeHysteres;


	private final int goHomeThresholdNegNarrow;
	private final int goHomeThresholdPosNarrow;
	private final int goHomeThresholdNegWide;
	private final int goHomeThresholdPosWide;
	private final int goHomeOffset;


	public Constants(int fullSpeed, int noSpeed, int rangeLimit, int bwfLimit,
			int batteryLow, int batteryCharged, int goHomeHysteres,
			int goHomeThresholdNegNarrow, int goHomeThresholdPosNarrow,
			int goHomeThresholdNegWide, int goHomeThresholdPosWide,
			int goHomeOffset) {
		this.fullSpeed = fullSpeed;
		this.noSpeed = noSpeed;
		this.rangeLimit = rangeLimit;
		this.bwfLimit = bwfLimit;
		this.batteryLow = batteryLow;
		this.batteryCharged = batteryCharged;
		this.goHomeHysteres = goHomeHysteres;
		this.goHomeThresholdNegNarrow = goHomeThresholdNegNarrow;
		this.goHomeThresholdPosNarrow = goHomeThresholdPosNarrow;
		this.goHomeThresholdNegWide = goHomeThresholdNegWide;
		this.goHomeThresholdPosWide = goHomeThresholdPosWide;
		this.goHomeOffset = goHomeOffset;
	}



	public int getFullSpeed() {
		return fullSpeed;
	}

	public int getNoSpeed() {
		return noSpeed;
	}

	public int getRangeLimit() {
		return rangeLimit;
	}

	public int getBwfLimit() {
		return bwfLimit;
	}

	public int getBatteryLow() {
		return batteryLow;
	}

	public int getBatteryCharged() {
		return batteryCharged;
	}

	public int getGoHomeHysteres() {
		return goHomeHysteres;
	}

	public int getGoHomeOffset() {
		return goHomeOffset;
	}

	public int getGoHomeThresholdNegNarrow() {
		return goHomeThresholdNegNarrow;
	}

	public int getGoHomeThresholdPosNarrow() {
		return goHomeThresholdPosNarrow;
	}

	public int getGoHomeThresholdNegWide() {
		return goHomeThresholdNegWide;
	}

	public int getGoHomeThresholdPosWide() {
		return goHomeThresholdPosWide;
	}
}
