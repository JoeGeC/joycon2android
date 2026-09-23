package com.joegec.joycon2android.update

import org.json.JSONArray
import org.json.JSONObject

class GitHubReleaseParser {

    fun parse(json: String): AvailableUpdate? {
        val release = JSONObject(json)
        val version = AppVersion.parse(release.optString(TAG_NAME)) ?: return null
        val apkUrl = release.optJSONArray(ASSETS)?.firstApkUrl() ?: return null
        return AvailableUpdate(version, ReleaseNotes.highlights(release.optString(BODY)), apkUrl)
    }

    private fun JSONArray.firstApkUrl(): String? = (0 until length())
        .mapNotNull { optJSONObject(it) }
        .firstOrNull { it.optString(NAME).endsWith(APK_SUFFIX, ignoreCase = true) }
        ?.optString(DOWNLOAD_URL)
        ?.takeIf { it.isNotEmpty() }

    private companion object {
        const val TAG_NAME = "tag_name"
        const val BODY = "body"
        const val ASSETS = "assets"
        const val NAME = "name"
        const val DOWNLOAD_URL = "browser_download_url"
        const val APK_SUFFIX = ".apk"
    }
}
