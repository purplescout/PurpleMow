package se.purplescout.purplemow.onboard.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import se.purplescout.purplemow.onboard.shared.constant.enums.ConstantEnum;

import static se.purplescout.purplemow.onboard.db.entity.Constant_.ID;
import static se.purplescout.purplemow.onboard.db.entity.Constant_.NAME;
import static se.purplescout.purplemow.onboard.db.entity.Constant_.VALUE;

@Entity(name = "constant")
public class Constant {

	@Id
	@Column(name = ID)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = NAME, nullable = false)
	@Enumerated(EnumType.STRING)
	ConstantEnum name;

	@Column(name = VALUE, nullable = false)
	int value;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ConstantEnum getName() {
		return name;
	}

	public void setName(ConstantEnum name) {
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}
