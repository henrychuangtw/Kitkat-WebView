package henrychuang.tw.kitkatwebview;


import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import com.google.gson.reflect.TypeToken;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class Main extends Activity {

	int PICK_PHOTO = 100;	
//	String sInitURL = "http://dev.henry.com.tw/KitkatWebview.htm";
	String sInitURL = "file:///android_asset/KitkatWebview.htm";
	WebView myWebView;
	
	
	@SuppressLint("JavascriptInterface")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		myWebView = (WebView)findViewById(R.id.myWebView);
		myWebView.setWebViewClient(new WebViewClient());
		myWebView.setWebChromeClient(new WebChromeClient());
		myWebView.getSettings().setJavaScriptEnabled(true);
		myWebView.getSettings().setAllowFileAccess(true);
//		myWebView.getSettings().setBuiltInZoomControls(true);
		myWebView.addJavascriptInterface(new JSCallFun(Main.this, PICK_PHOTO), "KitKatWebview");
		
		myWebView.loadUrl(sInitURL);	
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == PICK_PHOTO && resultCode == RESULT_OK) {	
			ArrayList<ImageData> list = intent.getParcelableArrayListExtra(Intent.EXTRA_TEXT);
			ArrayList<ImagePath> lstPath = new ArrayList<ImagePath>();
			for (ImageData obj : list) {
				String uriPath = "content://media/external/images/media/" + Long.toString(obj.getId());
				
				Options op = new Options();
				op.inSampleSize = 4;
				Bitmap bm = BitmapFactory.decodeFile(obj.getPath(),op);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();  
				bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); 
				byte[] b = baos.toByteArray(); 
				String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);				
				
				ImagePath oPath = new ImagePath(obj.getId(), uriPath, obj.getPath(), encodedImage);
				lstPath.add(oPath);
			}
			
			Type typeImagePath = new TypeToken<ArrayList<ImagePath>>() {}.getType();
			String sJSON= new com.google.gson.Gson().toJson(lstPath, typeImagePath);
			
			
			myWebView.loadUrl("javascript:KitKatWebviewChooseImgResult(" + sJSON + ")");
			
		}  else {
			super.onActivityResult(requestCode, resultCode, intent);
		}
	}
	
	
}
