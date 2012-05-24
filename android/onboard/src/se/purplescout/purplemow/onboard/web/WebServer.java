package se.purplescout.purplemow.onboard.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import se.purplescout.purplemow.core.fsm.AbstractFSM;
import se.purplescout.purplemow.core.fsm.event.MainFSMEvent;
import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent;
import se.purplescout.purplemow.onboard.web.thirdparty.NanoHTTPD;
import android.content.Context;
import android.util.Log;

public class WebServer extends NanoHTTPD {

	private Context context;
	private AbstractFSM<MainFSMEvent> mainFSM;
	private AbstractFSM<MotorFSMEvent> motorFSM;

	public WebServer(int port, Context context, AbstractFSM<MainFSMEvent> mainFSM, AbstractFSM<MotorFSMEvent> motorFSM) throws IOException {
		super(port, new File("dummy"));
		this.context = context;
		this.mainFSM = mainFSM;
		this.motorFSM = motorFSM;
	}

	@Override
	public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
		if (uri.equalsIgnoreCase("/command")) {
			if (parms.getProperty("cmd").equals("connect")) {
				mainFSM.queueEvent(new MainFSMEvent(MainFSMEvent.EventType.REMOTE_CONNECTED));
			} else if (parms.getProperty("cmd").equals("disconnect")) {
				mainFSM.queueEvent(new MainFSMEvent(MainFSMEvent.EventType.REMOTE_DISCONNECTED));
			} else if (parms.getProperty("cmd").equals("forward")) {
				motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.MOVE_FWD));
			} else if (parms.getProperty("cmd").equals("left")) {
				motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.TURN_LEFT));
			} else if (parms.getProperty("cmd").equals("stop")) {
				motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.STOP));
			} else if (parms.getProperty("cmd").equals("right")) {
				motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.TURN_RIGHT));
			} else if (parms.getProperty("cmd").equals("backward")) {
				motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.REVERSE));
			}

			return new Response(HTTP_OK, MIME_PLAINTEXT, "Moving " + parms.getProperty("cmd"));
		} else {
			return serveFile(uri, header, new File("dummy"), false);
		}
	}

	@Override
	public Response serveFile(String uri, Properties header, File homeDir, boolean allowDirectoryListing) {
		try {
			if (uri.equals("/") || uri.equals("/index.html")) {
				InputStream fileStream = context.getAssets().open("web/index.html");
				return new Response(HTTP_OK, MIME_HTML, fileStream);
			} else if (uri.equals("/js.js")) {
				InputStream fileStream = context.getAssets().open("web/js.js");
				return new Response(HTTP_OK, "application/javascript", fileStream);
			}

			return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "404 Not found");
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(this.getClass().getCanonicalName(), e.getMessage(), e);

			return new Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, e.getMessage());
		}
	}
}
