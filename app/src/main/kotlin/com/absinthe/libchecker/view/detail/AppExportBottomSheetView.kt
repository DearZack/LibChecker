package com.absinthe.libchecker.view.detail

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import com.absinthe.libchecker.R
import com.absinthe.libchecker.utils.extensions.dp
import com.absinthe.libchecker.utils.extensions.getDimensionPixelSize
import com.absinthe.libchecker.utils.showLongToast
import com.absinthe.libchecker.view.app.IHeaderView
import com.absinthe.libraries.utils.view.BottomSheetHeaderView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.chip.Chip
import java.io.FileInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.core.os.FileUtils

class AppExportBottomSheetView(context: Context, val packageInfo: PackageInfo) :
  LinearLayout(context),
  IHeaderView {



  private val header = BottomSheetHeaderView(context).apply {
    layoutParams =
      LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    title.text = context.getString(R.string.lib_detail_app_export_title)
  }

  private val chip = Chip(context).apply {
    isClickable = false
    layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    val padding = 16.dp
    setPadding(padding, padding, padding, padding * 3)
    text = context.getString(R.string.lib_detail_app_export_hint)
    setChipIconResource(R.drawable.ic_app_export)
    setOnClickListener {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appExport(context)
      }
    }
  }

  private val loadingView = LottieAnimationView(context).apply {
    val size = context.getDimensionPixelSize(R.dimen.lottie_anim_size)
    layoutParams = FrameLayout.LayoutParams(size, size).also {
      it.gravity = Gravity.CENTER
    }
    imageAssetsFolder = "/"
    repeatCount = LottieDrawable.INFINITE
    setAnimation("anim/anim_llujkxg.json")

  }


  @RequiresApi(Build.VERSION_CODES.Q)
  private fun appExport(context: Context) {
    loadingView.visibility = VISIBLE
    loadingView.playAnimation()
    CoroutineScope(Dispatchers.IO).launch {
      val sourceFileInputStream = FileInputStream(packageInfo.applicationInfo?.sourceDir)
      val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/app_export"
      val name = "${packageInfo.packageName}_${packageInfo.versionName}.apk"
      val destPath = "${relativePath}/${name}"
      val resolver: ContentResolver = context.contentResolver
      val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.android.package-archive")
        put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
      }
      val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
      uri?.let {
        val outputStream = resolver.openOutputStream(it)
        outputStream?.let {
          outputStream.use {
            FileUtils.copy(sourceFileInputStream, outputStream)
            withContext(Dispatchers.Main) {
              context.showLongToast(String.format(context.getString(R.string.lib_detail_app_export_success), destPath))
            }
          }
        }
      }
    }
  }

  init {
    orientation = VERTICAL
    gravity = Gravity.CENTER_HORIZONTAL
    val padding = 16.dp
    setPadding(padding, padding, padding, 0)
    addView(header)
    addView(chip)
    addView(loadingView)
    loadingView.visibility = INVISIBLE
    loadingView.pauseAnimation()
  }

  override fun getHeaderView(): BottomSheetHeaderView {
    return header
  }
}
