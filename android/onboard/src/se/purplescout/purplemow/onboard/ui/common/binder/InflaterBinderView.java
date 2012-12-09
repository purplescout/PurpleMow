package se.purplescout.purplemow.onboard.ui.common.binder;

import java.lang.reflect.Field;

import se.purplescout.purplemow.onboard.ui.common.binder.annotation.ContentView;
import se.purplescout.purplemow.onboard.ui.common.binder.annotation.UiField;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class InflaterBinderView {

	protected View parentView;

	public InflaterBinderView(LayoutInflater layoutInflater, View parentView) {
		ContentView contentView = getClass().getAnnotation(ContentView.class);
		if (contentView == null) {
			throw new IllegalStateException(String.format("A @ContentType must be set on the class %s", this.getClass().getSimpleName()));
		}
		if (parentView == null) {
			parentView = layoutInflater.inflate(contentView.value(), null);
		}
		this.parentView = parentView;

		for (Field field : getClass().getDeclaredFields()) {
			UiField injectView = field.getAnnotation(UiField.class);
			if (injectView == null) {
				continue;
			}
			int viewId = injectView.value();
			View view = parentView.findViewById(viewId);
			if (view == null) {
				throw new IllegalStateException(String.format("View %d could not be injected in %s because it is not present in the provided content view",
						viewId, this.getClass().getSimpleName()));
			}
			try {
				field.setAccessible(true);
				field.set(this, view);
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException(String.format("Field %s in class %s could not be assigned an object of type %s", field.getName(),
						getClass().getSimpleName(), view.getClass().getSimpleName()));
			} catch (IllegalAccessException e) {
				Log.e(getClass().getSimpleName(), e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}
	}
}
