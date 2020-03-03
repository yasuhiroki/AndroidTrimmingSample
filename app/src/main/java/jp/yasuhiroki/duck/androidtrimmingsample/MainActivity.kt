package jp.yasuhiroki.duck.androidtrimmingsample

import android.content.Intent
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.values
import androidx.databinding.DataBindingUtil
import com.yalantis.ucrop.UCrop
import jp.yasuhiroki.duck.androidtrimmingsample.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val src: Uri = Uri.parse("https://avatars1.githubusercontent.com/u/3108110")
    private lateinit var dst: Uri
    private var decoInfos: ArrayList<DecoInfo>? = null
    private var cropImageMatrix: FloatArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dst = Uri.fromFile(File(cacheDir, "temp.png"))

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.button.setOnClickListener {
            UCrop.of(src, dst)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(200, 200)
                .start(this)
        }
        binding.button2.setOnClickListener {
            val intent = Intent(this, CustomTrimmingActivity::class.java)
            intent.putExtra("SRC_URI", src)
                .putExtra("DST_URI", dst)
                .putExtra("MATRIX", cropImageMatrix)
                .putParcelableArrayListExtra("DATA", decoInfos)
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.imageView.setImageBitmap(null)
        binding.decoFrame.removeAllViews()

        binding.imageView.setImageURI(dst)

        cropImageMatrix = data?.getFloatArrayExtra("MATRIX")

        decoInfos = data?.getParcelableArrayListExtra("DATA")
        decoInfos?.forEach {
            val rateX: Float = binding.decoFrame.width.toFloat() / it.frameWidth
            val rateY: Float = binding.decoFrame.height.toFloat() / it.frameHeight

            val decoImage = ImageView(this, null)
            decoImage.setImageResource(R.mipmap.ic_launcher)
            decoImage.scaleType = ImageView.ScaleType.MATRIX

            val matrix = Matrix().apply { setValues(it.matrix) }
            matrix.postScale(rateX, rateY, 0f, 0f)
            decoImage.imageMatrix = matrix

            binding.decoFrame.addView(
                decoImage,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
    }
}

class DecoInfo : Parcelable {
    var marginLeft: Float
    var marginTop: Float
    var scale: Float
    var rect: Float
    var layoutWidth: Int
    var layoutHeight: Int

    var matrix: FloatArray

    var frameWidth: Int
    var frameHeight: Int

    constructor(v: ImageView, f: FrameLayout) {
        marginLeft = v.translationX
        marginTop = v.translationY
        scale = v.scaleX
        rect = v.rotation
        layoutWidth = v.layoutParams.width
        layoutHeight = v.layoutParams.height

        matrix = v.matrix.values()

        frameWidth = f.width
        frameHeight = f.height
    }

    constructor(i: Parcel) {
        marginLeft = i.readFloat()
        marginTop = i.readFloat()
        scale = i.readFloat()
        rect = i.readFloat()
        layoutWidth = i.readInt()
        layoutHeight = i.readInt()

        val m = FloatArray(9)
        i.readFloatArray(m)
        matrix = m

        frameWidth = i.readInt()
        frameHeight = i.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeFloat(marginLeft)
        dest.writeFloat(marginTop)
        dest.writeFloat(scale)
        dest.writeFloat(rect)
        dest.writeInt(layoutWidth)
        dest.writeInt(layoutHeight)

        dest.writeFloatArray(matrix)

        dest.writeInt(frameWidth)
        dest.writeInt(frameHeight)
    }

    companion object CREATOR : Parcelable.Creator<DecoInfo> {
        override fun createFromParcel(parcel: Parcel): DecoInfo {
            return DecoInfo(parcel)
        }

        override fun newArray(size: Int): Array<DecoInfo?> {
            return arrayOfNulls(size)
        }
    }
}
