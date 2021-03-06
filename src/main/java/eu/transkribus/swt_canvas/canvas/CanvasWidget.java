package eu.transkribus.swt_canvas.canvas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_canvas.canvas.editing.UndoStack;
import eu.transkribus.swt_canvas.canvas.listener.CanvasSceneListener;
import eu.transkribus.swt_canvas.canvas.listener.CanvasToolBarSelectionListener;
import eu.transkribus.swt_canvas.util.SWTUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolItem;


/**
 * Widget class that wraps an SWTCanvas and a corresponding toolbar into a Composite.
 */
public class CanvasWidget extends Composite {
	static Logger logger = LoggerFactory.getLogger(CanvasWidget.class);
	
	protected SWTCanvas canvas;
	protected CanvasToolBar toolBar;
	protected CanvasToolBarSelectionListener canvasToolBarSelectionListener;

	/**
	 * @wbp.parser.constructor
	 */
	public CanvasWidget(Composite parent, int style) {
		super(parent, style);
								
		init(null, null);
	}
	
	/**
	 * Wraps the DeaSWTCanvas widget into a widget containing a toolbar for the most common operations such as scaling, rotation, translation etc.
	 */
	public CanvasWidget(Composite parent, int style, SWTCanvas canvas) {
		super(parent, style);

		init(canvas, null);
	}

	/**
	 * Wraps the DeaSWTCanvas widget into a widget containing a toolbar for the most common operations such as scaling, rotation, translation etc.
	 */
	public CanvasWidget(Composite parent, int style, SWTCanvas canvas, CanvasToolBar toolBar) {
		super(parent, style);
					
		init(canvas, toolBar);
	}
	
	protected void init(SWTCanvas canvas, CanvasToolBar toolBar) {
		setLayout(new GridLayout(1, false));
		
		if (this.canvas != null && !this.canvas.isDisposed())
			this.canvas.dispose();
		
		if (canvas != null) {
			this.canvas = canvas;
		}
		else {
			this.canvas = new SWTCanvas(SWTUtil.dummyShell, SWT.NONE);
		}
		this.canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		if (this.toolBar != null && !this.toolBar.isDisposed())
			this.toolBar.dispose();
		
		if (toolBar!=null) {
			this.toolBar = toolBar;
			this.toolBar.setParent(this);
		} else {
			this.toolBar = new CanvasToolBar(this, SWT.FLAT | SWT.WRAP);
		}
		this.toolBar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
		
		this.canvas.setParent(this);
		
	}
	
	protected void addListener() {
		// selection listener for toolbar:
		//canvasToolBarSelectionListener = new CanvasToolBarSelectionListener(toolBar, canvas);
		//toolBar.addAddButtonsSelectionListener(canvasToolBarSelectionListener);
		// selection listener on canvas:
		canvas.getScene().addCanvasSceneListener(new CanvasSceneListener() {
			@Override
			public void onSelectionChanged(SceneEvent e) {
				toolBar.updateButtonVisibility();
			}
		});
		// update buttons on changes in canvas settings:
		canvas.getSettings().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				toolBar.updateButtonVisibility();
				canvas.redraw();
			}
		});
		// update undo button on changes in undo stack:
		canvas.getUndoStack().addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				if (arg == UndoStack.AFTER_UNDO || arg == UndoStack.AFTER_ADD_OP) {
					logger.trace("updating undo button after undo or add op!");
					updateUndoButton();
				}
			}
		});
	}

	public SWTCanvas getCanvas() {
		return canvas;
	}

	public CanvasToolBar getToolBar() {
		return toolBar;
	}

	public ToolItem getZoomIn() {
		return toolBar.getZoomIn();
	}

	public ToolItem getZoomOut() {
		return toolBar.getZoomOut();
	}

	public ToolItem getZoomSelection() {
		return toolBar.getZoomSelection();
	}

//	public ToolItem getRotateRight() {
//		return toolBar.getRotateRight();
//	}
//
//	public ToolItem getRotateLeft() {
//		return toolBar.getRotateLeft();
//	}
//
//	public ToolItem getTranslateLeft() {
//		return toolBar.getTranslateLeft();
//	}
//
//	public ToolItem getFitToPage() {
//		return toolBar.getFitToPage();
//	}
//
//	public ToolItem getTranslateRight() {
//		return toolBar.getTranslateRight();
//	}

	public ToolItem getSelectionMode() {
		return toolBar.getSelectionMode();
	}

//	public ToolItem getTranslateDown() {
//		return toolBar.getTranslateDown();
//	}
//
//	public ToolItem getTranslateUp() {
//		return toolBar.getTranslateUp();
//	}

	public ToolItem getOriginalSize() {
		return toolBar.getOriginalSize();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void updateUiStuff() {
		updateUndoButton();
		toolBar.updateButtonVisibility();
	}
	
	public void updateUndoButton() {
		if (canvas.getUndoStack().getSize()!=0) {
			toolBar.getUndo().setEnabled(true);
			toolBar.getUndo().setToolTipText("Undo ("+canvas.getUndoStack().getSize()+"): "+canvas.getUndoStack().getLastOperationDescription());
		}
		else {
			toolBar.getUndo().setEnabled(false);
			toolBar.getUndo().setToolTipText("Undo: Nothing do undo...");
		}
	}
}
