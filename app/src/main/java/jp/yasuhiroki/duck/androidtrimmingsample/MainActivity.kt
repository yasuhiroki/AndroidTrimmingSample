package jp.yasuhiroki.duck.androidtrimmingsample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.yalantis.ucrop.UCrop
import jp.yasuhiroki.duck.androidtrimmingsample.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val src: Uri = Uri.parse("https://avatars1.githubusercontent.com/u/3108110")
    private lateinit var dst: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dst = Uri.fromFile(File(cacheDir, "temp.png"))

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.button.setOnClickListener {
            binding.imageView.setImageBitmap(null)
            UCrop.of(src, dst)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(200, 200)
                .start(this)
        }
        binding.button2.setOnClickListener {
            binding.imageView.setImageBitmap(null)
            val intent = Intent(this, CustomTrimmingActivity::class.java)
            intent.putExtra("SRC_URI", src)
            intent.putExtra("DST_URI", dst)
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.imageView.setImageURI(dst)
    }
}
