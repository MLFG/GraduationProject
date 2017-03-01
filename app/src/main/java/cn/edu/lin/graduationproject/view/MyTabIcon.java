package cn.edu.lin.graduationproject.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import cn.edu.lin.graduationproject.R;

/**
 * Created by liminglin on 17-3-1.
 */

public class MyTabIcon extends View {

    Drawable drawable; // 自定义 view 要显示的图片
    Bitmap bitmap;
    String text;       // 自定义 view 要显示的文字
    int color;         // 自定义 view 要呈现的颜色（图片、文字）
    int textSize;      // 自定义 view 显示文字时字体的大小
    Paint textPaint;   // 画文字的画笔
    Paint drawPaint;   // 画图的画笔
    // 0 - 255 0 完全透明 ; 255 完全不透明
    int alpha;

    public MyTabIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
        initPaint();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);
        drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    /**
     * 读取布局文件中，使用该 view 时用户提供的自定义属性的属性值
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {
        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.MyTabIcon);
        // 读取图片
        drawable = t.getDrawable(R.styleable.MyTabIcon_tabicon_drawable);
        bitmap = ((BitmapDrawable)drawable).getBitmap();
        bitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth()*2,bitmap.getHeight()*2,true);
        // 读取文字
        text = t.getString(R.styleable.MyTabIcon_tabicon_text);
        // 颜色（默认为绿色）
        color = t.getColor(R.styleable.MyTabIcon_tabicon_color, Color.GREEN);
        // 字体大小(默认为 12 sp)
        textSize = t.getDimensionPixelSize(R.styleable.MyTabIcon_tabicon_textsize,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,12,getResources().getDisplayMetrics()));
        t.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST &&
                MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST){
            // 如果 MyTabIcon 宽度和高度都指定为 wrap_content 时则通过计算自行设定宽度和高度的尺寸
            Rect bounds = new Rect();
            textPaint.getTextBounds(text,0,text.length(),bounds);
            int leftPadding = getPaddingLeft();
            int rightPadding = getPaddingRight();
            // 取图片宽度和文字宽度中较大的值
            int contentWidth = Math.max(bitmap.getWidth(),bounds.width());
            int widthSize = leftPadding + contentWidth + rightPadding;
            int topPadding = getPaddingTop();
            int bottomPadding = getPaddingBottom();
            int contentHeight = bitmap.getHeight() + bounds.height();
            int heightSize = topPadding + contentHeight + bottomPadding;

            setMeasuredDimension(widthSize,heightSize);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float left = getWidth() / 2 - bitmap.getWidth() / 2;
        float top = getHeight() / 2 - bitmap.getHeight() / 2 - 8;
        // 画图
        canvas.drawBitmap(bitmap,left,top,null);
        // 画字
        Rect bounds = new Rect();
        textPaint.getTextBounds(text,0,text.length(),bounds);
        float x = getWidth() / 2 - bounds.width() / 2;
        float y = getHeight() / 2 + bitmap.getHeight() + bounds.height() - 10;
        textPaint.setColor(Color.GRAY);
        canvas.drawText(text,x,y,textPaint);
        drawColorText(canvas,x,y);
        drawColorBitmap(canvas,left,top);
    }

    /**
     * 画彩色的图片
     * @param canvas
     * @param left
     * @param top
     */
    private void drawColorBitmap(Canvas canvas, float left, float top) {
        // 1)创建一副彩色的图片
        // 1.1 创建一副空白的图片
        Bitmap emptyBitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas myCanvas = new Canvas(emptyBitmap);
        // 1.2 将灰色图片画到空白图片上
        myCanvas.drawBitmap(bitmap,0,0,null);
        // 1.3 利用SRC_IN混合模式再画一个色块到空白图片上
        Rect r = new Rect(0,0,emptyBitmap.getWidth(),emptyBitmap.getHeight());
        drawPaint.setColor(color);
        drawPaint.setAlpha(alpha);
        drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        myCanvas.drawRect(r,drawPaint);

        // 2)再利用 canvas 将彩色的图片画到屏幕上
        // 画的位置恰好与已经在屏幕上的灰色图片一样
        canvas.drawBitmap(emptyBitmap,left,top,null);
    }

    /**
     * 画彩色的文字
     * @param canvas
     * @param x
     * @param y
     */
    private void drawColorText(Canvas canvas, float x, float y) {
        textPaint.setColor(color);
        textPaint.setAlpha(alpha);
        canvas.drawText(text,x,y,textPaint);
    }

    public void setPaintAlpha(int alpha){
        this.alpha = alpha;
        invalidate();
    }
}
