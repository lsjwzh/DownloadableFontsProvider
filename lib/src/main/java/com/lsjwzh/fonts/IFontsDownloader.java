package com.lsjwzh.fonts;

public interface IFontsDownloader {
  int getCursorId(String key);
  long getFileId(String key);
  int getFontTTCIndex(String key);
  String getFontVariationSettings(String key);
  int getFontWeight(String key);
  int getFontItalic(String key);
  boolean support(String key);
  DownloadStatus queryStatus(String key);
  void startDownload(String key);

  enum DownloadStatus {
    IDLE,
    DOWNLOADING,
    DOWNLOADED,
    ERROR,
  }
}
