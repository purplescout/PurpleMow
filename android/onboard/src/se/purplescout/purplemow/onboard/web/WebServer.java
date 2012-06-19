package se.purplescout.purplemow.onboard.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import se.purplescout.purplemow.onboard.RemoteController;
import se.purplescout.purplemow.onboard.web.thirdparty.NanoHTTPD;
import android.content.Context;
import android.util.Log;

public class WebServer extends NanoHTTPD {

	private Context context;
	private RemoteController remoteController;

	public WebServer(int port, Context context, RemoteController remoteController) throws IOException {
		super(port, new File("dummy"));
		this.context = context;
		this.remoteController = remoteController;
	}

	@Override
	public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
		
		if (uri.equalsIgnoreCase("/command")) {
			Log.i(this.getClass().getCanonicalName(), "Received remote procedure call");
			long t = System.currentTimeMillis();
			try {
				if (parms.getProperty("cmd").equals("forward")) {
					remoteController.incrementForward();
				} else if (parms.getProperty("cmd").equals("left")) {
					remoteController.incrementLeft();
				} else if (parms.getProperty("cmd").equals("stop")) {
					remoteController.stop();
				} else if (parms.getProperty("cmd").equals("right")) {
					remoteController.incrementRight();
				} else if (parms.getProperty("cmd").equals("backward")) {
					remoteController.incrementBackward();
				} else if (parms.getProperty("cmd").equals("cutter_on")) {
					remoteController.incrementCutter();
				} else if (parms.getProperty("cmd").equals("cutter_off")) {
					remoteController.decrementCutter();
				}
			} catch (NumberFormatException e) {
				return new Response(HTTP_BADREQUEST, MIME_PLAINTEXT, e.getMessage());
			} catch (NullPointerException e) {
				return new Response(HTTP_BADREQUEST, MIME_PLAINTEXT, e.getMessage());
			}
			Log.i(this.getClass().getCanonicalName(), "Call served in " + (System.currentTimeMillis() - t) + "ms");
			return new Response(HTTP_OK, MIME_PLAINTEXT, "Moving " + parms.getProperty("cmd"));
		} else if (uri.startsWith("/logs")) {
			CharSequence relPath = uri.subSequence(5, uri.length());
			if (relPath.equals("/motorFSMEventLog.csv")) {
				return new Response(HTTP_OK, MIME_PLAINTEXT, remoteController.getMotorFSMEventLog());
			} else {
				return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "404 File not found");
			}
		} else {
			return serveFile(uri, header, new File("dummy"), false);
		}
	}

	@Override
	public Response serveFile(String uri, Properties header, File homeDir, boolean allowDirectoryListing) {
		String root = "web";
		if (uri.equals("/")) {
			uri = "/index.html";
		}
		try {
			String mimeType = MIME_PLAINTEXT;
			if (uri.endsWith(".html")) {
				mimeType = MIME_HTML;
			} else if (uri.endsWith(".js")) {
				mimeType = "application/javascript";
			}

			InputStream fileStream = context.getAssets().open(root + uri);
			return new Response(HTTP_OK, mimeType, fileStream);
		} catch (FileNotFoundException e) {
			Log.i(this.getClass().getCanonicalName(), e.getMessage());
			return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "404 File not found");
		} catch (IOException e) {
			Log.e(this.getClass().getCanonicalName(), e.getMessage(), e);
			return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, e.getMessage());
		}
	}
}
