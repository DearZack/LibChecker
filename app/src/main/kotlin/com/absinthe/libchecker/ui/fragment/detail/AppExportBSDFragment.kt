package com.absinthe.libchecker.ui.fragment.detail


import android.content.pm.PackageInfo
import androidx.core.os.BundleCompat
import com.absinthe.libchecker.features.applist.detail.ui.EXTRA_PACKAGE_INFO
import com.absinthe.libchecker.view.detail.AppExportBottomSheetView
import com.absinthe.libraries.utils.base.BaseBottomSheetViewDialogFragment
import com.absinthe.libraries.utils.view.BottomSheetHeaderView


class AppExportBSDFragment :
  BaseBottomSheetViewDialogFragment<AppExportBottomSheetView>() {

  private val packageInfo by lazy {
    BundleCompat.getParcelable(requireArguments(), EXTRA_PACKAGE_INFO, PackageInfo::class.java)!!
  }

  override fun initRootView(): AppExportBottomSheetView =
    AppExportBottomSheetView(requireContext(), packageInfo)

  override fun getHeaderView(): BottomSheetHeaderView = root.getHeaderView()

  override fun init() {

  }

  override fun onDestroyView() {
    super.onDestroyView()
  }
}
