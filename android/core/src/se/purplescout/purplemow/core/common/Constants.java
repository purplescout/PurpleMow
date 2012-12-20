package se.purplescout.purplemow.core.common;

public class Constants {

	private final int fullSpeed;
	private final int noSpeed;
	private final int rangeLimit;
	private final int bwfLimit;
	private final int batteryLow;
	private final int batteryCharged;
	private final int goHomeHysteres;
	private final int goHomeThresholdNeg;
	private final int goHomeThresholdPos;
	private final int goHomeOffset;

	public Constants(int fullSpeed, int noSpeed, int rangeLimit, int bwfLimit, int batteryLow, int batteryCharged, int goHomeHysteres, int goHomeThresholdNeg,
			int goHomeThresholdPos, int goHomeOffset) {
		this.fullSpeed = fullSpeed;
		this.noSpeed = noSpeed;
		this.rangeLimit = rangeLimit;
		this.bwfLimit = bwfLimit;
		this.batteryLow = batteryLow;
		this.batteryCharged = batteryCharged;
		this.goHomeHysteres = goHomeHysteres;
		this.goHomeThresholdNeg = goHomeThresholdNeg;
		this.goHomeThresholdPos = goHomeThresholdPos;
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

	public int getGoHomeThresholdNeg() {
		return goHomeThresholdNeg;
	}

	public int getGoHomeThresholdPos() {
		return goHomeThresholdPos;
	}

	public int getGoHomeOffset() {
		return goHomeOffset;
	}
}
