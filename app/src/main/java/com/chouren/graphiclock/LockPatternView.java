package com.chouren.graphiclock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 图形解锁
 * @author ChouRenTang
 */
public class LockPatternView extends View {
	/* 存放已绘制的点  */
	private Point[][] points = new Point[3][3];
	/* 绘制的画笔  */
	private Paint paint;
	/* 点在不同状态下使用的图片  */
	private Bitmap bitmapNormal/* 正常 */, bitmapPressed/* 按下 */, bitmapError/*错误*/;
	/* 连线在不同状态下使用的图片 */
	private Bitmap lineNormal/* 正常 */, lineError/* 错误 */;
	/* 变化连线时使用的矩阵 */
	private Matrix matrix;
	/* 图片等已初始化的标志  */
	private boolean hasInit;
	/* 整个view（正方形）的边长  */
	private int sideLength;
	/* 图片的直径  */
	private int diameters;
	/* 按下的点的集合  */
	private List<Point> pressedPoints;
	/* 当前触摸的左边  */
	private float touchX, touchY;
	/* 标志  */
	private boolean drawBegin/*绘制开始*/, drawFinish/*绘制结束*/, drawingBlank/*绘制中（未接触到点）*/;
	
	public LockPatternView(Context context) {
		super(context);
		init();
	}

	public LockPatternView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		hasInit = false;
		drawingBlank = false;
		paint = new Paint();
		pressedPoints = new ArrayList<Point>();
		matrix = new Matrix();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int specWidth = MeasureSpec.getSize(widthMeasureSpec);  
        int specHeight = MeasureSpec.getSize(heightMeasureSpec); 
        sideLength = specWidth >= specHeight ? specHeight : specWidth;
        diameters = sideLength/5;
        setMeasuredDimension(specWidth, specHeight);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(!hasInit) {
			Logger.debug(null, "init");
			initBitmaps();
			initPoints();
			hasInit = true;
		}
		
