package se.purplescout.purplemow.onboard.backend.dao.constant;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import se.purplescout.purplemow.onboard.backend.dao.GenericDAOImpl;
import se.purplescout.purplemow.onboard.db.entity.Constant;
import se.purplescout.purplemow.onboard.db.entity.Constant_;
import se.purplescout.purplemow.onboard.shared.constant.enums.ConstantEnum;

import com.google.inject.Inject;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;

public class ConstantDAOImpl extends GenericDAOImpl<Constant, Integer> implements ConstantDAO {

	private RuntimeExceptionDao<Constant, Integer> dao;

	@Inject
	public ConstantDAOImpl(ConnectionSource connectionSource) throws SQLException {
		this.dao = RuntimeExceptionDao.<Constant, Integer>createDao(connectionSource, Constant.class);
	}

	@Override
	protected RuntimeExceptionDao<Constant, Integer> getDAO() {
		return dao;
	}

	@Override
	public int getFullSpeed() throws SQLException {
		QueryBuilder<Constant, Integer> qb = dao.queryBuilder();
		qb.where().eq(Constant_.NAME, ConstantEnum.FULL_SPEED);
		PreparedQuery<Constant> preparedQuery = qb.prepare();

		return dao.queryForFirst(preparedQuery).getValue();
	}

	@Override
	public Map<ConstantEnum, Integer> getConstantMap() {
		Map<ConstantEnum, Integer> constantMap = new HashMap<ConstantEnum, Integer>();
		for (Constant constant : listAll()) {
			constantMap.put(constant.getName(), constant.getValue());
		}

		return constantMap;
	}

	@Override
	public void save(ConstantEnum constantEnum, int value) throws SQLException {
		UpdateBuilder<Constant, Integer> ub = dao.updateBuilder();
		ub.updateColumnValue(Constant_.VALUE, value);
		ub.where().eq(Constant_.NAME, constantEnum);
		ub.update();
	}
}
