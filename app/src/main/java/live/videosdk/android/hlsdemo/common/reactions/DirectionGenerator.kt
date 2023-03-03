package live.videosdk.android.hlsdemo.common.reactions

import android.app.Activity
import java.util.*

class DirectionGenerator {
    enum class Direction {
        TOP, LEFT, RIGHT, BOTTOM, RANDOM
    }

    /**
     * Gets the random pixel points in the given direction of the screen
     * @param activity - activity from where you are referring the random value.
     * @param direction - on among LEFT,RIGHT,TOP,BOTTOM,RANDOM
     * @return a pixel point {x,y} in the given direction.
     */
    fun getPointsInDirection(activity: Activity, direction: Direction?): IntArray {
        return when (direction) {
            Direction.LEFT -> getRandomLeft(activity)
            Direction.RIGHT -> getRandomRight(activity)
            Direction.BOTTOM -> getRandomBottom(activity)
            Direction.TOP -> getRandomTop(activity)
            else -> {
                val allDirections =
                    arrayOf(Direction.LEFT, Direction.TOP, Direction.BOTTOM, Direction.RIGHT)
                val index = Random().nextInt(allDirections.size)
                getPointsInDirection(activity, allDirections[index])
            }
        }
    }

    /**
     * Gets the random pixel points in the left direction of the screen. The value will be of {0,y} where y will be a random value.
     * @param activity - activity from where you are referring the random value.
     * @return a pixel point {x,y}.
     */
    private fun getRandomLeft(activity: Activity): IntArray {
        val x = 0
        val height = activity.resources.displayMetrics.heightPixels
        val random = Random()
        val y = random.nextInt(height)
        return intArrayOf(x, y)
    }

    /**
     * Gets the random pixel points in the top direction of the screen. The value will be of {x,0} where x will be a random value.
     * @param activity - activity from where you are referring the random value.
     * @return a pixel point {x,y}.
     */
    private fun getRandomTop(activity: Activity): IntArray {
        val y = 0
        val width = activity.resources.displayMetrics.widthPixels
        val random = Random()
        val x = random.nextInt(width)
        return intArrayOf(x, y)
    }

    /**
     * Gets the random pixel points in the right direction of the screen. The value will be of {screen_width,y} where y will be a random value.
     * @param activity - activity from where you are referring the random value.
     * @return a pixel point {x,y}.
     */
    private fun getRandomRight(activity: Activity): IntArray {
        val width = activity.resources.displayMetrics.widthPixels
        val height = activity.resources.displayMetrics.heightPixels
        val random = Random()
        val y = random.nextInt(height)
        return intArrayOf(width, y)
    }

    /**
     * Gets the random pixel points in the bottom direction of the screen. The value will be of {x,screen_height} where x will be a random value.
     * @param activity - activity from where you are referring the random value.
     * @return a pixel point {x,y}.
     */
    private fun getRandomBottom(activity: Activity): IntArray {
        val width = activity.resources.displayMetrics.widthPixels
        val height = activity.resources.displayMetrics.heightPixels
        val random = Random()
        val x = random.nextInt(width)
        return intArrayOf(x, height)
    }

    /**
     * Gets a random direction.
     * @return one among LEFT,RIGHT,BOTTOM,TOP
     */
    val randomDirection: Direction
        get() {
            val allDirections =
                arrayOf(Direction.LEFT, Direction.TOP, Direction.BOTTOM, Direction.RIGHT)
            val index = Random().nextInt(allDirections.size)
            return allDirections[index]
        }

    /**
     * Gets a random direction skipping the given direction.
     * @param toSkip a direction which should not be returned by this method.
     * @return one among LEFT,RIGHT,BOTTOM if TOP is provided as direction to skip,
     * one among TOP,RIGHT,BOTTOM if LEFT is provided as direction to skip
     * and so on.
     */
    fun getRandomDirection(toSkip: Direction?): Direction {
        val allExceptionalDirections: Array<Direction> = when (toSkip) {
            Direction.LEFT -> arrayOf(Direction.TOP, Direction.BOTTOM, Direction.RIGHT)
            Direction.RIGHT -> arrayOf(Direction.TOP, Direction.BOTTOM, Direction.LEFT)
            Direction.BOTTOM -> arrayOf(Direction.TOP, Direction.LEFT, Direction.RIGHT)
            Direction.TOP -> arrayOf(Direction.LEFT, Direction.BOTTOM, Direction.RIGHT)
            else -> arrayOf(Direction.LEFT, Direction.TOP, Direction.BOTTOM, Direction.RIGHT)
        }
        val index = Random().nextInt(allExceptionalDirections.size)
        return allExceptionalDirections[index]
    }
}
