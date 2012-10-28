package se.purplescout.purplemow.webapp.client;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.user.client.Window;

public abstract class AbstractCallback<T> implements MethodCallback<T>{

	public abstract void onSuccess(T response);

	@Override
	public void onFailure(Method method, Throwable exception) {
		handleException(exception, method);
	}

	@Override
	public void onSuccess(Method method, T response) {
		onSuccess(response);
	}

	private void handleException(Throwable exception, Method method) {
		Window.alert("Failure in: " + method.toString() + "\n"+ exception.getMessage());
	}
}
