package live.videosdk.android.hlsdemo.common.reactions

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import java.util.*

class ZeroGravityAnimation {
    private var mOriginationDirection = DirectionGenerator.Direction.RANDOM
    private var mDestinationDirection = DirectionGenerator.Direction.RANDOM
    private var mDuration = RANDOM_DURATION
    private var mCount = 1
    private var drawable: Drawable? = null
    private var mScalingFactor = 1f
    private var mAnimationListener: Animation.AnimationListener? = null

    /**
     * Sets the original direction. The animation will originate from the given direction.
     */
    fun setOriginationDirection(direction: DirectionGenerator.Direction): ZeroGravityAnimation {
        mOriginationDirection = direction
        return this
    }

    /**
     * Sets the animation destination direction. The translate animation will proceed towards the given direction.
     */
    fun setDestinationDirection(direction: DirectionGenerator.Direction): ZeroGravityAnimation {
        mDestinationDirection = direction
        return this
    }

    /**
     * Sets the time duration in millseconds for animation to proceed.
     */
    fun setDuration(duration: Int): ZeroGravityAnimation {
        mDuration = duration
        return this
    }

    /**
     * Sets the image reference for drawing the image
     */
    fun setImage(drawable: Drawable?): ZeroGravityAnimation {
        this.drawable = drawable
        return this
    }

    /**
     * Sets the image scaling value.
     */
    fun setScalingFactor(scale: Float): ZeroGravityAnimation {
        mScalingFactor = scale
        return this
    }

    fun setAnimationListener(listener: Animation.AnimationListener?): ZeroGravityAnimation {
        mAnimationListener = listener
        return this
    }

    fun setCount(count: Int): ZeroGravityAnimation {
        mCount = count
        return this
    }
    /**
     * Starts the Zero gravity animation by creating an OTT and attach it to th given ViewGroup
     */
    /**
     * Takes the content view as view parent for laying the animation objects and starts the animation.
     *
     * @param activity - activity on which the zero gravity animation should take place.
     */
    @JvmOverloads
    fun play(activity: Activity?, ottParent: ViewGroup? = null) {
        val generator = DirectionGenerator()
        if (mCount > 0) {
            for (i in 0 until mCount) {
                val origin =
                    if (mOriginationDirection === DirectionGenerator.Direction.RANDOM) generator.randomDirection else mOriginationDirection
                val destination =
                    if (mDestinationDirection === DirectionGenerator.Direction.RANDOM) generator.getRandomDirection(
                        origin
                    ) else mDestinationDirection
                val startingPoints = generator.getPointsInDirection(activity!!, origin)
                val endPoints = generator.getPointsInDirection(activity, destination)
                val bitmap = drawableToBitmap(drawable)
                val scaledBitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * mScalingFactor).toInt(),
                    (bitmap.height * mScalingFactor).toInt(),
                    false
                )
                when (origin) {
                    DirectionGenerator.Direction.LEFT -> startingPoints[0] -= scaledBitmap.width
                    DirectionGenerator.Direction.RIGHT -> startingPoints[0] += scaledBitmap.width
                    DirectionGenerator.Direction.TOP -> startingPoints[1] -= scaledBitmap.height
                    DirectionGenerator.Direction.BOTTOM -> startingPoints[1] += scaledBitmap.height
                    else -> {}
                }
                when (destination) {
                    DirectionGenerator.Direction.LEFT -> endPoints[0] -= scaledBitmap.width
                    DirectionGenerator.Direction.RIGHT -> endPoints[0] += scaledBitmap.width
                    DirectionGenerator.Direction.TOP -> endPoints[1] -= scaledBitmap.height
                    DirectionGenerator.Direction.BOTTOM -> endPoints[1] += scaledBitmap.height
                    else -> {}
                }
                val layer = OverTheTopLayer()
                val ottLayout = layer.with(activity)
                    .scale(mScalingFactor)
                    .attachTo(ottParent)
                    .setBitmap(scaledBitmap, startingPoints)
                    .create()
                when (origin) {
                    DirectionGenerator.Direction.LEFT -> {}
                    else -> {}
                }
                val deltaX = endPoints[0] - startingPoints[0]
                val deltaY = endPoints[1] - startingPoints[1]
                var duration = mDuration
                if (duration == RANDOM_DURATION) {
                    duration = generateRandomBetween(1, 10)
                }
                val animation = TranslateAnimation(
                    0F,
                    deltaX.toFloat(), 0F, deltaY.toFloat()
                )
                animation.duration = duration.toLong()
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {
                        if (i == 0) {
                            if (mAnimationListener != null) {
                                mAnimationListener!!.onAnimationStart(animation)
                            }
                        }
                    }

                    override fun onAnimationEnd(animation: Animation) {
                        layer.destroy()
                        if (i == mCount - 1) {
                            if (mAnimationListener != null) {
                                mAnimationListener!!.onAnimationEnd(animation)
                            }
                        }
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
                layer.applyAnimation(animation)
            }
        } else {
            Log.e(
                ZeroGravityAnimation::class.java.simpleName,
                "Count was not provided, animation was not started"
            )
        }
    }

    companion object {
        private const val RANDOM_DURATION = -1
        fun drawableToBitmap(drawable: Drawable?): Bitmap {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }
            var width = drawable!!.intrinsicWidth
            width = if (width > 0) width else 1
            var height = drawable.intrinsicHeight
            height = if (height > 0) height else 1
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }

        /**
         * Generates the random between two given integers.
         */
        fun generateRandomBetween(start: Int, end: Int): Int {
            val random = Random()
            var rand = random.nextInt(Int.MAX_VALUE - 1) % end
            if (rand < start) {
                rand = start
            }
            return rand
        }
    }
}