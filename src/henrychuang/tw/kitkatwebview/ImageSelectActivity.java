package henrychuang.tw.kitkatwebview;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageSelectActivity extends FragmentActivity implements LoaderCallbacks<Cursor>{
  public static final String EXTRA_MAX_SELECT = "max_select";
  public static final String EXTRA_MIN_SELECT = "min_select";
  
  private GridView gridGallery;
  private GalleryAdapter adapter;
  
  private ImageView noMediaImage;
  private Button galleryOkButton, btnCamera;
  
  private int imageThumbSize;
  private int imageThumbSpacing;
  
  private int maxSelect;
  private int minSelect;
  
  private static final int IMAGE_CAPTURE = 60; 
  private Uri imageUri; 
  
  private int loaderID = 100;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    this.imageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
    this.imageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
    
    this.maxSelect = this.getIntent().getIntExtra(EXTRA_MAX_SELECT, Integer.MAX_VALUE);
    this.minSelect = this.getIntent().getIntExtra(EXTRA_MIN_SELECT, 1);
    
    this.setContentView(R.layout.image_selector);
    
    this.gridGallery = (GridView) findViewById(R.id.gridview);
    this.noMediaImage = (ImageView) findViewById(R.id.img_no_media);
    this.galleryOkButton = (Button) findViewById(R.id.btn_gallery_ok);
    this.btnCamera = (Button)findViewById(R.id.btn_camera);
    
    this.btnCamera.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			java.util.Date now = new java.util.Date();		
		    String fileName = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(now) + ".jpg";	        
		    ContentValues values = new ContentValues(); 
		    values.put(MediaStore.Images.Media.TITLE, fileName); 
		    values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera"); 
		    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); 
//		    imageUri = getContentResolver().insert( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values); 
			
	        
	        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
	        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); 
	        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); 
	        startActivityForResult(intent, IMAGE_CAPTURE); 
		}
	});
    
    this.galleryOkButton.setEnabled(false);
    this.galleryOkButton.setText(this.getString(R.string.btn_chioce, 0));
    
    this.adapter = new GalleryAdapter(this.getApplicationContext(), true);
    
    this.gridGallery.setAdapter(this.adapter);
    this.gridGallery.setFastScrollEnabled(false);
    
    this.gridGallery.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        adapter.changeSelection(position);
        int selectedCount = adapter.getSelectedCount();
        if(minSelect <= selectedCount && maxSelect >= selectedCount){
          galleryOkButton.setEnabled(true);
        }else{
          galleryOkButton.setEnabled(false);
        }
        galleryOkButton.setText(getString(R.string.btn_chioce, selectedCount));
      }
    });
    
    this.gridGallery.getViewTreeObserver().addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            if (adapter.getItemHeight() == 0) {
              final int numColumns = (int) Math.floor(gridGallery.getWidth()/ (imageThumbSize + imageThumbSpacing));
              if (numColumns > 0) {
                final int columnWidth = (gridGallery.getWidth() / numColumns) - imageThumbSpacing;
                adapter.setItemHeight(columnWidth);
              }
            }
          }
        });
    
    this.galleryOkButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        ArrayList<ImageData> selected = adapter.getSelected();

        Intent data = new Intent().putExtra(Intent.EXTRA_TEXT, selected);
        setResult(RESULT_OK, data);
        finish();
      }
    });
    
    this.getSupportLoaderManager().initLoader(loaderID, null, this);
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  Log.d("henrytest",String.format("ImageSelectActivity.onActivityResult -> requestCode: %d, resultCode: %d",requestCode,resultCode));
      if (requestCode == IMAGE_CAPTURE && resultCode == RESULT_OK) { 
    	  	try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
              this.getSupportLoaderManager().restartLoader(loaderID, null, this);             
              
      } 
  }
  
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    final String[] columns = {
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.MIME_TYPE,
        MediaStore.Images.Media.DATA};
    final String orderBy = MediaStore.Images.Media.DATE_MODIFIED + " desc";
    
    final int maxSize = 5*1024*1024; 
    
    CursorLoader loader = new CursorLoader(this, 
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
        MediaStore.Images.Media.MIME_TYPE+"=? and "+MediaStore.Images.Media.SIZE+"<"+maxSize,
        new String[]{"image/jpeg"},
        orderBy);
    
    return loader;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    try {
      List<ImageData> list = new ArrayList<ImageData>();
      
      if (cursor != null && cursor.getCount() > 0) {

        while (cursor.moveToNext()) {
          
          ImageData data = new ImageData();

          data.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
          data.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
          list.add(data);
        }
        
        adapter.clear();
        adapter.addAll(list);
        checkImageStatus();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if(cursor != null){
        cursor.close();
      }
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> cursor) {
  }
  
  private void checkImageStatus() {
    if (adapter.isEmpty()) {
      this.noMediaImage.setVisibility(View.VISIBLE);
    } else {
      this.noMediaImage.setVisibility(View.GONE);
    }
  }
}
