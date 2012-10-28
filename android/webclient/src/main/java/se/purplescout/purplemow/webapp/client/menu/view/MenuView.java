package se.purplescout.purplemow.webapp.client.menu.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;


import se.purplescout.purplemow.webapp.client.menu.presenter.MenuPresenter;

public class MenuView extends Composite implements MenuPresenter.View {
	public interface MenuViewUIBinder extends UiBinder<HTMLPanel, MenuView> {
	}

	public interface Style extends CssResource {

		String active();
	}

	public class MenuBar extends Widget {

		private UListElement list;

		public MenuBar() {
			list = Document.get().createULElement();
			setElement(list);
		}

		public void addMenu(String name, String historyToken, boolean isActive) {
			LIElement element = Document.get().createLIElement();
			Hyperlink link = new Hyperlink(name, historyToken);
			if (isActive) {
				link.addStyleName(style.active());
			}
			element.appendChild(link.getElement());
			list.appendChild(element);
		}
	}

	MenuViewUIBinder uiBinder = GWT.create(MenuViewUIBinder.class);
	MenuBar menuBar;

	@UiField HTMLPanel menu;
	@UiField Style style;

	public MenuView() {
		initWidget(uiBinder.createAndBindUi(this));
		menuBar = new MenuBar();
		menu.add(menuBar);
	}

	@Override
	public void addMenu(String name, String historyToken, boolean isActive) {
		menuBar.addMenu(name, historyToken, isActive);
	}
}
