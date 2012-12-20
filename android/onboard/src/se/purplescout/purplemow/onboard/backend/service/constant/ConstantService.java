package se.purplescout.purplemow.onboard.backend.service.constant;

import se.purplescout.purplemow.core.common.Constants;
import se.purplescout.purplemow.onboard.shared.constant.dto.ConstantsDTO;

public interface ConstantService {

	Constants getConstants();

	ConstantsDTO getConstantsDTO();

	void save(ConstantsDTO constantsDTO);

	int getFullSpeed();
}
