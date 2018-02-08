package com.lsjwzh.fonts;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.FileNotFoundException;

public class FontsProvider extends ContentProvider {
  public static final int RESULT_CODE_OK = 0;
  /**
   * Constant used to represent a result was not found.
   */
  public static final int RESULT_CODE_FONT_NOT_FOUND = 1;
  /**
   * Constant used to represent a result was found, but cannot be provided at this moment. Use
   * this to indicate, for example, that a font needs to be fetched from the network.
   */
  public static final int RESULT_CODE_FONT_UNAVAILABLE = 2;
  /**
   * Constant used to represent that the query was not in a supported format by the provider.
   */
  public static final int RESULT_CODE_MALFORMED_QUERY = 3;
  public static final String C_ID = "_id";
  public static final String C_FILE_ID = "file_id";
  public static final String C_FONT_TTC_INDEX = "font_ttc_index";
  public static final String C_FONT_VARIATION_SETTINGS = "font_variation_settings";
  public static final String C_FONT_WEIGHT = "font_weight";
  public static final String C_FONT_ITALIC = "font_italic";
  public static final String C_RESULT_CODE = "result_code";
  static IFontsDownloader sFontsDownloader;

  public static void injectFontsDownloader(@NonNull IFontsDownloader downloader) {
    sFontsDownloader = downloader;
  }

  public static int modeToMode(String mode) {
    int modeBits;
    if ("r".equals(mode)) {
      modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
    } else if ("w".equals(mode) || "wt".equals(mode)) {
      modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
          | ParcelFileDescriptor.MODE_CREATE
          | ParcelFileDescriptor.MODE_TRUNCATE;
    } else if ("wa".equals(mode)) {
      modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
          | ParcelFileDescriptor.MODE_CREATE
          | ParcelFileDescriptor.MODE_APPEND;
    } else if ("rw".equals(mode)) {
      modeBits = ParcelFileDescriptor.MODE_READ_WRITE
          | ParcelFileDescriptor.MODE_CREATE;
    } else if ("rwt".equals(mode)) {
      modeBits = ParcelFileDescriptor.MODE_READ_WRITE
          | ParcelFileDescriptor.MODE_CREATE
          | ParcelFileDescriptor.MODE_TRUNCATE;
    } else {
      throw new IllegalArgumentException("Invalid mode: " + mode);
    }
    return modeBits;
  }

  @Override
  public boolean onCreate() {
    return sFontsDownloader != null;
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, @Nullable String[] fields, @Nullable String selection,
                      @Nullable String[] args, @Nullable String orderFiled) {
    MatrixCursor cursor = new MatrixCursor(fields, 1);
    if (args == null || args.length == 0 || !checkFields(fields)) {
      cursor.addRow(new Object[]{
          0, 0L, 0, "", -1, -1, RESULT_CODE_MALFORMED_QUERY
      });
      return cursor;
    }
    String fontKey = args[0];
    int resultCode = RESULT_CODE_FONT_NOT_FOUND;
    if (sFontsDownloader.support(fontKey)) {
      IFontsDownloader.DownloadStatus downloadStatus = sFontsDownloader.queryStatus(fontKey);
      if (downloadStatus == IFontsDownloader.DownloadStatus.DOWNLOADED) {
        resultCode = RESULT_CODE_OK;
      } else if (downloadStatus == IFontsDownloader.DownloadStatus.DOWNLOADING) {
        resultCode = RESULT_CODE_FONT_UNAVAILABLE;
      } else {
        resultCode = RESULT_CODE_FONT_UNAVAILABLE;
        sFontsDownloader.startDownload(fontKey);
      }
    }
    cursor.addRow(new Object[]{
        sFontsDownloader.getCursorId(fontKey),
        sFontsDownloader.getFileId(fontKey),
        sFontsDownloader.getFontTTCIndex(fontKey),
        sFontsDownloader.getFontVariationSettings(fontKey),
        sFontsDownloader.getFontWeight(fontKey),
        sFontsDownloader.getFontItalic(fontKey), resultCode
    });
    return cursor;
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    return null;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
    return null;
  }

  @Override
  public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
    return 0;
  }

  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s,
                    @Nullable String[] strings) {
    return 0;
  }

  @Nullable
  @Override
  public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws
      FileNotFoundException {
    String fileId = uri.getLastPathSegment();
    if (!TextUtils.isEmpty(fileId)) {
      try {
        return ParcelFileDescriptor.fromFd(Integer.valueOf(fileId));
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
    throw new FileNotFoundException(uri.toString());
  }

  private boolean checkFields(String[] fields) {
    return fields != null && fields.length == 7
        && C_ID.equals(fields[0])
        && C_FILE_ID.equals(fields[1])
        && C_FONT_TTC_INDEX.equals(fields[2])
        && C_FONT_VARIATION_SETTINGS.equals(fields[3])
        && C_FONT_WEIGHT.equals(fields[4])
        && C_FONT_ITALIC.equals(fields[5])
        && C_RESULT_CODE.equals(fields[6]);
  }


}
