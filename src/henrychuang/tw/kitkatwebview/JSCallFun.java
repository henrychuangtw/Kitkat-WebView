package henrychuang.tw.kitkatwebview;

import android.app.Activity;
import android.content.Intent;

public class JSCallFun {
	private Activity activity;
	private int requestCode;
	public JSCallFun(Activity activity, int requestCode){
		this.activity = activity;
		this.requestCode = requestCode;
	}
	
	public void chooseImg(int minSelect,int maxSelect){		
		Intent intent = new Intent(activity, ImageSelectActivity.class);
        intent.putExtra(ImageSelectActivity.EXTRA_MAX_SELECT, maxSelect);
        intent.putExtra(ImageSelectActivity.EXTRA_MIN_SELECT, minSelect);
        activity.startActivityForResult(intent, requestCode);
	}
}
