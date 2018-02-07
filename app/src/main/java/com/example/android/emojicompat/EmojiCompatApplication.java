/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.emojicompat;

import android.app.Application;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.FontRequestEmojiCompatConfig;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;
import android.support.v4.provider.FontRequest;
import android.util.Log;

import com.lsjwzh.fonts.FontsProvider;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.GET_SIGNATURES;


/**
 * This application uses EmojiCompat.
 */
public class EmojiCompatApplication extends Application {

  public static final String NOTO_COLOR_EMOJI_COMPAT_TTF = "NotoColorEmojiCompat.ttf";
  private static final String TAG = "EmojiCompatApplication";
  /**
   * Change this to {@code false} when you want to use the downloadable Emoji font.
   */
  private static final boolean USE_BUNDLED_EMOJI = false;

  @Override
  public void onCreate() {
    super.onCreate();
    FontsProvider.injectFontsDownloader(new FontDownloader(this));
    final EmojiCompat.Config config;
    if (USE_BUNDLED_EMOJI) {
      // Use the bundled font for EmojiCompat
      config = new BundledEmojiCompatConfig(getApplicationContext());
    } else {
      List<List<byte[]>> certs = new ArrayList<>();
      ArrayList<byte[]> c = new ArrayList<>();
      try {
        c.add(getPackageManager().getPackageInfo(getPackageName(), GET_SIGNATURES).signatures[0]
            .toByteArray());
      } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
      }
      certs.add(c);
      // Use a downloadable font for EmojiCompat
      final FontRequest fontRequest = new FontRequest(
          "me.contentprovidertest",
          "com.example.android.emojicompat",
          NOTO_COLOR_EMOJI_COMPAT_TTF, certs);
      config = new FontRequestEmojiCompatConfig(getApplicationContext(), fontRequest)
          .setReplaceAll(true)
          .registerInitCallback(new EmojiCompat.InitCallback() {
            @Override
            public void onInitialized() {
              Log.i(TAG, "EmojiCompat initialized");
            }

            @Override
            public void onFailed(@Nullable Throwable throwable) {
              Log.e(TAG, "EmojiCompat initialization failed", throwable);
            }
          });
    }
    EmojiCompat.init(config);
  }

}
