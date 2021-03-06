package eu.transkribus.swt_gui.vkeyboards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.UnicodeList;
import eu.transkribus.swt_canvas.util.Fonts;
import eu.transkribus.swt_canvas.util.Images;

public class VirtualKeyboard extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(VirtualKeyboard.class);
	
	List<SelectionListener> selListener = new ArrayList<>(); 
	
	/**
	 * redirects selection events from buttons to internal listeners
	 */
	SelectionListener btnSelectionListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
//			e.doit = false; 
			if (e.getSource() instanceof Button) {
				Button b = (Button) e.getSource();
				String text = b.getText();
				
				if (!text.isEmpty()) {
					Event e1 = new Event();
					e1.widget = VirtualKeyboard.this;
					e1.detail = text.charAt(0);
					e1.text = b.getToolTipText();
					
					for (SelectionListener l : selListener) {
						l.widgetSelected(new SelectionEvent(e1));
					}
				}
			}
		}
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};	
	
	List<Button> charBtns=new ArrayList<>();
	Button addBtn;
	
	UnicodeList ul;
		
	public VirtualKeyboard(Composite parent, int style, UnicodeList ul) {
		super(parent, style);
		
		this.ul = ul;
				
		initLayout();
		        
        initButtons(ul.getChars());
//        initInternalListener();
	}
	
	public String getUnicodeHexRange() {
		return ul.getUnicodeHexRange();
	}
	
	public VirtualKeyboard(Composite parent, int style, String name, char unicodeStart, char unicodeEnd) {
		this(parent, style, new UnicodeList(name, unicodeStart, unicodeEnd));
	}

	public VirtualKeyboard(Composite parent, int style, String name, List<Character> chars) {
		this(parent, style, new UnicodeList(name, chars));
	}

	private void initLayout() {
		RowLayout rowLayout = new RowLayout();
        rowLayout.wrap = true;
        rowLayout.pack = true;
        rowLayout.justify = false;
        rowLayout.type = SWT.HORIZONTAL;
        rowLayout.marginLeft = 5;
        rowLayout.marginTop = 5;
        rowLayout.marginRight = 5;
        rowLayout.marginBottom = 5;
        rowLayout.spacing = 0;
        setLayout(rowLayout);
	}
	
	public void reload(String unicodeString) {
		this.ul.initChars(unicodeString);
		logger.info("got chars: "+ul.getChars().size());
		clearButtons();
		initButtons(ul.getChars());
	}
	
	private void clearButtons() {
		for (Button b : charBtns) {
			if (b!=null && !b.isDisposed())
				b.dispose();
		}
		charBtns.clear();
	}
	
	private void initButtons(Collection<Character> unicode) {
		String undefined = "";
		for (Character i : unicode) {
			if (!Character.isDefined(i)) {
				undefined += (int) i+" ";
				
//				throw new RuntimeException("Undefined unicode character: "+(int)i);
//				logger.warn("Undefined unicode value: "+(int)i);
				continue;
			}
			
			Button b = initButton(i);
			
			charBtns.add(b);
		}
		if (!undefined.isEmpty()) {
			logger.warn("Undefined unicode values in virtual keyboard: "+undefined);	
		}
	}
	
	private Button initButton(Character c) {
		Button b = new Button(this, SWT.PUSH);
		
		FontData[] fD = b.getFont().getFontData();
		fD[0].setHeight(16);
//		b.setFont( new Font(getDisplay(), fD[0]));
		b.setFont(Fonts.createFont(fD[0]));
		
		b.setText(Character.toString(c));
		b.setToolTipText(Character.getName(c));
		
		b.addSelectionListener(btnSelectionListener);
		
		return b;
	}
	
//	private void initInternalListener() {
//		internalSelectionListener = new SelectionListener() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				if (e.getSource() instanceof Button) {
//					Button b = (Button) e.getSource();
//					String text = b.getText();
//					
//					if (!text.isEmpty())
//						onKeyPressed(b, text.charAt(0), b.getToolTipText());
//				}
//			}
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		};
//		
//		addSelectionListener(internalSelectionListener);
//	}
	
//	protected void onKeyPressed(Button source, Character character, String name) {
//		logger.info("key pressed: "+character+" name = "+name);
//		logger.info("name = "+Character.getName(character));
//	}
	
	public void addKeySelectionListener(SelectionListener listener) {
		selListener.add(listener);
	}
	
	public void removeKeySelectionListener(SelectionListener listener) {
		selListener.remove(listener);
	}
	
	public String getVirtualKeyboardName() {
		return ul.getName();
	}
	
//	public void addCharachter(Character c) {
//		
//		if (ul.addChar(c)) {
//			Button b = initButton(c);
//			charBtns.add(b);
//		}
//	}
	
	private List<Button> getUnicodeButtons() {
		return charBtns;
//		List<Button> btns = new ArrayList<>();
//		for (Control c : this.getChildren()) {
//			if (c instanceof Button) {
//				btns.add((Button)c);
//			}
//		}
//		return btns;
	}
	
	public static void main(String [] args) throws Exception {
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		
		final VirtualKeyboard vk = new VirtualKeyboard(shell, SWT.SHELL_TRIM, "whatever", (char) 65, (char) 122);
//		final VirtualKeyboard vk = new VirtualKeyboard(shell, SWT.NONE, (char) 65, (char) 122);
//		
		shell.pack();
		shell.open();
		
//		shell.addListener(SWT.Resize, new Listener() {
//			public void handleEvent(Event e) {
//				vk.pack();
//			}
//		});
		
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}
}
