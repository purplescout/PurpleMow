package se.purplescout.purplemow.onboard.web.dispatcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.TypeReference;

import se.purplescout.purplemow.onboard.shared.schedule.dto.ScheduleEventDTO;
import se.purplescout.purplemow.onboard.web.WebServer.Request;
import se.purplescout.purplemow.onboard.web.service.RemoteService;
import se.purplescout.purplemow.onboard.web.service.RemoteService.Direction;
import se.purplescout.purplemow.onboard.web.service.ScheduleService;
import se.purplescout.purplemow.onboard.web.thirdparty.NanoHTTPD;
import se.purplescout.purplemow.onboard.web.thirdparty.NanoHTTPD.Response;

public class RpcDispatcher {

	private RemoteService remoteService;
	private ScheduleService scheduleService;

	public RpcDispatcher(RemoteService remoteService, ScheduleService scheduleService) {
		this.remoteService = remoteService;
		this.scheduleService = scheduleService;
	}

	public Response dispatch(Request request) {
		try {
			// RemoteService
			if (request.getUri().startsWith("/remote")) {
				String suffix = request.getUri().replaceFirst("/remote", "");
				if (suffix.equals("/incrementMovementSpeed")) {
					Direction direction = Direction.valueOf(request.getParms().getProperty("content").replace("\"", ""));
					remoteService.incrementMovmentSpeed(direction);
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "200 Ok");
				} else if (suffix.equals("/stopMovment")) {
					remoteService.stop();
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "200 Ok");
				} else if (suffix.equals("/incrementCutterSpeed")) {
					remoteService.incrementCutterSpeed();
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "200 Ok");
				} else if (suffix.equals("/decrementCutterSpeed")) {
					remoteService.decrementCutterSpeed();
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "200 Ok");
				}

			}

			// ScheduleSerivce
			if (request.getUri().startsWith("/schedule")) {
				String suffix = request.getUri().replaceFirst("/schedule", "");

				if (suffix.equals("/getDatesForWeek")) {
					String content = request.getParms().getProperty("content");
					Date date = deserialize(content, Date.class);
					List<Date> result = scheduleService.getDatesForWeek(date);
					InputStream response = serialize(result);

					return new Response(NanoHTTPD.HTTP_OK, "application/json", response);
				}
				if (suffix.equals("/getScheduleForWeek")) {
					String content = request.getParms().getProperty("content");
					Date date = deserialize(content, Date.class);
					List<ScheduleEventDTO> result = scheduleService.getScheduleForWeek(date);
					InputStream response = serialize(result);

					return new Response(NanoHTTPD.HTTP_OK, "application/json", response);
				}
				if (suffix.equals("/save")) {
					String content = request.getParms().getProperty("content");
					ObjectMapper mapper = new ObjectMapper();
					mapper.getDeserializationConfig().withDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz"));
					List<ScheduleEventDTO> dtos = mapper.readValue(content, new TypeReference<List<ScheduleEventDTO>>() { });
					scheduleService.save(dtos);
					return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "200 Ok");
				}
			}
		} catch (JsonParseException e) {
			return new Response(NanoHTTPD.HTTP_BADREQUEST, NanoHTTPD.MIME_PLAINTEXT, "400 Invalid argument");
		} catch (JsonMappingException e) {
			return new Response(NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.HTTP_INTERNALERROR, "500 Internal error");
		} catch (IOException e) {
			return new Response(NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.HTTP_INTERNALERROR, "500 Internal error");
		}

		return new Response(NanoHTTPD.HTTP_NOTFOUND, NanoHTTPD.MIME_PLAINTEXT, "404 Resource not found");
	}

	private <T> InputStream serialize(T e) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);

	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    mapper.writeValue(out, e);

	    return new ByteArrayInputStream(out.toByteArray());
	}

	private <T> T deserialize(String json, Class<? extends T> clazz) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.getDeserializationConfig().withDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz"));

	    return mapper.readValue(json, clazz);
	}
}
