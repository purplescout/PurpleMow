package se.purplescout.purplemow.webapp.client.resource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface Resources extends ClientBundle {

    public static final Resources RESOURCE = GWT.create(Resources.class);

    interface Style extends CssResource {
    }
    
    @Source("strip-arrow_up.png")
    ImageResource stripArrowUp();

    @Source("strip-arrow_down.png")
    ImageResource stripArrowDown();
    
    @Source("strip-arrow_left.png")
    ImageResource stripArrowLeft();
    
    @Source("strip-arrow_right.png")
    ImageResource stripArrowRight();
    
    @Source("strip-arrow_stop.png")
    ImageResource stripArrowStop();
    
    @Source("strip-arrow_plus.png")
    ImageResource stripArrowPlus();
    
    @Source("strip-arrow_minus.png")
    ImageResource stripArrowMinus();
}

