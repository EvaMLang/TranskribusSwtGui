package eu.transkribus.swt_gui.factory;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent.BaselineType;
import eu.transkribus.core.model.beans.pagecontent.CoordsType;
import eu.transkribus.core.model.beans.pagecontent.RegionType;
import eu.transkribus.core.model.beans.pagecontent.TextEquivType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.RegionTypeUtil;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPrintSpaceType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.core.model.beans.pagecontent_trp.observable.TrpObserveEvent.TrpConstructedWithParentEvent;
import eu.transkribus.core.util.PrimaUtils;
import eu.transkribus.swt_canvas.canvas.CanvasMode;
import eu.transkribus.swt_canvas.canvas.SWTCanvas;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasPolygon;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasPolyline;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.canvas.TrpCanvasAddMode;
import eu.transkribus.swt_gui.canvas.TrpSWTCanvas;
import eu.transkribus.swt_gui.exceptions.BaselineExistsException;
import eu.transkribus.swt_gui.exceptions.NoParentLineException;
import eu.transkribus.swt_gui.exceptions.NoParentRegionException;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;
import eu.transkribus.swt_gui.util.GuiUtil;

/**
 * Collection of static methods to create a TreeItem and / or ICanvasShape objects from a given PAGE data object or vice versa
 */
public class TrpShapeElementFactory {
	private final static Logger logger = LoggerFactory.getLogger(TrpShapeElementFactory.class);
	
	TrpMainWidget mainWidget;
	TrpSWTCanvas canvas;

	public TrpShapeElementFactory(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		this.canvas = mainWidget.getCanvas();
	}
	
	public void readjustChildrenForShape(ICanvasShape shape, ITrpShapeType parentTrpShape) throws Exception {
		parentTrpShape.removeChildren();
		for (ICanvasShape childShape : shape.getChildren(false)) {
			ITrpShapeType st = GuiUtil.getTrpShape(childShape);
			
			logger.trace("shape type: "+st+" parent shape: "+parentTrpShape);
			
			if (st!=null) {
				st.removeFromParent();
				st.setParent(parentTrpShape);
				st.reInsertIntoParent();
			} else {
				throw new Exception("Fatal error: could not find the data object for the child shape: "+childShape);
			}
			
		}
		logger.trace("n-childs after child readjust: "+parentTrpShape.getChildren(false).size());
	}
	
	/** Synchronizes parent/child and data info between a CanvasShape and a ITrpShapeType. Also sets the color of the shape
	 * according to the ITrpShapeType. Returns the parent shape of the given ICanvasShape */
	private ICanvasShape syncCanvasShapeAndTrpShape(ICanvasShape shape, ITrpShapeType trpShape) {
		TrpSettings sets = mainWidget.getTrpSets();
		// update info in shape:
		shape.setColor(TrpSettings.determineColor(sets, trpShape));
		shape.setLevel(trpShape.getLevel());
		
		boolean hasBaseline = false;
		double readingOrderSize = 0;
				
		if (trpShape.getChildren(false).size() > 0 && trpShape.getChildren(false).get(0) instanceof TrpBaselineType){
			hasBaseline = true;
		}
		
//		if (trpShape instanceof RegionType){
//			for (ITrpShapeType currShape : trpShape.getChildren(false)){
//				if (currShape instanceof TrpTextLineType){
//					TrpTextLineType tl = (TrpTextLineType) currShape;
//					Rectangle tmp = (Rectangle) PageXmlUtils.buildPolygon(tl.getCoords().getPoints()).getBounds();
//					if (readingOrderSize == 0){
//						readingOrderSize = tmp.getHeight()/2;
//					}
//					else{
//						readingOrderSize = (readingOrderSize + tmp.getHeight()/2)/2;
//					}
//					
//				}
//			}
//		}
//		else if (trpShape instanceof TrpTextLineType || trpShape instanceof TrpWordType){
//			TrpTextLineType tl = null;
//			if (trpShape instanceof TrpTextLineType)
//				tl = (TrpTextLineType) trpShape;
//			else
//				tl = (TrpTextLineType) trpShape.getParentShape();
//			
//			Rectangle tmp = (Rectangle) PageXmlUtils.buildPolygon(tl.getCoords().getPoints()).getBounds();
//			readingOrderSize = tmp.getHeight()/2;
//		}
		
		//create reading Order shape for canvas shape
		shape.createReadingOrderShape((SWTCanvas) canvas, trpShape instanceof TrpTextRegionType, trpShape instanceof TrpTextLineType, trpShape instanceof TrpWordType, hasBaseline);
		
		// needed? also done in onBeforeDrawScene
		if (trpShape instanceof TrpTextRegionType){
			shape.showReadingOrder(mainWidget.getTrpSets().isShowReadingOrderRegions());
		}
		else if (trpShape instanceof TrpTextLineType){
			shape.showReadingOrder(mainWidget.getTrpSets().isShowReadingOrderLines());
		}
		else if (trpShape instanceof TrpWordType){
			shape.showReadingOrder(mainWidget.getTrpSets().isShowReadingOrderWords());
		}

		
		// update parent info for shape:
		ICanvasShape pShape = null;
		if (trpShape.getParentShape() != null) {
			pShape = canvas.getScene().findShapeWithData(trpShape.getParentShape());
			shape.setParentAndAddAsChild(pShape);
		} else {
			shape.setParent(null);
		}
			
		// set new data to this shape:
		shape.setData(trpShape);
		
		// set shape as data of new ITrpShapeType object
		trpShape.setData(shape);		
		// add observer:
		trpShape.addObserver(mainWidget.getTranscriptObserver());
		
		return pShape;
	}
	
