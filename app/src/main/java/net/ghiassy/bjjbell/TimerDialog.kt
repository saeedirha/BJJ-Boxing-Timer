package net.ghiassy.bjjbell

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment

class TimerDialog : AppCompatDialogFragment() {

    var mListener: MyDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        var builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.layout_time_dialog, null)

        val mTimerMin: NumberPicker? = view?.findViewById(R.id.TimerMin)
        val mTimerSecond: NumberPicker? = view?.findViewById(R.id.TimerSec)
        mTimerMin?.maxValue = 99
        mTimerMin?.minValue = 0
        mTimerSecond?.maxValue = 59
        mTimerSecond?.minValue = 0

        builder.setView(view)
        builder.setTitle("Please Select Time:")
        builder.setIcon(R.drawable.ic_timer_icon)
        builder.setPositiveButton("Set",
            DialogInterface.OnClickListener { dialog, id ->

                mListener?.applyChange(mTimerMin!!.value, mTimerSecond!!.value)
            })
        builder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, id ->
                this.dismiss()
            })

        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as MyDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement the listener")
        }
    }

}