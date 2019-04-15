package io.wookey.wallet.support.extensions

import android.content.Context
import java.io.File
import java.io.IOException

/**
 * 获取应用文件目录
 *
 * 应用程序文件目录("/data/data/<包名>/files")
 */
val Context.fileDirPath: String
    get() = filesDir.absolutePath

/**
 * 获取应用缓存目录
 *
 * 应用程序缓存目录("/data/data/<包名>/cache")
 */
val Context.cacheDirPath: String
    get() = cacheDir.absolutePath

/**
 * 获取应用外置文件目录
 *
 * 应用程序文件目录("/Android/data/<包名>/files")
 */
val Context.externalFileDirPath: String
    get() = getExternalFilesDir("")?.absolutePath ?: ""

/**
 * 获取应用外置缓存目录
 *
 * 应用程序缓存目录("/Android/data/<包名>/cache")
 */
val Context.externalCacheDirPath: String
    get() = externalCacheDir?.absolutePath ?: ""

/**
 * 通过文件路径获取File对象
 *
 * @param filePath
 * @return nullable
 */
fun getFileByPath(filePath: String): File? = if (filePath.isBlank()) null else File(filePath)

/**
 * 判断文件是否存在
 *
 */
val File.isFileExists: Boolean get() = exists() && isFile

/**
 * 判断文件是否存在
 *
 * @param filePath
 */
fun isFileExists(filePath: String): Boolean {
    val file = getFileByPath(filePath)
    return file?.isFileExists ?: false
}

/**
 * 判断文件夹是否存在
 *
 */
val File.isDirExists: Boolean get() = exists() && isDirectory

/**
 * 判断文件夹是否存在
 *
 * @param filePath
 */
fun isDirExists(filePath: String): Boolean {
    val file = getFileByPath(filePath)
    return file?.isDirExists ?: false
}

/**
 * 判断目录是否存在，不存在则判断是否创建成功
 *
 * @return true 文件夹存在或者创建成功  false 文件夹不存在或者创建失败
 */
fun File.createOrExistsDir(): Boolean =
// 如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
    if (exists()) isDirectory else mkdirs()

/**
 * 判断目录是否存在，不存在则判断是否创建成功
 *
 * @param filePath
 * @return true 文件夹存在或者创建成功  false 路径无效、文件夹不存在或者创建失败
 */
fun createOrExistsDir(filePath: String): Boolean {
    val file = getFileByPath(filePath)
    return file?.createOrExistsDir() ?: false
}

/**
 * 判断文件是否存在，不存在则判断是否创建成功
 *
 * @return true 文件存在或者创建成功  false 文件不存在或者创建失败
 */
fun File.createOrExistsFile(): Boolean {
    if (exists()) return isFile
    if (parentFile?.createOrExistsDir() != true) return false

    return try {
        createNewFile()
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}

/**
 * 判断文件是否存在，不存在则判断是否创建成功
 *
 * @param filePath
 * @return true 文件存在或者创建成功  false 路径无效、文件不存在或者创建失败
 */
fun createOrExistsFile(filePath: String): Boolean {
    val file = getFileByPath(filePath)
    return file?.createOrExistsFile() ?: false
}

