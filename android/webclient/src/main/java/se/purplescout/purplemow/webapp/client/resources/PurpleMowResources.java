package se.purplescout.purplemow.webapp.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

public interface PurpleMowResources extends ClientBundle {

    public static final PurpleMowResources RESOURCE = GWT.create(PurpleMowResources.class);

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

    @Source("content_repeat.jpg")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource contentRepeat();
    
    @Source("purple_header_bg_graphic.gif")
    ImageResource purpleHeaderBgGraphic();
    
    @Source("purple_header_bg.gif")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal, height = -1)
    ImageResource purpleHeaderBg();
    
    public interface Style extends CssResource {
    	
    	String headerWrap();
    	
    	String headerBg();
    	
    	String header();
    	
    	String logo();
    	
    	String contentWrapper();
    	
    	String content();
    	
    	String box();
    }

	@Source("purplemow.css")
	Style style();
}
