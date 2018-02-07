package com.example.android.emojicompat;

import android.content.Context;
import android.os.ParcelFileDescriptor;

import com.lsjwzh.fonts.FontsProvider;
import com.lsjwzh.fonts.IFontsDownloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by wenye on 2018/2/6.
 */
public class FontDownloader implements IFontsDownloader {
  Context mContext;

  public FontDownloader(Context context) {
    mContext = context;
  }

  @Override
  public int getCursorId(String key) {
    return 0;
  }

  @Override
  public long getFileId(String key) {
    File file = new File(mContext.getCacheDir(), key);
    try {
      return ParcelFileDescriptor.open(file, FontsProvider.modeToMode("rw")).getFd();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  public int getFontTTCIndex(String key) {
    return 0;
  }

  @Override
  public String getFontVariationSettings(String key) {
    return "";
  }

  @Override
  public int getFontWeight(String key) {
    return -1;
  }

  @Override
  public int getFontItalic(String key) {
    return -1;
  }

  @Override
  public boolean support(String key) {
    return true;
  }

  @Override
  public DownloadStatus queryStatus(String key) {
    File file = new File(mContext.getCacheDir(), key);
    return file.exists() ? DownloadStatus.DOWNLOADED : DownloadStatus.DOWNLOADING;
  }

  @Override
  public void startDownload(String key) {
    File file = new File(mContext.getCacheDir(), key);
    if (!file.exists()) {
      try {
        InputStream ttfFileInAssets = mContext.getAssets().open(key);
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[1024 * 10];
        int byteCount = 0;
        while ((byteCount = ttfFileInAssets.read(buffer)) != -1) {
          outputStream.write(buffer, 0, byteCount);
        }
        outputStream.flush();
        ttfFileInAssets.close();
        outputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
