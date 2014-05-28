package henrychuang.tw.kitkatwebview;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageData implements Parcelable {

	public static final Parcelable.Creator<ImageData> CREATOR = new Parcelable.Creator<ImageData>() {

		@Override
		public ImageData createFromParcel(Parcel parcel) {
			return new ImageData(parcel);
		}

		@Override
		public ImageData[] newArray(int size) {
			return new ImageData[size];
		}
	};

	private long id;
	private String path;

	public ImageData() {
	}

	public ImageData(Parcel parcel) {
		this.id = parcel.readLong();
		this.path = parcel.readString();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeLong(this.id);
		parcel.writeString(this.path);
	}
}
