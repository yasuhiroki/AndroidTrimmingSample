package jp.yasuhiroki.duck.androidtrimmingsample

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.yalantis.ucrop.UCrop
import jp.yasuhiroki.duck.androidtrimmingsample.databinding.ActivityCustomTrimmingBinding
import java.io.File

class CustomTrimmingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_trimming)
        val binding = DataBindingUtil.setContentView<ActivityCustomTrimmingBinding>(
            this,
            R.layout.activity_custom_trimming
        )

        val src = intent.getParcelableExtra<Uri>("SRC_URI")!!
        val dst = intent.getParcelableExtra<Uri>("DST_URI")!!
        binding.ucropView.cropImageView.setImageUri(src, dst)
    }
}
