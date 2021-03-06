package eu.transkribus.swt_gui.mainwidget;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt_canvas.canvas.CanvasImage;
import eu.transkribus.util.DataCacheFactory;

public class ImageDataDacheFactory extends DataCacheFactory<URL, CanvasImage> {
	private final static Logger logger = LoggerFactory.getLogger(ImageDataDacheFactory.class);
	
//	String removeFileType(String urlStr) {
//		StringBuffer buf = new StringBuffer(urlStr);
//		int s = urlStr.indexOf("&fileType=");
//		if (s != -1) {
//			int e = urlStr.indexOf('&', s+1);
//			if (e==-1) {
//				e = urlStr.length();
//			}
//			
//			buf.replace(s, e, ""); 
//		}
//		
//		return buf.toString();
//	}
	
	@Override public CanvasImage createFromKey(URL key, Object opts) throws Exception {		
//		String fileType = "view";
//		if (opts instanceof String) {
//			fileType = (String) opts;
//		}
//		
//		logger.debug("fileType1 = "+fileType);
//		if (!fileType.equals("orig") && !fileType.equals("view") && !fileType.equals("bin"))
//			fileType = "view";
//		
//		logger.debug("fileType2 = "+fileType);
		
		String urlStr = key.toString();
//		urlStr += "&fileType="+fileType;
		
		try {
//			if (true)
//				throw new Exception("TEST");
			
//			return new CanvasImage(key);
			return new CanvasImage(new URL(urlStr));
		} catch (Exception e) {
			// if a fimagestore viewing file could not be loaded, try to load original image:
			
			urlStr = CoreUtils.removeFileTypeFromUrl(key.toString());
			urlStr+= "&fileType=orig";
			logger.debug("error displaying url - showing orig img at url: "+urlStr);
			return new CanvasImage(new URL(urlStr));
			
//			if (key.toString().endsWith("&fileType=view")) {
//					URL origUrl = new URL(key.toString().replace("&fileType=view", ""));
//					
//					logger.debug("orig img url: "+urlStr);
//						
//			} else
//				throw e;
		}
	}

	@Override public void dispose(CanvasImage element) {
		element.dispose();
	}

}