	/** Creates a <emph>new</emph> ITrpShapeType element from the given canvas shape where an existing ITrpShape element is
	 * already emebedded. This is needed for the split and merge operations! */
	public ITrpShapeType copyJAXBElementFromShapeAndData(ICanvasShape shape, int index) throws Exception {
		if (Storage.getInstance().getTranscript()==null)
			throw new Exception("No transcript loaded - should not happen!");
		
		ITrpShapeType trpShape = GuiUtil.getTrpShape(shape);
		
		ITrpShapeType copyTrpShape=trpShape.copy();
		
		// update parent info for trpShape:
		logger.debug("setting new parent shape: "+GuiUtil.getTrpShape(shape.getParent())+ " shape: "+shape);
		ITrpShapeType parentTrpShape = GuiUtil.getTrpShape(shape.getParent());
		if (parentTrpShape!=null)
			copyTrpShape.setParent(parentTrpShape);
		
		// set coordinates:
		copyTrpShape.setCoordinates(PrimaUtils.pointsToString(shape.getPoints()), this);
		
		copyTrpShape.reInsertIntoParent(index);				
			
		// sync canvas shape and trp shape info:
		syncCanvasShapeAndTrpShape(shape, copyTrpShape);
		
		return copyTrpShape;		
	}	
	
