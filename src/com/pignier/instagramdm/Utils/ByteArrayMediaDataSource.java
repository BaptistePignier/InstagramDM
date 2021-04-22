package com.pignier.instagramdm.Utils;

import android.media.MediaDataSource;
import android.util.Log;
import java.io.IOException;

//https://stackoverflow.com/a/34759311
public class ByteArrayMediaDataSource extends MediaDataSource {

	private final byte[] data;

	public ByteArrayMediaDataSource(byte []data) {
		assert data != null;
		this.data = data;
	}
	@Override
	public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
		if (position >= data.length) {
			return -1; // -1 indicates EOF 
		}
		int endPosition = (int) (position + size);
		int size2 = size;
		if (endPosition > data.length){
			size2 -= endPosition - data.length;
		}
		System.arraycopy(data, (int)position, buffer, offset, size2);
		return size2;
	}
	@Override
	public long getSize() throws IOException {
		return data.length;
	}

	@Override
	public void close() throws IOException {
		// Nothing to do here
	}
}