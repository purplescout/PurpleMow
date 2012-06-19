package se.purplescout.purplemow.webapp.client.remote.service;

import com.google.gwt.user.client.AsyncProxy;
import com.google.gwt.user.client.AsyncProxy.ConcreteType;

import se.purplescout.purplemow.webapp.client.remote.presenter.RemotePresenter;

@ConcreteType(RemoteService.class)
public interface RemoteServiceProxy extends AsyncProxy<RemotePresenter.Service>, RemotePresenter.Service {
}