	/** Creates a new ITrpShapeType element from the given shape that was created in the canvas. The CanvasMode m determines
	 * the type of shape that shall be created.
	 * @param selSt 
	 */
	public ITrpShapeType createJAXBElementFromShape(ICanvasShape shape, CanvasMode m, ICanvasShape selectedParentShape) throws NoParentRegionException, NoParentLineException, BaselineExistsException, Exception {
		if (Storage.getInstance().getTranscript()==null)
			throw new Exception("No transcript loaded - should not happen!");
		
		ITrpShapeType trpShape=null;
		ICanvasShape parentShape = null;
		
		TrpSettings setts = TrpMainWidget.getTrpSettings();
				
		logger.debug("adding - data = "+m.data);
		
		String specialRegionType = (m.data != null && m.data instanceof String) ? (String) m.data : "";
//		String specialRegionType = mainWidget.getCanvasWidget().getToolBar().getSelectedSpecialRegionType();

		if (m.equals(TrpCanvasAddMode.ADD_PRINTSPACE) || specialRegionType.equals(RegionTypeUtil.PRINTSPACE_TYPE)) {
			TrpPageType parent = Storage.getInstance().getTranscript().getPage();
			if (parent.getPrintSpace()!=null)
				throw new Exception("Printspace already exists!");
			TrpPrintSpaceType ps = createPAGEPrintSpace(shape, parent);
			trpShape = ps;
		}
		else if (m.equals(TrpCanvasAddMode.ADD_TEXTREGION)) {
			// create text region and add it to the shape:
			TrpPageType parent = Storage.getInstance().getTranscript().getPage();
			TrpTextRegionType tr = createPAGETextRegion(shape, parent);
			trpShape = tr;
		}
		else if (m.equals(TrpCanvasAddMode.ADD_OTHERREGION)) {
			logger.debug("adding special region, type  = "+specialRegionType);
			if (!specialRegionType.isEmpty()) {
				TrpPageType parent = Storage.getInstance().getTranscript().getPage();
				TrpRegionType rt = createRegionType(shape, parent, specialRegionType);
				trpShape = rt;
			} else
				throw new Exception("Invalid special region type: "+specialRegionType+" - should not happen!");			
		}
		else if (m.equals(TrpCanvasAddMode.ADD_LINE)) {
			String errorMsg = "";
			if (setts.isAddLinesToOverlappingRegions()) {
				parentShape = canvas.getScene().findOverlappingShapeWithDataType(shape, TrpTextRegionType.class);
				errorMsg = "Could not find an overlapping parent text region!";
			}
			else if (selectedParentShape != null && selectedParentShape.getData() instanceof TrpTextRegionType) {
				parentShape = selectedParentShape;
				errorMsg = "No parent region selected!";
			}
			
			if (parentShape == null)
				throw new NoParentRegionException(errorMsg);
			
			TrpTextRegionType parent = (TrpTextRegionType) parentShape.getData();
			TrpTextLineType tl = createPAGETextLine(shape, parent);
			trpShape = tl;
		}
		else if (m.equals(TrpCanvasAddMode.ADD_BASELINE)) {
			String errorMsg = "";
			if (setts.isAddBaselinesToOverlappingLines()) {
				parentShape = canvas.getScene().findOverlappingShapeWithDataType(shape, TrpTextLineType.class);
				errorMsg = "Could not find an overlapping parent line!";	
			}
			else if (selectedParentShape != null && selectedParentShape.getData() instanceof TrpTextLineType) {
				parentShape = selectedParentShape;
				errorMsg = "No parent line selected!";
			}
			
			if (parentShape == null)
				throw new NoParentLineException(errorMsg);			
			
			TrpTextLineType parent = (TrpTextLineType) parentShape.getData();
			if (parent.getBaseline()!=null)
				throw new BaselineExistsException("Baseline already exists in parent line with id = "+parent.getId()+"\nRemove or edit existing baseline!");
			
			TrpBaselineType bl = createPAGEBaseline(shape, parent);
			trpShape = bl;
		}
		else if (m.equals(TrpCanvasAddMode.ADD_WORD)) {
			String errorMsg = "";
			if (setts.isAddWordsToOverlappingLines()) {
				parentShape = canvas.getScene().findOverlappingShapeWithDataType(shape, TrpTextLineType.class);
				errorMsg = "Could not find an overlapping parent line!";
			}
			else if (selectedParentShape != null && selectedParentShape.getData() instanceof TrpTextLineType) {
				parentShape = selectedParentShape;
				errorMsg = "No parent line selected!";
			}
			
			if (parentShape == null)
				throw new NoParentLineException(errorMsg);
			
			TrpTextLineType parent = (TrpTextLineType) parentShape.getData();
			TrpWordType word = createPAGEWord(shape, parent);
			trpShape = word;
		}
		else {
			throw new Exception("No add valid operation specified (should not happen...)");
		}
		
		// sync canvas shape and trp shape info:
		syncCanvasShapeAndTrpShape(shape, trpShape);
		
		return trpShape;		
	}
	
