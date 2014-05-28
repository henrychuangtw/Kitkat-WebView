package henrychuang.tw.kitkatwebview;

public class ImagePath {
	public long ID;
	public String UriPath;
	public String Path;
	public String Base64;
	
	public ImagePath(){};
	
	public ImagePath(long ID, String UriPath, String Path, String Base64){
		this.ID = ID;
		this.UriPath = UriPath;
		this.Path = Path;
		this.Base64 = Base64;
	}
}
