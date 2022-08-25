package com.quiz.pride.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Base64
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.quiz.pride.R

private fun initUrlGlide(context: Context, url: String?) =
    Glide.with(context)
        .setDefaultRequestOptions(RequestOptions().timeout(30000))
        .load(url)
        .error(getCircularProgressDrawable(context))
        .addListener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                log("Glide", "onLoadFailed", e)
                e?.logRootCauses("GLIDE")
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                log("Glide", "onResourceReady")
                return false
            }

        })

fun glideLoadURL(context: Context, url: String?, where: ImageView) {
    initUrlGlide(context, url)
        .placeholder(getCircularProgressDrawable(context))
        .into(where)
}

fun glideLoadBase64(context: Context, imageBytes: String?, where: ImageView) {
    val imageByteArray: ByteArray = Base64.decode(imageBytes, Base64.DEFAULT)

    Glide.with(context)
        .asBitmap()
        .load(imageByteArray)
        .transition(BitmapTransitionOptions.withCrossFade())
        .into(where)
}
fun glideCircleLoadBase64(context: Context, imageBytes: String?, where: ImageView) {
    val imageByteArray = convertImageToByteArray(imageBytes)

    Glide.with(context)
        .asBitmap()
        .apply(RequestOptions.circleCropTransform())
        .load(imageByteArray)
        .transition(BitmapTransitionOptions.withCrossFade())
        .into(where)
}

fun convertImageToByteArray(imageBytes: String?): ByteArray {
    return if(imageBytes.isNullOrEmpty() || imageBytes == Constants.DEFAULT_IMAGE_UPLOAD_TO_SERVER) {
        Base64.decode(Constants.DEFAULT_IMAGE_TO_SHOW, Base64.DEFAULT)
    } else {
        try {
            Base64.decode(imageBytes, Base64.DEFAULT)
        } catch (exception: Exception) {
            Base64.decode(Constants.DEFAULT_IMAGE_TO_SHOW, Base64.DEFAULT)
        }
    }
}

fun glideLoadingGif(context: Context, where: ImageView) {

    Glide.with(context)
        .asGif()
        .load(R.drawable.image_loading)
        .into(where)
}