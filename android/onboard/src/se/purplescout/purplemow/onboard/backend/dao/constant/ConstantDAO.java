package se.purplescout.purplemow.onboard.backend.dao.constant;

import java.sql.SQLException;
import java.util.Map;

import se.purplescout.purplemow.onboard.backend.dao.GenericDAO;
import se.purplescout.purplemow.onboard.db.entity.Constant;
import se.purplescout.purplemow.onboard.shared.constant.enums.ConstantEnum;

public interface ConstantDAO extends GenericDAO<Constant, Integer> {

	int getFullSpeed() throws SQLException;

	Map<ConstantEnum, Integer> getConstantMap();

	void save(ConstantEnum constantEnum, int value) throws SQLException;
}
