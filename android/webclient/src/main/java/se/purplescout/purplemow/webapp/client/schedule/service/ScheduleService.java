package se.purplescout.purplemow.webapp.client.schedule.service;

import java.util.Date;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import se.purplescout.purplemow.onboard.shared.schedule.dto.ScheduleEventDTO;
import se.purplescout.purplemow.webapp.client.AbstractCallback;

@Path("rpc/schedule")
public interface ScheduleService extends RestService {

	@POST
	@Path("getScheduleForWeek")
	public void getScheduleForWeek(Date date, AbstractCallback<List<ScheduleEventDTO>> callback);

	@POST
	@Path("getDatesForWeek")
	void getDatesForWeek(Date date, AbstractCallback<List<Date>> callback);

	@POST
	@Path("save")
	public void save(List<ScheduleEventDTO> changed, MethodCallback<Void> methodCallback);
}
