package se.purpleout.purplemow;

public interface IRobot {
	public boolean isConnected();
	public void setSensorListener(ISensorListener listener);
	public void moveForward();
	public void moveBackward();
	public void turnLeft();
	public void turnRight();
	public void stop();
	public void startCutter();
	public void stopCutter();
	public void readSensor();
}
