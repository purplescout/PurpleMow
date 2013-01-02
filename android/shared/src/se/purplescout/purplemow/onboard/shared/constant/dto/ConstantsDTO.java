package se.purplescout.purplemow.onboard.shared.constant.dto;

public class ConstantsDTO {
	
	private int fullSpeed;
	private int noSpeed;
	private int rangeLimit;
	private int bwfLimit;
	private int batteryLow;
	private int batteryCharged;
	private int goHomeHysteres;
	private int goHomeThresholdNegNarrow;
	private int goHomeThresholdPosNarrow;
	private int goHomeThresholdNegWide;
	private int goHomeThresholdPosWide;
	private int goHomeOffset;
	private boolean changed;
	
	public int getFullSpeed() {
		return fullSpeed;
	}
	public void setFullSpeed(int fullSpeed) {
		this.fullSpeed = fullSpeed;
	}
	public int getNoSpeed() {
		return noSpeed;
	}
	public void setNoSpeed(int noSpeed) {
		this.noSpeed = noSpeed;
	}
	public int getRangeLimit() {
		return rangeLimit;
	}
	public void setRangeLimit(int rangeLimit) {
		this.rangeLimit = rangeLimit;
	}
	public int getBwfLimit() {
		return bwfLimit;
	}
	public void setBwfLimit(int bwfLimit) {
		this.bwfLimit = bwfLimit;
	}
	public int getBatteryLow() {
		return batteryLow;
	}
	public void setBatteryLow(int batteryLow) {
		this.batteryLow = batteryLow;
	}
	public int getBatteryCharged() {
		return batteryCharged;
	}
	public void setBatteryCharged(int batteryCharged) {
		this.batteryCharged = batteryCharged;
	}
	public int getGoHomeHysteres() {
		return goHomeHysteres;
	}
	public void setGoHomeHysteres(int goHomeHysteres) {
		this.goHomeHysteres = goHomeHysteres;
	}
	public int getGoHomeOffset() {
		return goHomeOffset;
	}
	public void setGoHomeOffset(int goHomeOffset) {
		this.goHomeOffset = goHomeOffset;
	}
	public boolean isChanged() {
		return changed;
	}
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	public int getGoHomeThresholdNegNarrow() {
		return goHomeThresholdNegNarrow;
	}
	public void setGoHomeThresholdNegNarrow(int goHomeThresholdNegNarrow) {
		this.goHomeThresholdNegNarrow = goHomeThresholdNegNarrow;
	}
	public int getGoHomeThresholdPosNarrow() {
		return goHomeThresholdPosNarrow;
	}
	public void setGoHomeThresholdPosNarrow(int goHomeThresholdPosNarrow) {
		this.goHomeThresholdPosNarrow = goHomeThresholdPosNarrow;
	}
	public int getGoHomeThresholdNegWide() {
		return goHomeThresholdNegWide;
	}
	public void setGoHomeThresholdNegWide(int goHomeThresholdNegWide) {
		this.goHomeThresholdNegWide = goHomeThresholdNegWide;
	}
	public int getGoHomeThresholdPosWide() {
		return goHomeThresholdPosWide;
	}
	public void setGoHomeThresholdPosWide(int goHomeThresholdPosWide) {
		this.goHomeThresholdPosWide = goHomeThresholdPosWide;
	}
}
