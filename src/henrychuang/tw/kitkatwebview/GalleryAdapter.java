package henrychuang.tw.kitkatwebview;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore.Images.Thumbnails;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GalleryAdapter extends BaseAdapter {
  private static final ThreadFactory sThreadFactory = new ThreadFactory() {
    private final AtomicInteger mCount = new AtomicInteger(1);

    public Thread newThread(Runnable r) {
      return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
    }
  };

  public static final Executor DUAL_THREAD_EXECUTOR = Executors.newFixedThreadPool(5, sThreadFactory);
  
  private LayoutInflater infalter;
  private List<ImageData> data;
  private Set<Long> selected;
  private ContentResolver resolver;
  
  private int itemHeight = 0;
  private GridView.LayoutParams imageViewLayoutParams;
  
  private Map<Long, SoftReference<Bitmap>> cache;
  
  private boolean selectMode;
  public boolean noCache = false;

  public GalleryAdapter(Context context, boolean selectMode) {
    this.infalter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.data = new ArrayList<ImageData>();
    this.selected = new HashSet<Long>();
    
    this.resolver = context.getContentResolver();
    
    this.imageViewLayoutParams = new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);
    
    this.cache = new HashMap<Long, SoftReference<Bitmap>>();
    this.selectMode = selectMode;
  }

  @Override
  public int getCount() {
    return data.size();
  }

  @Override
  public ImageData getItem(int position) {
    return data.get(position);
  }

  @Override
  public long getItemId(int position) {
    return data.get(position).getId();
  }

  public void selectAll(boolean selection) {
    if (this.selectMode) {
      for (ImageData d : this.data) {
        this.selected.add(d.getId());
      }
      notifyDataSetChanged();
    }
  }

  public boolean isAllSelected() {
    return this.data.size() == this.selected.size();
  }

  public boolean isAnySelected() {
    return this.data.size() >= this.selected.size();
  }
  
  public int getSelectedCount() {
    return this.selected.size();
  }

  public ArrayList<ImageData> getSelected() {
    ArrayList<ImageData> selectedData = new ArrayList<ImageData>();
    for(long selectedId : this.selected){
      for(ImageData d : this.data){
        if(d.getId() == selectedId){
          selectedData.add(d);
          break;
        }
      }
    }
    
    return selectedData;
  }

  public void addAll(List<ImageData> data) {
	this.cache.clear();
    this.data.clear();
    this.data.addAll(data);

    this.notifyDataSetChanged();
  }

  public void changeSelection(int position) {
    if(this.selectMode){
      if (this.selected.contains(data.get(position).getId())) {
        this.selected.remove(data.get(position).getId());
      } else {
        this.selected.add(data.get(position).getId());
      }

      this.notifyDataSetChanged();
    }
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      convertView = infalter.inflate(R.layout.gallery_item, null);
    }
    
    ImageView image = (ImageView) convertView.findViewById(R.id.image);
    ImageView check = (ImageView) convertView.findViewById(R.id.check);
    
    final ImageData d = data.get(position); 
    
    if(noCache)
    	this.cache.clear();
    
    Bitmap b = null;
    if(this.cache.containsKey(d.getId())){
      b = this.cache.get(d.getId()).get();
    }
    
    if(b != null){
      image.setImageBitmap(b);
    }else{
      image.setImageBitmap(null);
      image.setTag(d.getId());
      
      final BitmapWorkerTask task = new BitmapWorkerTask(image, this.resolver, this.cache);
      
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        task.executeOnExecutor(DUAL_THREAD_EXECUTOR);
      } else {
        task.execute();
      }
    }
    
    if(this.selectMode){
      check.setSelected(this.selected.contains(d.getId()));
    }else{
      check.setVisibility(View.GONE);
    }
    

    convertView.setTag(d);
    convertView.setLayoutParams(this.imageViewLayoutParams);

    return convertView;
  }

  public void clear() {
    this.data.clear();
    this.cache.clear();
    this.notifyDataSetChanged();
  }
  

  public void setItemHeight(int height) {
    if (height == itemHeight) {
        return;
    }
    this.itemHeight = height;
    this.imageViewLayoutParams =
            new GridView.LayoutParams(
                GridView.LayoutParams.MATCH_PARENT,
                this.itemHeight);
    this.notifyDataSetChanged();
  }
  
  public int getItemHeight() {
    return this.itemHeight;
  }
  
  private static class BitmapWorkerTask extends AsyncTask<Void, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private final WeakReference<ContentResolver> resolverReference;
    private final WeakReference<Map<Long, SoftReference<Bitmap>>> cacheReference;
    private final long id;

    public BitmapWorkerTask(
        ImageView imageView,
        ContentResolver resolver,
        Map<Long, SoftReference<Bitmap>> cache) {
      this.imageViewReference = new WeakReference<ImageView>(imageView);
      this.resolverReference = new WeakReference<ContentResolver>(resolver);
      this.cacheReference = new WeakReference<Map<Long, SoftReference<Bitmap>>>(cache);
      
      this.id = (Long)imageView.getTag();
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
      Bitmap bitmap = null;
      ContentResolver resolver = this.resolverReference.get();
      
      if(resolver != null){
        bitmap = Thumbnails.getThumbnail(resolver, this.id, Thumbnails.MICRO_KIND, null);
      }
      
      Map<Long, SoftReference<Bitmap>> cache = this.cacheReference.get();
      
      if(bitmap != null && cache != null){
        cache.put(this.id, new SoftReference<Bitmap>(bitmap));
      }

      return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      ImageView imageView = this.imageViewReference.get();
      
      if(bitmap != null && imageView != null &&
          ((Long)imageView.getTag()).equals(this.id)){
        imageView.setImageBitmap(bitmap);
      }
    }
  }

}
