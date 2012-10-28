package se.purplescout.purplemow.onboard.shared.log.dto;

public class LogcatFilterDTO {

	private String tag;
	private String priorityConstant;

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getPriorityConstant() {
		return priorityConstant;
	}

	public void setPriorityConstant(String priorityConstant) {
		this.priorityConstant = priorityConstant;
	}
}
