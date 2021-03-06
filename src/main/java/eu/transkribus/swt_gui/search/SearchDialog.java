package eu.transkribus.swt_gui.search;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_canvas.util.LabeledCombo;
import eu.transkribus.swt_canvas.util.LabeledText;
import eu.transkribus.swt_gui.search.documents.DocSearchComposite;
import eu.transkribus.swt_gui.search.kws.KeywordSpottingComposite;
import eu.transkribus.swt_gui.search.text_and_tags.TagSearchComposite;
import eu.transkribus.swt_gui.search.text_and_tags.TextSearchComposite;

public class SearchDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(SearchDialog.class);
	
	DocSearchComposite docSearchComposite;
	KeywordSpottingComposite kwsComposite;

	LabeledText kwsDocId;
	LabeledCombo kwsCollection;
	
	CTabFolder tabFolder;
	CTabItem docSearchTabItem, kwsTabItem, textAndTagsItem;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public SearchDialog(Shell parentShell) {
		super(parentShell);
		
		setShellStyle(SWT.SHELL_TRIM | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		setBlockOnOpen(false);
	}
	
	@Override protected boolean isResizable() {
		return true;
	}
	
	@Override protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Search for...");
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
//		c.setLayout(new FillLayout());
		c.setLayout(new GridLayout());
		
		tabFolder = new CTabFolder(c, SWT.BORDER | SWT.FLAT);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		docSearchComposite = new DocSearchComposite(tabFolder, 0);
		docSearchComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		docSearchTabItem = createCTabItem(tabFolder, docSearchComposite, "Documents");
		
		TextSearchComposite textSearchComp = new TextSearchComposite(tabFolder, 0);
		textSearchComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		textAndTagsItem = createCTabItem(tabFolder, textSearchComp, "Text");
		
		TagSearchComposite tagSearchComp = new TagSearchComposite(tabFolder, 0);
		tagSearchComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		textAndTagsItem = createCTabItem(tabFolder, tagSearchComp, "Tags");		

//		TextAndTagSearchComposite tsc = new TextAndTagSearchComposite(tabFolder, 0);
//		tsc.setLayoutData(new GridData(GridData.FILL_BOTH));
//		textAndTagsItem = createCTabItem(tabFolder, tsc, "Text / Tags");
		
		kwsComposite = new KeywordSpottingComposite(tabFolder, 0);
		kwsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		kwsTabItem = createCTabItem(tabFolder, kwsComposite, "KWS (Demo)");

		return c;
	}
	
	
	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override protected void createButtonsForButtonBar(Composite parent) {
//		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override protected Point getInitialSize() {
		return new Point(1000, 800);
	}
	private CTabItem createCTabItem(CTabFolder tabFolder, Control control, String Text) {
		CTabItem ti = new CTabItem(tabFolder, SWT.NONE);
		ti.setText(Text);
		ti.setControl(control);
		return ti;
	}
	

}