	/**
	 * Creates a canvas shape for the given jaxb shape element
	 */
	private ICanvasShape addCanvasShape(ITrpShapeType trpShape) throws Exception {
		String points = trpShape.getCoordinates();
				
		// create polygon and set wrapped data and color:
		ICanvasShape shape = null;
		if (trpShape instanceof BaselineType) {
			shape = new CanvasPolyline(points);
		}
		else {
			shape = new CanvasPolygon(points);
		}
		
		ICanvasShape pShape = syncCanvasShapeAndTrpShape(shape, trpShape);
		
		// add it to the canvas and adjust some stuff:
		canvas.getScene().addShape(shape, pShape, false); // add shape without sending a signal
		mainWidget.getCanvasShapeObserver().addShapeToObserve(shape);
		shape.setEditable(canvas.getSettings().isEditingEnabled());
		
		return shape;
	}
	
	/**
	 * Creates and adds canvas shapes for the given ITrpShapeType object and all its children elements
	 */
	public List<ICanvasShape> addAllCanvasShapes(ITrpShapeType trpShape) throws Exception {
		// add shape for given shape:
		List<ICanvasShape> shapes = new ArrayList<ICanvasShape>();
		shapes.add(addCanvasShape(trpShape));

		// add shape for all subelemetns:
		for (ITrpShapeType t : trpShape.getChildren(true)) {
			shapes.add(addCanvasShape(t));
		}
		
		return shapes;
	}
		
	// Methods to create PAGE elements from a shape:
	private static TrpPrintSpaceType createPAGEPrintSpace(ICanvasShape shape, TrpPageType parent) {
		TrpPrintSpaceType ps = new TrpPrintSpaceType(parent);
		
		CoordsType coords = new CoordsType();
		coords.setPoints(PrimaUtils.pointsToString(shape.getPoints()));
		ps.setCoords(coords);
		
		parent.setPrintSpace(ps);
		
		return ps;
	}
	
	private static TrpTextRegionType createPAGETextRegion(ICanvasShape shape, TrpPageType parent) {		
		TrpTextRegionType tr = new TrpTextRegionType(parent);
		
		tr.setId(TrpPageType.getUniqueId("region"));
//		tr.setId("region_"+System.currentTimeMillis());
		
		CoordsType coords = new CoordsType();
		coords.setPoints(PrimaUtils.pointsToString(shape.getPoints()));
		tr.setCoords(coords);	
		
		//TODO: add index according to coordinates		int idxOfNewLine = parent.getIndexAccordingToCoordinates(tl);
		int idxOfNewTextRegion = parent.getIndexAccordingToCoordinates(tr);
		logger.debug("idxOfNewTextRegion " + idxOfNewTextRegion);
		
		if (parent.getTextRegionOrImageRegionOrLineDrawingRegion().size() > idxOfNewTextRegion){
			//inserts at specific pos
			tr.setReadingOrder(idxOfNewTextRegion,  TrpShapeElementFactory.class);
			parent.getTextRegionOrImageRegionOrLineDrawingRegion().add(idxOfNewTextRegion, tr);
		}
		else{
			//append list
			tr.setReadingOrder(parent.getTextRegionOrImageRegionOrLineDrawingRegion().size(),  TrpShapeElementFactory.class);
			parent.getTextRegionOrImageRegionOrLineDrawingRegion().add(tr);
		}
				
		//parent.getTextRegionOrImageRegionOrLineDrawingRegion().add(tr);
		
		TrpMainWidget.getInstance().getScene().updateAllShapesParentInfo();
		parent.sortRegions();
		
		return tr;
	}
	
	private static TrpTextLineType createPAGETextLine(ICanvasShape shape, TrpTextRegionType parent) {
		TrpTextLineType tl = new TrpTextLineType(parent);
		
		tl.setId(TrpPageType.getUniqueId("line"));
//		tl.setId("line_"+System.currentTimeMillis());
		
		//during creation set the ReadingOrder on the first position - sorting should than merge the shape according to the coordinates
		//tl.setReadingOrder(-1,  TrpShapeElementFactory.class);
		
		CoordsType coords = new CoordsType();
		coords.setPoints(PrimaUtils.pointsToString(shape.getPoints()));
		tl.setCoords(coords);			
		
		tl.setTextEquiv(new TextEquivType());
		tl.getTextEquiv().setUnicode("");
		
		int idxOfNewLine = parent.getIndexAccordingToCoordinates(tl);
		logger.debug("idxOfNewLine " + idxOfNewLine);
		
		if (parent.getTextLine().size() > idxOfNewLine){
			//inserts at specific pos
			tl.setReadingOrder(idxOfNewLine,  TrpShapeElementFactory.class);
			parent.getTextLine().add(idxOfNewLine, tl);
		}
		else{
			//append list
			tl.setReadingOrder(parent.getTextLine().size(),  TrpShapeElementFactory.class);
			parent.getTextLine().add(tl);
		}

		for (int i = 0; i<parent.getTextLine().size(); i++){
		logger.debug(i + "-th line in text " + parent.getTextLine().get(i).getId());	
		}
		
		parent.applyTextFromLines();
		
		TrpMainWidget.getInstance().getScene().updateAllShapesParentInfo();
		parent.getPage().sortContent();
		
//		parent.getTextLine().add(tl);
		//parent.sortLines();
		
			
		return tl;
	}
	
