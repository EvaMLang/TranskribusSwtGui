package eu.transkribus.swt_canvas.pagination_table;

import org.eclipse.nebula.widgets.pagination.AbstractPageControllerComposite;
import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.swt_canvas.pagingtoolbar.PagingToolBar;

public class PagingToolBarNavigationRenderer extends AbstractPageControllerComposite implements SelectionListener, TraverseListener {
	
	PagingToolBar tb;
	
	public PagingToolBarNavigationRenderer(Composite parent, int style, PageableController controller) {
		super(parent, style, controller);
//		refreshEnabled(controller);
	}

	@Override public void pageIndexChanged(int oldPageNumber, int newPageNumber, PageableController controller) {
		updateUI();
	}

	@Override public void pageSizeChanged(int oldPageSize, int newPageSize, PageableController controller) {
		updateUI();
	}

	@Override public void sortChanged(String oldPopertyName, String propertyName, int oldSortDirection, int sortDirection,
			PageableController paginationController) {
		updateUI();
	}

	@Override public void totalElementsChanged(long oldTotalElements, long newTotalElements, PageableController controller) {
		updateUI();
	}

	@Override public void widgetSelected(SelectionEvent e) {
		PageableController c = getController();
		Object s = e.getSource();
		if (s == tb.getPageFirstBtn()) {
			c.setCurrentPage(0);
		}
		else if (s == tb.getPageLastBtn()) {
			c.setCurrentPage(c.getTotalPages()-1);
		}
		else if (s == tb.getPagePrevBtn()) {
			int prev = c.getCurrentPage()-1 >= 0 ? c.getCurrentPage()-1 : 0;
			c.setCurrentPage(prev);
		}
		else if (s == tb.getPageNextBtn()) {
			int next = c.getCurrentPage()+1 >= c.getTotalPages() ? c.getTotalPages()-1 : c.getCurrentPage()+1;
			c.setCurrentPage(next);
		}
	}

	@Override public void widgetDefaultSelected(SelectionEvent e) {
	}
	
	void updateUI() {
		PageableController c = getController();
		
		int nStart = c.getPageOffset()+1;
		int nEnd = (int) Math.min(c.getPageOffset()+1+c.getPageSize(), c.getTotalElements());
		
		tb.getLabelItem().setText(""+nStart+"-"+nEnd+" / "+c.getTotalElements()+" ");
		tb.setCurrentPageValue((c.getCurrentPage()+1)+"");
		tb.setNPagesValue(c.getTotalPages()+"");
	}

	@Override protected void createUI(Composite arg0) {
		this.setLayout(new FillLayout());
		tb = new PagingToolBar("", true, false, this, SWT.NONE);
		tb.removeReloadButton();
		
		tb.getPageFirstBtn().addSelectionListener(this);
		tb.getPageLastBtn().addSelectionListener(this);
		tb.getPageNextBtn().addSelectionListener(this);
		tb.getPagePrevBtn().addSelectionListener(this);
		
		tb.getCurrentPageText().addTraverseListener(this);
	}

	@Override public void keyTraversed(TraverseEvent e) {
		if (e.getSource() == tb.getCurrentPageText() && e.detail == SWT.TRAVERSE_RETURN) {
			PageableController c = getController();
			String pageTxt = tb.getCurrentPageValue();
			try {
				int page = Integer.parseInt(pageTxt) - 1;
				if (page >= 0 && page<c.getTotalPages())
					c.setCurrentPage(page);
				else
					updateUI();
			} catch (NumberFormatException ex) {
				updateUI();
			}
		}
	}

}
