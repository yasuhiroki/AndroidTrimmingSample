package jp.yasuhiroki.duck.androidtrimmingsample

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.values
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.view.TransformImageView
import jp.ogwork.gesturetransformableview.view.GestureTransformableImageView
import jp.yasuhiroki.duck.androidtrimmingsample.databinding.ActivityCustomTrimmingBinding


class CustomTrimmingActivity : AppCompatActivity() {
    lateinit var binding: ActivityCustomTrimmingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_trimming)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_custom_trimming)

        binding.ucropView.cropImageView.setTransformImageListener(object :
            TransformImageView.TransformImageListener {
            override fun onRotate(currentAngle: Float) {
                // nothing
            }

            override fun onLoadComplete() {
                binding.blocking.visibility = View.GONE
            }

            override fun onScale(currentScale: Float) {
                // nothing
            }

            override fun onLoadFailure(e: Exception) {
                // nothing
            }
        })

        // This is workaround for update init Matrix.
        // Overwrite ucropView's listener to change overlayView aspect,
        // but it is not necessary because overlayView is fixed square layout in this Activity.
        // Be careful, following logic implementation is overwrote when UCropView#resetCropImageView() method is called.
        binding.ucropView.cropImageView.setCropBoundsChangeListener {
            val imageMatrixValues = intent.getFloatArrayExtra("MATRIX")
            if (imageMatrixValues != null) {
                val matrix = Matrix().apply { setValues(imageMatrixValues) }
                binding.ucropView.cropImageView.imageMatrix = matrix
            }
        }

        val src = intent.getParcelableExtra<Uri>("SRC_URI")!!
        val dst = intent.getParcelableExtra<Uri>("DST_URI")!!
        binding.ucropView.cropImageView.setImageUri(src, dst)

        // Square Crop
        binding.ucropView.overlayView.setTargetAspectRatio(1f)

        val decoInfos: ArrayList<DecoInfo>? = intent.getParcelableArrayListExtra("DATA")
        val onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener =
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    decoInfos?.forEach {
                        val decoImage = GestureTransformableImageView(baseContext, null)
                        decoImage.setImageResource(R.mipmap.ic_launcher)
                        decoImage.rotation = it.rect
                        decoImage.setScale(it.scale)
                        decoImage.setAngle(it.rect)
                        decoImage.translationX = it.marginLeft
                        decoImage.translationY = it.marginTop

                        binding.decoFrame.addView(
                            decoImage,
                            FrameLayout.LayoutParams(it.layoutWidth, it.layoutHeight)
                        )
                    }
                    binding.decoFrame.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        binding.decoFrame.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)

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
                            binding.ucropView.cropImageView.targetAspectRatio,
                            offsetX,
                            offsetY,
                            imageWidth,
                            imageHeight
                        )

                        finish()
                    }

                    override fun onCropFailure(t: Throwable) {
                        t.printStackTrace()
                        finish()
                    }
                })
        }

        binding.buttonCut.setOnClickListener {

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
                        val croppedBitmap = BitmapFactory.decodeFile(resultUri.path)
                        val canvasBitmap = Bitmap.createBitmap(
                            croppedBitmap.width,
                            croppedBitmap.height,
                            Bitmap.Config.ARGB_8888
                        )
                        val canvas = Canvas(canvasBitmap)
                        canvas.drawBitmap(croppedBitmap, 0f, 0f, null)

                        val imageViewWidth = binding.decoFrame.width
                        val imageViewHeight = binding.decoFrame.height
                        val viewScaleX = croppedBitmap.width.toFloat() / imageViewWidth
                        val viewScaleY = croppedBitmap.height.toFloat() / imageViewHeight

                        binding.decoFrame.children.forEach {
                            val decoImageView = it as GestureTransformableImageView

                            val bitmap = Bitmap.createBitmap(
                                croppedBitmap.width,
                                croppedBitmap.height,
                                Bitmap.Config.ARGB_8888
                            )
                            val decoCanvas = Canvas(bitmap)
                            val matrix = Matrix().apply { setValues(decoImageView.matrix.values()) }
                            matrix.postScale(viewScaleX, viewScaleY)
                            decoCanvas.setMatrix(matrix)
                            decoImageView.draw(decoCanvas)

                            canvas.drawBitmap(bitmap, 0f, 0f, null)
                        }

                        binding.blendImage.setImageBitmap(canvasBitmap)

                        binding.ucropView.cropImageView.setImageUri(src, dst)
                    }

                    override fun onCropFailure(t: Throwable) {
                        t.printStackTrace()
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
                DecoInfo(decoImageView, binding.decoFrame)
            }.toList())

        val cropMatrixValues = binding.ucropView.cropImageView.imageMatrix.values()

        setResult(
            Activity.RESULT_OK, Intent()
                .putExtra(UCrop.EXTRA_OUTPUT_URI, uri)
                .putExtra(UCrop.EXTRA_OUTPUT_CROP_ASPECT_RATIO, resultAspectRatio)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_WIDTH, imageWidth)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_HEIGHT, imageHeight)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_X, offsetX)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_Y, offsetY)
                .putExtra("MATRIX", cropMatrixValues)
                .putParcelableArrayListExtra("DATA", decoInfos)
        )
    }
}