	private static TrpBaselineType createPAGEBaseline(ICanvasShape shape, TrpTextLineType parent) {
		TrpBaselineType bl = new TrpBaselineType(parent);
		bl.setPoints(PrimaUtils.pointsToString(shape.getPoints()));
		
		//during creation set the ReadingOrder on the first position - sorting should than merge the shape according to the coordinates
		bl.setReadingOrder(-1,  TrpShapeElementFactory.class);
		
		parent.setBaseline(bl);
		return bl;
	}
	
	private static TrpWordType createPAGEWord(ICanvasShape shape, TrpTextLineType parent) {
		TrpWordType word = new TrpWordType(parent);
		
		word.setId(TrpPageType.getUniqueId("word"));
//		word.setId("word_"+System.currentTimeMillis());
		
		//during creation set the ReadingOrder on the first position - sorting should than merge the shape according to the coordinates
		word.setReadingOrder(-1,  TrpShapeElementFactory.class);
		
		CoordsType coords = new CoordsType();
		coords.setPoints(PrimaUtils.pointsToString(shape.getPoints()));
		word.setCoords(coords);	
		
		word.setTextEquiv(new TextEquivType());
		word.getTextEquiv().setUnicode("");
		
		parent.getWord().add(word);
		parent.sortWords();
			
		return word;
	}
	
	/**
	 * Creates a generic region of the given type. Valid types are contained in the field {@link #REGIONS}.<br>
	 * NOTE: currently under testing
	 * @param shape The shape where the points are extracted from
	 * @param parent The parent {@link TrpPageType} object where the created region is added to
	 * @param type The type of region. Valid types are contained in the field {@link #REGIONS}.
	 * @return The created region
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static TrpRegionType createRegionType(ICanvasShape shape, TrpPageType parent, String type) throws InstantiationException, IllegalAccessException {
		
		if (!RegionTypeUtil.isSpecialRegion(type)) {
			throw new UnsupportedOperationException("This is not a special region type: "+type);
		}
		
		Class<? extends ITrpShapeType> clazz = RegionTypeUtil.getRegionClass(type);
		if (clazz == null) {
			throw new UnsupportedOperationException("Could not create region of type: "+type);
		}
		
		TrpRegionType rt = (TrpRegionType) clazz.newInstance();
		
		rt.setParent(parent);
		rt.getObservable().setChangedAndNotifyObservers(new TrpConstructedWithParentEvent(rt));
		
		//during creation set the ReadingOrder on the first position - sorting should than merge the shape according to the coordinates
		rt.setReadingOrder(-1,  TrpShapeElementFactory.class);
		
		if (type.equals(RegionTypeUtil.BLACKENING_REGION)) {
			RegionTypeUtil.setRegionTypeTag(rt, RegionTypeUtil.BLACKENING_REGION, null);
		}
		
		rt.setId(TrpPageType.getUniqueId(type));
//		word.setId("word_"+System.currentTimeMillis());
		
		CoordsType coords = new CoordsType();
		coords.setPoints(PrimaUtils.pointsToString(shape.getPoints()));
		rt.setCoords(coords);	
				
		parent.getTextRegionOrImageRegionOrLineDrawingRegion().add(rt);
		parent.sortRegions();
			
		return rt;
	}
	

	

}
