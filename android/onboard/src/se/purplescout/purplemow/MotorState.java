package se.purpleout.purplemow;

public class MotorState {
	// -1 back, 0 står still, 1 kör framåt
	private int motorRight;
	private int motorLeft;
	public int getMotorLeft() {
		return motorLeft;
	}
	public void setMotorLeft(int motorLeft) {
		this.motorLeft = motorLeft;
	}
	public int getMotorRight() {
		return motorRight;
	}
	public void setMotorRight(int motorRight) {
		this.motorRight = motorRight;
	}
}
