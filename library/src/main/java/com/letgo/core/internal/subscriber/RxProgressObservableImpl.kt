package com.letgo.core.internal.subscriber

import android.content.Context
import android.content.DialogInterface

class RxProgressObservableImpl(msg: String, cancelable: Boolean = true) :
    RxProgressObservable(msg, cancelable) {

    override fun show(context: Context, msg: String) {
        if (cancelable) {
            ProgressDialogUtil.showLoadingDialog(context,
                msg,
                cancelable,
                { cancelObservable.onNext(true) },
                { cancelObservable.onComplete() })
        } else {
            ProgressDialogUtil.showLoadingDialog(context,
                msg,
                cancelable,
                null,
                { cancelObservable.onComplete() })
        }
    }

    override fun isShowing(): Boolean {
        return ProgressDialogUtil.isShowing()
    }

    override fun dismiss() {
        ProgressDialogUtil.dismissLoadingDialog()
    }
}