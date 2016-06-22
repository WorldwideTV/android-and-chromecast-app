/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved.
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

package com.wwtv.player.browser;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.common.images.WebImage;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoProvider {

	private static final String TAG = "VideoProvider";
	private static final String TAG_CHANNELS = "channels";

	private static final String TAG_COUNTRIES = "countries";

	private static final String TAG_THUMB = "thumbnail";
	private static final String TAG_TITLE = "title";

	private static final String TAG_URL = "url";

	public static final String KEY_DESCRIPTION = "description";

	private static List<MediaInfo> mediaList;

	private JSONObject parseUrl(String urlString) {
		InputStream is = null;
		try {
			java.net.URL url = new java.net.URL(urlString);
			URLConnection urlConnection = url.openConnection();
			is = new BufferedInputStream(urlConnection.getInputStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "iso-8859-1"), 1024);
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			String json = sb.toString();
			return new JSONObject(json);
		} catch (Exception e) {
			Log.d(TAG, "Failed to parse the json for media list", e);
			return null;
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	static List<MediaInfo> buildMedia(String url) throws JSONException {

		if (null != mediaList) {
			return mediaList;
		}
//		Map<String, String> urlPrefixMap = new HashMap<>();
		mediaList = new ArrayList<>();
		JSONObject jsonObj = new VideoProvider().parseUrl(url);
		JSONArray countries = jsonObj.getJSONArray(TAG_COUNTRIES);
		if (null != countries) {
			for (int i = 0; i < countries.length(); i++) {

				JSONObject category = countries.getJSONObject(i);

				category.getString(TAG_TITLE);
				JSONArray channels = category.getJSONArray(TAG_CHANNELS);
				if (null != channels) {
					for (int j = 0; j < channels.length(); j++) {
						JSONObject video = channels.getJSONObject(j);

						String imageUrl = video.getString(TAG_THUMB);
						String bigImageUrl = video.getString(TAG_THUMB);

						String title = video.getString(TAG_TITLE);
						String videoUrl = video.getString(TAG_URL);

//						List<MediaTrack> tracks = null;
//						if (video.has(TAG_TRACKS)) {
//							JSONArray tracksArray = video.getJSONArray(TAG_TRACKS);
//							if (tracksArray != null) {
//								tracks = new ArrayList<>();
//								for (int k = 0; k < tracksArray.length(); k++) {
//									JSONObject track = tracksArray.getJSONObject(k);
//									tracks.add(buildTrack(track.getLong(TAG_TRACK_ID),
//										track.getString(TAG_TRACK_TYPE),
//										track.getString(TAG_TRACK_SUBTYPE),
//										urlPrefixMap.get(TAG_TRACKS) + track
//											.getString(TAG_TRACK_CONTENT_ID),
//										track.getString(TAG_TRACK_NAME),
//										track.getString(TAG_TRACK_LANGUAGE)
//									));
//								}
//							}
//						}
//						imageUrl = "https://raw.githubusercontent.com/WorldwideTV/TVChannels/master/images/bbc1.jpg";
//						videoUrl = "http://rtp-pull-live.hls.adaptive.level3.net/liverepeater/smil:rtp1.smil/playlist.m3u8";
						MediaInfo mediaInfo = buildMediaInfo(title, videoUrl, imageUrl, bigImageUrl);
						mediaList.add(mediaInfo);
					}
				}
			}
		}
		return mediaList;
	}

	private static MediaInfo buildMediaInfo(String title, String url, String imgUrl, String bigImageUrl) {
		MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

		movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
		movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
		movieMetadata.addImage(new WebImage(Uri.parse(bigImageUrl)));
//		JSONObject jsonObj = null;
//		try {
//			jsonObj = new JSONObject();
////			jsonObj.put(KEY_DESCRIPTION, subTitle);
//		} catch (JSONException e) {
//			Log.e(TAG, "Failed to add description to the json object", e);
//		}

		String mimeType = "videos/mp4";
		return new MediaInfo.Builder(url)
			.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
			.setContentType(mimeType)
			.setMetadata(movieMetadata)
//			.setMediaTracks(tracks)
//			.setStreamDuration(duration * 1000)
//			.setCustomData(jsonObj)
			.build();
	}

	private static MediaTrack buildTrack(long id, String type, String subType, String contentId,
	                                     String name, String language) {
		int trackType = MediaTrack.TYPE_UNKNOWN;
		if ("text".equals(type)) {
			trackType = MediaTrack.TYPE_TEXT;
		} else if ("video".equals(type)) {
			trackType = MediaTrack.TYPE_VIDEO;
		} else if ("audio".equals(type)) {
			trackType = MediaTrack.TYPE_AUDIO;
		}

//		int trackSubType = MediaTrack.SUBTYPE_NONE;
//		if (subType != null) {
//			if ("captions".equals(type)) {
//				trackSubType = MediaTrack.SUBTYPE_CAPTIONS;
//			} else if ("subtitle".equals(type)) {
//				trackSubType = MediaTrack.SUBTYPE_SUBTITLES;
//			}
//		}

		return new MediaTrack.Builder(id, trackType)
			.setName(name)
//			.setSubtype(trackSubType)
			.setContentId(contentId)
			.setLanguage(language).build();
	}
}


