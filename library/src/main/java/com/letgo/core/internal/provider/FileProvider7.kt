package com.letgo.core.internal.provider

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

class FileProvider7 : FileProvider() {

    companion object {
        fun getUriForFile(context: Context, file: File): Uri? {
            var fileUri: Uri? = null
            if (Build.VERSION.SDK_INT >= 24) {
                fileUri =
                    getUriForFile24(
                        context,
                        file
                    )
            } else {
                fileUri = Uri.fromFile(file)
            }
            return fileUri
        }


        fun getUriForFile24(context: Context, file: File): Uri {
            return getUriForFile(
                context,
                context.packageName + ".android7.fileProvider",
                file
            )
        }


        fun setIntentData(context: Context, intent: Intent, file: File, writeAble: Boolean):Uri? {
            if (Build.VERSION.SDK_INT >= 24) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (writeAble) {
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
            }
            return getUriForFile(
                context,
                file
            )
        }


        fun grantPermissions(context: Context, intent: Intent, uri: Uri, writeAble: Boolean) {
            var flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            if (writeAble) {
                flag = flag or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            }
            intent.addFlags(flag)
            val resInfoList = context.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(packageName, uri, flag)
            }
        }
    }


}