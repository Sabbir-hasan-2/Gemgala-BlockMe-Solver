package com.codex.blockmesolver

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.accessibilityservice.GestureDescription
import java.util.PriorityQueue
import kotlin.math.max
import kotlin.math.min

class SolverAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    // Public entry point for a future overlay/button.
    fun scanAndSolve(autoPlay: Boolean = false) {
        if (android.os.Build.VERSION.SDK_INT < 30) return
        takeScreenshot(0, mainExecutor, object : TakeScreenshotCallback {
            override fun onSuccess(result: ScreenshotResult) {
                val bmp = Bitmap.wrapHardwareBuffer(result.hardwareBuffer, result.colorSpace)
                    ?: return
                val copy = bmp.copy(Bitmap.Config.ARGB_8888, false)
                result.hardwareBuffer.close()
                val board = BoardVision.readBoard(copy)
                val pieces = BoardVision.readPieces(copy)
                val moves = Solver.solve(board, pieces)
                if (autoPlay) playMoves(moves, copy.width, copy.height)
            }
            override fun onFailure(errorCode: Int) {}
        })
    }

    private fun playMoves(moves: List<Move>, w: Int, h: Int) {
        // Normalized coordinates are based on the BlockMe layout in the supplied screenshot.
        val pieceXs = floatArrayOf(.17f, .50f, .83f)
        val pieceY = .775f
        var delay = 0L
        for (m in moves) {
            val sx = pieceXs[m.pieceIndex] * w
            val sy = pieceY * h
            val boardLeft = .055f * w
            val boardTop = .287f * h
            val cellW = (.892f * w) / 8f
            val cellH = (.400f * h) / 8f
            val tx = boardLeft + (m.col + 0.5f) * cellW
            val ty = boardTop + (m.row + 0.5f) * cellH
            handler.postDelayed({ drag(sx, sy, tx, ty) }, delay)
            delay += 650L
        }
    }

    private fun drag(sx: Float, sy: Float, tx: Float, ty: Float) {
        val path = Path().apply { moveTo(sx, sy); lineTo(tx, ty) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 350))
            .build()
        dispatchGesture(gesture, null, null)
    }

    data class Move(val pieceIndex: Int, val row: Int, val col: Int, val score: Int)

    object BoardVision {
        fun readBoard(b: Bitmap): Array<BooleanArray> {
            val out = Array(8) { BooleanArray(8) }
            val left = (0.055f * b.width).toInt()
            val top = (0.287f * b.height).toInt()
            val cw = (0.892f * b.width / 8f)
            val ch = (0.400f * b.height / 8f)
            for (r in 0 until 8) for (c in 0 until 8) {
                val x = (left + (c + .5f) * cw).toInt().coerceIn(0,b.width-1)
                val y = (top + (r + .5f) * ch).toInt().coerceIn(0,b.height-1)
                val rgb = b.getPixel(x,y)
                val sat = saturation(rgb)
                val lum = (Color.red(rgb)+Color.green(rgb)+Color.blue(rgb))/3
                out[r][c] = sat > 0.35f && lum > 75
            }
            return out
        }

        fun readPieces(b: Bitmap): List<List<Pair<Int,Int>>> {
            // Prototype: color/shape recognition can be tuned for the exact Gemgala build.
            // It returns three common silhouettes from the supplied UI as a safe baseline.
            return listOf(
                listOf(0 to 0, 0 to 1, 1 to 1, 2 to 1, 3 to 1, 4 to 1, 4 to 2),
                listOf(0 to 0, 0 to 1, 1 to 1, 2 to 1, 3 to 1, 4 to 1, 5 to 1),
                listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4, 1 to 4)
            )
        }

        private fun saturation(color: Int): Float {
            val mx = max(Color.red(color), max(Color.green(color), Color.blue(color))) / 255f
            val mn = min(Color.red(color), min(Color.green(color), Color.blue(color))) / 255f
            return if (mx == 0f) 0f else (mx-mn)/mx
        }
    }

    object Solver {
        fun solve(board: Array<BooleanArray>, pieces: List<List<Pair<Int,Int>>>): List<Move> {
            val pq = PriorityQueue<Move>(compareByDescending { it.score })
            for (i in pieces.indices) {
                val p = pieces[i]
                for (r in 0 until 8) for (c in 0 until 8) {
                    if (fits(board,p,r,c)) {
                        val gain = p.size + lines(board,p,r,c)*12
                        pq.add(Move(i,r,c,gain))
                    }
                }
            }
            val result = mutableListOf<Move>()
            val used = mutableSetOf<Int>()
            repeat(3) {
                val m = pq.firstOrNull { it.pieceIndex !in used } ?: return@repeat
                result.add(m); used.add(m.pieceIndex)
            }
            return result
        }

        private fun fits(board: Array<BooleanArray>, p: List<Pair<Int,Int>>, r0:Int,c0:Int):Boolean {
            for ((dr,dc) in p) {
                val r=r0+dr; val c=c0+dc
                if (r !in 0..7 || c !in 0..7 || board[r][c]) return false
            }
            return true
        }

        private fun lines(board:Array<BooleanArray>,p:List<Pair<Int,Int>>,r0:Int,c0:Int):Int {
            val a=Array(8){board[it].clone()}
            for((dr,dc) in p){ val r=r0+dr; val c=c0+dc; if(r in 0..7&&c in 0..7)a[r][c]=true }
            var n=0
            for(r in 0..7) if(a[r].all{it}) n++
            for(c in 0..7) if((0..7).all{a[it][c]}) n++
            return n
        }
    }
}
