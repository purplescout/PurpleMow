package se.purplescout.purplemow.webapp.client.log.presenter;

import java.util.ArrayList;
import java.util.List;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import se.purplescout.purplemow.onboard.shared.log.dto.LogcatDTO;
import se.purplescout.purplemow.onboard.shared.log.dto.LogcatFilterDTO;
import se.purplescout.purplemow.webapp.client.log.presenter.LogcatPresenter.FilterParser.FilterParserException;
import se.purplescout.purplemow.webapp.client.log.service.LogService;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;

public class LogcatPresenter extends AbstractActivity {
	
	private static String DEFAULT_FILTERS = "AndroidRuntime:E MainService:I MainFSM:I MotorFSM:I SensorReader:I WebServer:I RPCDispatcher:I";
	
	public interface View extends IsWidget {

		void setLogcat(List<LogcatDTO> logcatDTO);

		TextBox getFilters();

		void showParseError(boolean b);
	}
	
	static class FilterParser {
		
		static class FilterParserException extends Exception {

			private static final long serialVersionUID = -2045604641068716716L;
		}
		
		static List<String> validPriorityConstants = new ArrayList<String>();
		static {
			validPriorityConstants.add("V");
			validPriorityConstants.add("I");
			validPriorityConstants.add("D");
			validPriorityConstants.add("W");
			validPriorityConstants.add("E");
		}
		
		public static List<LogcatFilterDTO> parseFilters(String filters) throws FilterParserException {
			String[] textFilters = filters.split(" ");
			List<LogcatFilterDTO> result = new ArrayList<LogcatFilterDTO>();
			if (filters.isEmpty()) {
				return result;
			}
			for (String textFilter : textFilters) {
				result.add(parseFilter(textFilter));	
			}
			
			return result;
		}

		private static LogcatFilterDTO parseFilter(String textFilter) throws FilterParserException {
			String[] text = textFilter.split(":");
			if (text.length == 2) {
				if (validPriorityConstants.contains(text[1])) {
					LogcatFilterDTO filterDTO = new LogcatFilterDTO();
					filterDTO.setPriorityConstant(text[1]);
					
					RegExp regexp = RegExp.compile("[a-zA-Z_$]*");
					MatchResult tag = regexp.exec(text[0]);
					if (tag != null) {
						filterDTO.setTag(text[0]);
						return filterDTO;
					}
				}
			}
			
			throw new FilterParserException();
		}
	}

	View view;
	LogService srv;

	public LogcatPresenter(View view, LogService srv) {
		this.view = view;
		this.srv = srv;

		bind();
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(view);
	}

	private void bind() {
		view.getFilters().addKeyPressHandler(new KeyPressHandler() {
			
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getCharCode() == KeyCodes.KEY_ENTER) {
					getLogcat();
				}
			}
		});
		
		view.getFilters().setText(DEFAULT_FILTERS);
		getLogcat();
	}

	private void getLogcat() {		
		try {
			List<LogcatFilterDTO> filters = FilterParser.parseFilters((view.getFilters().getText()));
			view.showParseError(false);
			srv.getLogcat(filters, new MethodCallback<List<LogcatDTO>>() {
				
				@Override
				public void onSuccess(Method method, List<LogcatDTO> response) {
					view.setLogcat(response);
				}
				
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert(exception.getMessage());
				}
			});
		} catch (FilterParserException e) {
			view.showParseError(true);
		}
	}
}
