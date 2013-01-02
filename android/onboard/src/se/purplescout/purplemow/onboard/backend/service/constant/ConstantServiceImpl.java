package se.purplescout.purplemow.onboard.backend.service.constant;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import se.purplescout.purplemow.core.bus.CoreBus;
import se.purplescout.purplemow.core.common.Constants;
import se.purplescout.purplemow.core.fsm.common.event.NewConstantsEvent;
import se.purplescout.purplemow.onboard.backend.dao.constant.ConstantDAO;
import se.purplescout.purplemow.onboard.db.entity.Constant;
import se.purplescout.purplemow.onboard.shared.constant.dto.ConstantsDTO;
import se.purplescout.purplemow.onboard.shared.constant.enums.ConstantEnum;

public class ConstantServiceImpl implements ConstantService {

	@Inject	ConstantDAO constantDAO;
	
	CoreBus coreBus = CoreBus.getInstance();
	@Override
	public Constants getConstants() {
		List<Constant> constants = constantDAO.listAll();
		Map<ConstantEnum, Integer> constantMap = new HashMap<ConstantEnum, Integer>();
		for (Constant constant : constants) {
			constantMap.put(constant.getName(), constant.getValue());
		}
		
		return new Constants(
				constantMap.get(ConstantEnum.FULL_SPEED), 
				constantMap.get(ConstantEnum.NO_SPEED), 
				constantMap.get(ConstantEnum.RANGE_LIMIT), 
				constantMap.get(ConstantEnum.BWF_LIMIT),
				constantMap.get(ConstantEnum.BATTERY_LOW), 
				constantMap.get(ConstantEnum.BATTERY_CHARGED), 
				constantMap.get(ConstantEnum.GO_HOME_HYSTERES),
				constantMap.get(ConstantEnum.GO_HOME_THRESHOLD_NEG_NARROW), 
				constantMap.get(ConstantEnum.GO_HOME_THRESHOLD_POS_NARROW),
				constantMap.get(ConstantEnum.GO_HOME_THRESHOLD_NEG_WIDE), 
				constantMap.get(ConstantEnum.GO_HOME_THRESHOLD_POS_WIDE),
				constantMap.get(ConstantEnum.GO_HOME_OFFSET));
	}

	
	@Override
	public ConstantsDTO getConstantsDTO() {
		Map<ConstantEnum, Integer> constantMap = constantDAO.getConstantMap();
		
		ConstantsDTO dto = new ConstantsDTO();
		dto.setFullSpeed(constantMap.get(ConstantEnum.FULL_SPEED)); 
		dto.setNoSpeed(constantMap.get(ConstantEnum.NO_SPEED)); 
		dto.setRangeLimit(constantMap.get(ConstantEnum.RANGE_LIMIT)); 
		dto.setBwfLimit(constantMap.get(ConstantEnum.BWF_LIMIT));
		dto.setBatteryLow(constantMap.get(ConstantEnum.BATTERY_LOW)); 
		dto.setBatteryCharged(constantMap.get(ConstantEnum.BATTERY_CHARGED)); 
		dto.setGoHomeHysteres(constantMap.get(ConstantEnum.GO_HOME_HYSTERES));
		dto.setGoHomeThresholdNegNarrow(constantMap.get(ConstantEnum.GO_HOME_THRESHOLD_NEG_NARROW)); 
		dto.setGoHomeThresholdPosNarrow(constantMap.get(ConstantEnum.GO_HOME_THRESHOLD_POS_NARROW));
		dto.setGoHomeThresholdNegWide(constantMap.get(ConstantEnum.GO_HOME_THRESHOLD_NEG_WIDE)); 
		dto.setGoHomeThresholdPosWide(constantMap.get(ConstantEnum.GO_HOME_THRESHOLD_POS_WIDE));
		dto.setGoHomeOffset(constantMap.get(ConstantEnum.GO_HOME_OFFSET));
		
		return dto;
	}

	@Override
	public void save(ConstantsDTO constantsDTO) {
		if (!constantsDTO.isChanged()) {
			return;
		}
		try {
			constantDAO.save(ConstantEnum.FULL_SPEED, constantsDTO.getFullSpeed());
			constantDAO.save(ConstantEnum.NO_SPEED, constantsDTO.getNoSpeed());
			constantDAO.save(ConstantEnum.RANGE_LIMIT, constantsDTO.getRangeLimit());
			constantDAO.save(ConstantEnum.BWF_LIMIT, constantsDTO.getBwfLimit());
			constantDAO.save(ConstantEnum.BATTERY_LOW, constantsDTO.getBatteryLow());
			constantDAO.save(ConstantEnum.BATTERY_CHARGED, constantsDTO.getBatteryCharged());
			constantDAO.save(ConstantEnum.GO_HOME_HYSTERES, constantsDTO.getGoHomeHysteres());
			constantDAO.save(ConstantEnum.GO_HOME_THRESHOLD_NEG_NARROW, constantsDTO.getGoHomeThresholdNegNarrow());
			constantDAO.save(ConstantEnum.GO_HOME_THRESHOLD_POS_NARROW, constantsDTO.getGoHomeThresholdPosNarrow());
			constantDAO.save(ConstantEnum.GO_HOME_THRESHOLD_NEG_WIDE, constantsDTO.getGoHomeThresholdNegWide());
			constantDAO.save(ConstantEnum.GO_HOME_THRESHOLD_POS_WIDE, constantsDTO.getGoHomeThresholdPosWide());
			constantDAO.save(ConstantEnum.GO_HOME_OFFSET, constantsDTO.getGoHomeOffset());
			
			coreBus.fireEvent(new NewConstantsEvent(getConstants()));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getFullSpeed() {
		try {
			return constantDAO.getFullSpeed();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}