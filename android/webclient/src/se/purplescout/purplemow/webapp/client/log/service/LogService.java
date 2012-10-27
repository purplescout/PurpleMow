package se.purplescout.purplemow.webapp.client.log.service;

import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import se.purplescout.purplemow.onboard.shared.log.dto.LogcatDTO;
import se.purplescout.purplemow.onboard.shared.log.dto.LogcatFilterDTO;

@Path("rpc/log")
public interface LogService extends RestService {

	@POST
	@Path("getLogcat")
	void getLogcat(List<LogcatFilterDTO> filters, MethodCallback<List<LogcatDTO>> methodCallback);
}