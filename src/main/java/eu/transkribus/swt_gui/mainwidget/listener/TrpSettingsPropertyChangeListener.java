package eu.transkribus.swt_gui.mainwidget.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_canvas.portal.PortalWidget.Docking;
import eu.transkribus.swt_canvas.portal.PortalWidget.Position;
import eu.transkribus.swt_gui.canvas.TrpSWTCanvas;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;

public class TrpSettingsPropertyChangeListener implements PropertyChangeListener {
	private final static Logger logger = LoggerFactory.getLogger(TrpSettingsPropertyChangeListener.class);
	
	TrpMainWidget mainWidget;
	TrpMainWidgetView ui;
	TrpSWTCanvas canvas;
		
	public TrpSettingsPropertyChangeListener(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		this.ui = mainWidget.getUi();
		this.canvas = mainWidget.getCanvas();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		logger.debug(evt.getPropertyName() + " property changed, new value: " + evt.getNewValue());
		String pn = evt.getPropertyName();

		if (pn.equals(TrpSettings.AUTOCOMPLETE_PROPERTY)) {
			mainWidget.enableAutocomplete();
		} else if (pn.equals(TrpSettings.SHOW_LINE_EDITOR_PROPERTY)) {
			canvas.getLineEditor().updateEditor();
		} else if (TrpSettings.isSegmentationVisibilityProperty(pn)){
			mainWidget.getScene().updateSegmentationViewSettings();	
		} else if (pn.equals(TrpSettings.ENABLE_INDEXED_STYLES)) {
			logger.debug("indexed styles visibility toggled: "+evt.getNewValue());
			ui.getSelectedTranscriptionWidget().redrawText(true);
			mainWidget.updatePageRelatedMetadata();
		} else if (pn.equals(TrpSettings.RENDER_BLACKENINGS_PROPERTY)) {
			canvas.redraw();
		}
		else if (pn.equals(TrpSettings.LEFT_VIEW_DOCKING_STATE_PROPERTY)) {
			ui.getPortalWidget().setWidgetDockingType(Position.LEFT, (Docking) evt.getNewValue());
		}
		else if (pn.equals(TrpSettings.RIGHT_VIEW_DOCKING_STATE_PROPERTY)) {
			ui.getPortalWidget().setWidgetDockingType(Position.RIGHT, (Docking) evt.getNewValue());
		}
		else if (pn.equals(TrpSettings.BOTTOM_VIEW_DOCKING_STATE_PROPERTY)) {
			ui.getPortalWidget().setWidgetDockingType(Position.BOTTOM, (Docking) evt.getNewValue());
		}
		
		
		
		
		if (TrpSettings.isColorProperty(pn)) {
			logger.debug("color info changed - updating!");
			canvas.updateShapeColors();
		}
		
		canvas.redraw();
	}
}
