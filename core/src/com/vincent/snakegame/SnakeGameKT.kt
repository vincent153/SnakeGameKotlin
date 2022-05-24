package com.vincent.snakegame

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import java.awt.Color.black
import java.awt.Font
import kotlin.math.abs
import kotlin.random.Random
import kotlin.random.Random.Default.nextInt

class SnakeGameKT:ApplicationAdapter() , GestureDetector.GestureListener {
    private lateinit var camera: OrthographicCamera
    private lateinit var shapeRenderer: ShapeRenderer
    private var moveStepX = 80
    private var moveStepY = 0
    private val blockSize = 80f
    private val step = blockSize.toInt()
    private var viewportWidth = 1080f
    private var viewportHeight = 2020f
    private val fps = 5
    private var screenHeight = 0
    private var screenWidth = 0
    private var dragThreshold = 10
    private lateinit var head:Rectangle
    private lateinit var food:Rectangle
    private lateinit var snake:ArrayList<Rectangle>
    private var direction = Direction.RIGHT
    private var score = 0
    private lateinit var font: BitmapFont
    private lateinit var batch: SpriteBatch
    override fun create() {
        font = BitmapFont()
        font.color = Color.RED
        font.data.setScale(5f)

        batch = SpriteBatch()

        screenHeight = Gdx.graphics.height
        screenWidth = Gdx.graphics.width

        viewportHeight = screenHeight-(screenHeight%blockSize)
        viewportWidth = screenWidth-(screenWidth%blockSize)

        println("viewport width:$viewportWidth,viewport:$viewportHeight")

        camera = OrthographicCamera()
        camera.setToOrtho(false, viewportWidth, viewportHeight)

        shapeRenderer = ShapeRenderer()

        var gd = GestureDetector(this)
        Gdx.input.inputProcessor = gd

        val rndStart = (((viewportWidth-blockSize)/blockSize)/2).toInt()
        val rndEnd = ((viewportWidth-blockSize)/blockSize).toInt()
        println("x:${rndStart},y:${rndEnd}")
        val rndX = nextInt(rndStart,rndEnd).toFloat()
        val rndY = nextInt(rndStart,rndEnd).toFloat()
        head = makeRectangle(rndX*blockSize,rndY*blockSize)
        snake = arrayListOf(head,makeRectangle(head.x-blockSize.toInt(),head.y)
                ,makeRectangle(head.x-blockSize.toInt()*2,head.y))
        placeFood()
        println("init snake:${snake}")
    }

    private fun placeFood(){

        val rndX = nextInt(0, ((viewportWidth/blockSize)-1).toInt()).toFloat()
        val rndY = nextInt(0,((viewportHeight/blockSize)-1).toInt()).toFloat()
        food = makeRectangle(rndX*blockSize,rndY*blockSize)
        if(snake.contains(food)){
            placeFood()
        }
        println("new food:${food.x},${food.y}")
    }

    private fun eatFood():Boolean{
        return head == food
    }


    private fun updateScore(){
        batch.begin();
        font.draw(batch, "Score:$score", 0f, viewportHeight);
        batch.end();
    }

    private fun drawRect(rec:Rectangle,color:Color = Color.GREEN,border:Int=2){
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.BLACK
        shapeRenderer.rect(rec.x, rec.y, blockSize, blockSize)
        shapeRenderer.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = color
        shapeRenderer.rect(rec.x+border, rec.y+border, blockSize-border*2, blockSize-border*2)
        shapeRenderer.end()

    }

    private fun updateSnakeAndFood(){
        for(i in 0 until snake.size){
            drawRect(snake[i])
        }
        drawRect(food,Color.RED)
    }

    override fun render() {
        ScreenUtils.clear(0f, 0f, 0.2f, 1f)
        camera.update()

        //calculate new head pos
        head = Rectangle(head)
        head.x+=moveStepX
        head.y+=moveStepY

        //handle over boundary
        if(head.x >= viewportWidth){
            head.x = 0f
        }
        if(head.x < 0){
            head.x = viewportWidth-blockSize
        }
        if (head.y >= viewportHeight){
            head.y = 0f
        }
        if(head.y < 0) {
            head.y = viewportHeight-blockSize
        }

        //movement strategy,add new head,if eat food->keep tail
        //else trim tail
        snake.add(0,head)
        if(eatFood()){
            //keep tail
            placeFood()
            score++
        }else{
            //trim tail
            snake.removeAt(snake.size-1)
        }

        updateSnakeAndFood()
        updateScore()
        Thread.sleep((1000/fps-Gdx.graphics.deltaTime).toLong());

    }

    override fun dispose() {
        shapeRenderer.dispose()
        font.dispose()
        batch.dispose()
    }

    override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        println("touchDown")
        return true
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        println("tap")
        return true
    }

    override fun longPress(x: Float, y: Float): Boolean {
        println("longPress")
        return true
    }

    override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean {
        println("fling")
        return true
    }

    override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
        println("pan,x:$x,y:$y,deltaX:$deltaX,deltaY:$deltaY")
        val absX = abs(deltaX)
        val absY = abs(deltaY)
        if (absX>absY&&absX>dragThreshold) {
            moveStepY = 0
            moveStepX = when{
                deltaX >= 0 -> {
                    println("drag to right")
                    direction = Direction.RIGHT
                    step
                }
                else -> {
                    println("drag to left")
                    direction = Direction.LEFT
                    -step
                }
            }
        }else
        {
            if(absY>dragThreshold) {
                moveStepX = 0
                moveStepY = when {
                    deltaY >= 0 -> {
                        println("drag to down")
                        direction = Direction.DOWN
                        -step
                    }
                    else -> {
                        println("drag to up")
                        direction = Direction.UP
                        step
                    }
                }
            }
        }
        return true
    }

    override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        println("panStop")
        return true
    }
    override fun zoom(initialDistance: Float, distance: Float): Boolean {
        println("zoom")
        return true
    }

    override fun pinch(
        initialPointer1: Vector2?,
        initialPointer2: Vector2?,
        pointer1: Vector2?,
        pointer2: Vector2?
    ): Boolean {
        println("pinch")
        return true
    }

    override fun pinchStop() {
        println("pinchStop")
    }


    private fun makeRectangle(x:Float, y:Float):Rectangle{
        return Rectangle(x,y,blockSize,blockSize)
    }
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}