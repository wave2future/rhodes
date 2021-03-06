package com.rhomobile.barcode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Hashtable;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.rhomobile.rhodes.RhodesActivity;
import com.rhomobile.rhodes.RhodesService;
import com.rhomobile.rhodes.util.Utils;
import com.rhomobile.rhodes.file.RhoFileApi;
import com.rhomobile.rhodes.util.PerformOnUiThread;


public class Barcode {
	
	private static final String TAG = "Barcode Recognizer";

     public static String recognize(String filename) {

    	 InputStream istream = RhoFileApi.open(filename);
    	 
    	 Bitmap image = BitmapFactory.decodeStream(istream);
    	 
    	 RhoFileApi.close(istream);
 
	      if (image != null) {
	    	// we have image
	        LuminanceSource source = new RhoLuminanceSource(image);
	        BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
	        Result result;
	        try {
				  Reader reader = new MultiFormatReader();
				  Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(1);
				  hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
				  result = reader.decode(bitmap, hints);
	        } catch (ReaderException e) {
	          // image not decoded - not found any barcodes	
	          Utils.platformLog(TAG, "Barcode not found in image ["+filename+"] !");	
	          return "";
	        }
	        if (result != null) {
	          String resultText = result.getText();
	          // return recognized string !
	          Utils.platformLog(TAG, "Barcode successful recoginzed in image ["+filename+"], Barcode = ["+resultText+"] !");
	          return resultText;
	        }
	      }
	    Utils.platformLog(TAG, "Unrecognized Error during process file ["+filename+"] !");	      
		return "";
     } 

     private static String ourCallback = null;
     
     public static void take(String callback) {
    	 ourCallback = callback;
    	 
    	 PerformOnUiThread.exec( new Runnable() {
    		 public void run() {
				RhodesActivity ra = RhodesActivity.getInstance();
				Intent intent = new Intent(ra, com.google.zxing.client.android.CaptureActivity.class);
				ra.startActivity(intent);
    			 
    		 }
    	 });
     }
     
   	 public static native void callback(String callbackUrl, String body);
     
     public static void callCancelCallback() {
 		StringBuffer body = new StringBuffer();
		body.append("&rho_callback=1");
		body.append("&status=cancel");
		callback(ourCallback, body.toString());
     }
     
     public static void callOKCallback(String barcode) {
  		StringBuffer body = new StringBuffer();
		body.append("&rho_callback=1");
		body.append("&status=ok");
		if (barcode != null) {
			try {
				String b = URLEncoder.encode(barcode, "utf-8");
				body.append("&barcode=");
				body.append(b);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		callback(ourCallback, body.toString());
     }

}
