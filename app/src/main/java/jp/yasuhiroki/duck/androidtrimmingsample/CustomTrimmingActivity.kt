package jp.yasuhiroki.duck.androidtrimmingsample

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.callback.BitmapCropCallback
import jp.ogwork.gesturetransformableview.view.GestureTransformableImageView
import jp.yasuhiroki.duck.androidtrimmingsample.databinding.ActivityCustomTrimmingBinding


class CustomTrimmingActivity : AppCompatActivity() {
    lateinit var binding: ActivityCustomTrimmingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_trimming)
        binding = DataBindingUtil.setContentView<ActivityCustomTrimmingBinding>(
            this,
            R.layout.activity_custom_trimming
        )

        val src = intent.getParcelableExtra<Uri>("SRC_URI")!!
        val dst = intent.getParcelableExtra<Uri>("DST_URI")!!
        binding.ucropView.cropImageView.setImageUri(src, dst)

        binding.buttonCrop.setOnClickListener {
            binding.ucropView.cropImageView.cropAndSaveImage(
                Bitmap.CompressFormat.PNG,
                90,
                object : BitmapCropCallback {
                    override fun onBitmapCropped(
                        resultUri: Uri,
                        offsetX: Int,
                        offsetY: Int,
                        imageWidth: Int,
                        imageHeight: Int
                    ) {
                        setResultUri(
                            resultUri,
                            binding.ucropView.cropImageView.getTargetAspectRatio(),
                            offsetX,
                            offsetY,
                            imageWidth,
                            imageHeight
                        )
                        finish()
                    }

                    override fun onCropFailure(t: Throwable) {
                        finish()
                    }
                })
        }

        binding.buttonDeco.setOnClickListener {
            val imageView = GestureTransformableImageView(this, null)
            imageView.setImageResource(R.mipmap.ic_launcher)
            val lp = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            binding.decoFrame.addView(imageView, lp)
        }
    }


    private fun setResultUri(
        uri: Uri?,
        resultAspectRatio: Float,
        offsetX: Int,
        offsetY: Int,
        imageWidth: Int,
        imageHeight: Int
    ) {
        val decoInfos: ArrayList<DecoInfo> =
            ArrayList(binding.decoFrame.children.map {
                val decoImageView = it as GestureTransformableImageView
                DecoInfo(decoImageView)
            }.toList())

        setResult(
            Activity.RESULT_OK, Intent()
                .putExtra(UCrop.EXTRA_OUTPUT_URI, uri)
                .putExtra(UCrop.EXTRA_OUTPUT_CROP_ASPECT_RATIO, resultAspectRatio)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_WIDTH, imageWidth)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_HEIGHT, imageHeight)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_X, offsetX)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_Y, offsetY)
                .putParcelableArrayListExtra("DATA", decoInfos)
        )
    }
}
