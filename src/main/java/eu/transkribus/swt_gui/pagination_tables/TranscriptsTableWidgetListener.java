package eu.transkribus.swt_gui.pagination_tables;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MessageBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class TranscriptsTableWidgetListener implements SelectionListener, IDoubleClickListener {
	private final static Logger logger = LoggerFactory.getLogger(TranscriptsTableWidgetListener.class);
	
	TrpMainWidget mw;
	TranscriptsTableWidgetPagination vw;
	TableViewer tv;
	
	public TranscriptsTableWidgetListener(TrpMainWidget mw) {
		this.mw = mw;
		this.vw = mw.getUi().getVersionsWidget();
		this.tv = vw.getPageableTable().getViewer();
		
		tv.addDoubleClickListener(this);
		tv.getTable().addSelectionListener(this);
		
		if (vw.deleteBtn != null)
			vw.deleteBtn.addSelectionListener(this);
	}

	@Override public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();
		if(s == vw.deleteBtn){
			TrpTranscriptMetadata md = vw.getFirstSelected();
			if (md!=null) {
				deleteTranscript(md);
			}
		}
	}

	@Override public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override public void doubleClick(DoubleClickEvent event) {
		TrpTranscriptMetadata md = vw.getFirstSelected();
		logger.debug("double click on transcript: "+md);
		
		if (md!=null) {
			logger.debug("Loading transcript: "+md);
			mw.jumpToTranscript(md, true);
		}		
	}

	private void deleteTranscript(TrpTranscriptMetadata tMd) {
		logger.info("delete transcript: " + tMd.getKey());
		
		int itemCount = (int) vw.getPageableTable().getController().getTotalElements();
		
		if(itemCount == 1 || tMd.getKey() == null){
			MessageBox messageBox = new MessageBox(vw.getShell(), SWT.ICON_INFORMATION
		            | SWT.OK);
	        messageBox.setMessage("Can not delete this version.");
	        messageBox.setText("Unauthorized");
	        messageBox.open();
		} else {
			try {
				Storage store = Storage.getInstance();
				
				TrpTranscriptMetadata currentTranscript = store.getTranscriptMetadata();
				logger.debug("deleting transcript");
				store.deleteTranscript(tMd);
				
				// reload page if current transcript was deleted:
				if (currentTranscript!=null && currentTranscript.equals(tMd)) {
					mw.reloadCurrentPage(false);
				} else {
					store.reloadTranscriptsList(store.getCurrentDocumentCollectionId());
				}
			} catch (Exception e1) {
				MessageBox messageBox = new MessageBox(vw.getShell(), SWT.ICON_ERROR
			            | SWT.OK);
		        messageBox.setMessage("Could not delete transcript: " + e1.getMessage());
		        messageBox.setText("Error");
		        messageBox.open();
			}
		}
	}
}
