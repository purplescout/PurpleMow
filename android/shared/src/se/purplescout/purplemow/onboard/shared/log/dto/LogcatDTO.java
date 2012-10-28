package se.purplescout.purplemow.onboard.shared.log.dto;


public class LogcatDTO {

	private String priorityConstant;
	private String entry;

	public String getPriorityConstant() {
		return priorityConstant;
	}

	public void setPriorityConstant(String priorityConstant) {
		this.priorityConstant = priorityConstant;
	}

	public String getEntry() {
		return entry;
	}

	public void setEntry(String entry) {
		this.entry = entry;
	}
}
