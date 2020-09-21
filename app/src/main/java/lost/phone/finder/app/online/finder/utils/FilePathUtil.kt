package lost.phone.finder.app.online.finder.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object FilePathUtil {
    private val context: Context? = null
    fun getPath(context: Context, uri: Uri): String? {
        var context = context
        context = context
        return try {
            var uri2: Uri? = null
            if (!DocumentsContract.isDocumentUri(context, uri)) {
                if ("content".equals(uri.scheme, ignoreCase = true)) {
                    return if (isGooglePhotosUri(uri)) {
                        uri.lastPathSegment
                    } else getDataColumn(
                        context,
                        uri,
                        null as String?,
                        null as Array<String>?
                    )
                }
            } else if (isExternalStorageDocument(uri)) {
                val split =
                    DocumentsContract.getDocumentId(uri).split(":".toRegex()).toTypedArray()
                if ("primary".equals(split[0], ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory()
                        .toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                return getDataColumn(
                    context,
                    ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(DocumentsContract.getDocumentId(uri)).toLong()
                    ),
                    null as String?,
                    null as Array<String>?
                )
            } else if (isMediaDocument(uri)) {
                val split2 =
                    DocumentsContract.getDocumentId(uri).split(":".toRegex()).toTypedArray()
                val str = split2[0]
                if ("image" == str) {
                    uri2 = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == str) {
                    uri2 = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == str) {
                    uri2 = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                return getDataColumn(
                    context,
                    uri2,
                    "_id=?",
                    arrayOf(split2[1])
                )
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            getFilePathFromURI(context, uri)
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0039  */ /* Code decompiled incorrectly, please refer to instructions dump. */
    private fun getDataColumn(
        r7: Context,
        r8: Uri?,
        r9: String?,
        r10: Array<String>?
    ): String {
        /*
            r0 = 1
            java.lang.String[] r3 = new java.lang.String[r0]
            java.lang.String r0 = "_data"
            r1 = 0
            r3[r1] = r0
            r0 = 0
            android.content.ContentResolver r1 = r7.getContentResolver()     // Catch:{ all -> 0x0035 }
            r6 = 0
            r2 = r8
            r4 = r9
            r5 = r10
            android.database.Cursor r7 = r1.query(r2, r3, r4, r5, r6)     // Catch:{ all -> 0x0035 }
            if (r7 == 0) goto L_0x002f
            boolean r8 = r7.moveToFirst()     // Catch:{ all -> 0x002d }
            if (r8 == 0) goto L_0x002f
            java.lang.String r8 = "_data"
            int r8 = r7.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x002d }
            java.lang.String r8 = r7.getString(r8)     // Catch:{ all -> 0x002d }
            if (r7 == 0) goto L_0x002c
            r7.close()
        L_0x002c:
            return r8
        L_0x002d:
            r8 = move-exception
            goto L_0x0037
        L_0x002f:
            if (r7 == 0) goto L_0x0034
            r7.close()
        L_0x0034:
            return r0
        L_0x0035:
            r8 = move-exception
            r7 = r0
        L_0x0037:
            if (r7 == 0) goto L_0x003c
            r7.close()
        L_0x003c:
            throw r8
        */
        throw UnsupportedOperationException("Method not decompiled: com.utils.FilePath.getDataColumn(android.content.Context, android.net.Uri, java.lang.String, java.lang.String[]):java.lang.String")
    }

    private fun getFilePathFromURI(
        context: Context,
        uri: Uri
    ): String? {
        val fileName = getFileName1(uri)
        if (TextUtils.isEmpty(fileName)) {
            return null
        }
        val path = Environment.getExternalStorageDirectory().path
        val file = File(path + File.separator + fileName)
        copy(context, uri, file)
        return file.absolutePath
    }


    fun getFileName(r3: Uri?): String {

        throw UnsupportedOperationException("Method not decompiled: com.utils.FilePath.getFileName(android.net.Uri):java.lang.String")
    }

    fun getFileName1(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor =
                context!!.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    fun copy(
        context: Context,
        uri: Uri?,
        file: File?
    ) {
        try {
            val openInputStream =
                context.contentResolver.openInputStream(uri!!)
            if (openInputStream != null) {
                val fileOutputStream = FileOutputStream(file)
                IOUtils.copy(
                    openInputStream,
                    fileOutputStream as OutputStream
                )
                openInputStream.close()
                fileOutputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
}