		drawPoints2Canvas(canvas);
		drawLines2Canvas(canvas);
	}
	
	private void drawPoints2Canvas(Canvas canvas) {
		int radius = diameters/2;
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				Point point = points[i][j];
				if(Point.State.STATE_NORMAL == point.state) {
					canvas.drawBitmap(bitmapNormal, point.x - radius, point.y - radius, paint);
				}else if(Point.State.STATE_PRESSED == point.state) {
					canvas.drawBitmap(bitmapPressed, point.x - radius, point.y - radius, paint);
				}else if(Point.State.STATE_ERROR == point.state) {
					canvas.drawBitmap(bitmapError, point.x - radius, point.y - radius, paint);
				}
			}
		}
	}
	
	private void drawLines2Canvas(Canvas canvas) {
		if(pressedPoints.size() > 0) {
			Point from = pressedPoints.get(0);
			for(int i = 1; i < pressedPoints.size(); i++) {
				Point to = pressedPoints.get(i);
				drawLine(canvas, from, to);
				from = to;
			}
			
			if(drawingBlank) {
				drawLine(canvas, from, new Point(touchX, touchY));
			}
		}
		
	}
	
	private void drawLine(Canvas canvas, Point from, Point to) {
		float lineLength = (float) distance(from, to);
		float degrees = getDegrees(from, to);
		canvas.rotate(degrees, from.x, from.y);
		if(from.state == Point.State.STATE_PRESSED) {
			matrix.setScale(lineLength / lineNormal.getWidth(), 1);
			matrix.postTranslate(from.x, from.y);
			canvas.drawBitmap(lineNormal, matrix, paint);
		}else {
			matrix.setScale(lineLength / lineError.getWidth(), 1);
			matrix.postTranslate(from.x, from.y);
			canvas.drawBitmap(lineError, matrix, paint);
		}
		canvas.rotate(-degrees, (from.x), (from.y));
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		touchX = event.getX();
		touchY = event.getY();
		Point point = null;
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			resetDraw();
			point = isPointBeTouched(touchX, touchY);
			if(point != null) {
				drawBegin = true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			drawingBlank = true;
			if(drawBegin) {
				point = isPointBeTouched(touchX, touchY);
			}
			break;
		case MotionEvent.ACTION_UP:
			drawFinish = true;
			drawBegin = false;
			drawingBlank = false;
			break;
		}
		
		if(!drawFinish && drawBegin && point != null) {
			if(isCrossPoint(point)) {
				
			}else {
				point.state = Point.State.STATE_PRESSED;
				pressedPoints.add(point);
			}
		}
		
		if(drawFinish) {
			if(pressedPoints.size() < 2) {
				drawFail();
			}
			if(pressedPoints.size() >= 2 && pressedPoints.size() < 5) {
				drawError();
			}
		}
		
		postInvalidate();
		return true;
	}
	
	private void initPoints() {
		float offset = (getMeasuredHeight() - getMeasuredWidth()) / 2.0f;
		int gabWidth = (sideLength - 3*diameters)/4;
		int columnOneX = gabWidth + diameters/2;
		int columnTwoX = gabWidth*2 + 3*diameters/2;
		int columnThreeX = gabWidth*3 + 5*diameters/2;
		int rowOneY = (int) (offset + columnOneX);
		int rowTwoY = (int) (offset + columnTwoX);
		int rowThreeY = (int) (offset + columnThreeX);
		points[0][0] = new Point(columnOneX, rowOneY);
		points[0][0].setIndex(0);
		points[0][1] = new Point(columnTwoX, rowOneY);
		points[0][1].setIndex(1);
		points[0][2] = new Point(columnThreeX, rowOneY);
		points[0][2].setIndex(2);
		
		points[1][0] = new Point(columnOneX, rowTwoY);
		points[1][0].setIndex(3);
		points[1][1] = new Point(columnTwoX, rowTwoY);
		points[1][1].setIndex(4);
		points[1][2] = new Point(columnThreeX, rowTwoY);
		points[1][2].setIndex(5);
		
		points[2][0] = new Point(columnOneX, rowThreeY);
		points[2][0].setIndex(6);
		points[2][1] = new Point(columnTwoX, rowThreeY);
		points[2][1].setIndex(7);
		points[2][2] = new Point(columnThreeX, rowThreeY);
		points[2][2].setIndex(8);
	}
	
	private void initBitmaps() {
		bitmapNormal = decodeBitmap(R.drawable.ic_point_normal, diameters, diameters);
		bitmapPressed = decodeBitmap(R.drawable.ic_point_pressed, diameters, diameters);
		bitmapError = decodeBitmap(R.drawable.ic_point_error, diameters, diameters);
		
		lineNormal = BitmapFactory.decodeResource(getResources(), R.drawable.ic_line_normal);
		lineError = BitmapFactory.decodeResource(getResources(), R.drawable.ic_line_error);
	}
	
	private Bitmap decodeBitmap(int drawable, int needWidth, int needHeight) {
		Bitmap rawBitmap = BitmapFactory.decodeResource(getResources(), drawable);
		
		int rawWidth = rawBitmap.getWidth();
		int rawHeight = rawBitmap.getHeight();
		float widthScale = needWidth/(float)rawWidth;
		float heightScale = needHeight/(float)rawHeight;
		
		Matrix matrix = new Matrix();
		matrix.postScale(widthScale, heightScale);
		Bitmap needBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, rawWidth, rawHeight, matrix, false);
		return needBitmap;
	}
	
	private Point isPointBeTouched(float touchX, float touchY) {
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j< 3; j++) {
				Point p = points[i][j];
				if(isTouchInPoint(p.x, p.y, diameters/2, touchX, touchY)) {
					return p;
				}
			}
		}
		return null;
	}
	
	private boolean isTouchInPoint(float centerX, float centerY, float radius, float touchX, float touchY) {
		return Math.sqrt((centerX - touchX) * (centerX - touchX) + (centerY - touchY) * (centerY - touchY)) < radius;
	}
	
	private double distance(Point from, Point to) {
		return Math.sqrt(Math.abs(from.x - to.x) * Math.abs(from.x - to.x) + Math.abs(from.y - to.y) * Math.abs(from.y - to.y));
	}
	
	private float getDegrees(Point from, Point to) {
		return (float) Math.toDegrees(Math.atan2(to.y - from.y, to.x - from.x));
	}
	
	private boolean isCrossPoint(Point p) {
		if(pressedPoints.contains(p)) return true;
		else return false;
	}
	
	private void resetDraw() {
		Logger.warn(null, "reset points");
		drawFinish = false;
		pressedPoints.clear();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				points[i][j].state = Point.State.STATE_NORMAL;
			}
		}
	}
	
	private void drawFail() {
		resetDraw();
	}
	
	private void drawError() {
		for(Point p : pressedPoints) {
			p.state = Point.State.STATE_ERROR;
		}
	}
	
	public static class Point {
		public static enum State {STATE_NORMAL, STATE_PRESSED, STATE_ERROR};
		
		private State state;
		private float x, y;
		private int index;
		
		public Point() {
			this(0, 0);
		}
		public Point(float x, float y) {
			this.x = x;
			this.y = y;
			this.state = State.STATE_NORMAL;
			this.index = 0;
		}
		
		public void setIndex(int index) {
			this.index = index;
		}
		public int getIndex() {
			return this.index;
		}
	}

}
