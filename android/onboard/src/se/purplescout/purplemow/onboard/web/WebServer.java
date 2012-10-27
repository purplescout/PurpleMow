package se.purplescout.purplemow.onboard.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import se.purplescout.purplemow.onboard.web.dispatcher.RpcDispatcher;
import se.purplescout.purplemow.onboard.web.thirdparty.NanoHTTPD;
import android.content.Context;
import android.util.Log;

public class WebServer extends NanoHTTPD {

	private static final String RPC_ROOT = "/purplemow/rpc";

	public static class Request {
		private final String uri;
		private final String method;
		private final Properties header;
		private final Properties parms;
		private final Properties files;

		public Request(String uri, String method, Properties header, Properties parms, Properties files) {
			this.uri = uri;
			this.method = method;
			this.header = header;
			this.parms = parms;
			this.files = files;
		}

		public String getUri() {
			return uri;
		}

		public String getMethod() {
			return method;
		}

		public Properties getHeader() {
			return header;
		}

		public Properties getParms() {
			return parms;
		}

		public Properties getFiles() {
			return files;
		}
	}

	private Context context;
	private RpcDispatcher dispatcher;

	public WebServer(int port, Context context, RpcDispatcher dispatcher) throws IOException {
		super(port, new File("dummy"));
		this.context = context;
		this.dispatcher = dispatcher;
	}

	@Override
	public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
		if (uri.startsWith(RPC_ROOT)) {
			Log.i(this.getClass().getName(), "Received remote procedure call: " + uri);
			return dispatcher.dispatch(new Request(uri.replaceFirst(RPC_ROOT, ""), method, header, parms, files));
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
			Log.i(this.getClass().getName(), e.getMessage());
			return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "404 File not found");
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.getMessage(), e);
			return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, e.getMessage());
		}
	}
}
