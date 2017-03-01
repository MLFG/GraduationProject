package cn.edu.lin.graduationproject.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import cn.edu.lin.graduationproject.R;

/**
 * Created by liminglin on 17-3-1.
 */

public class MyLetterView extends View {

    Paint paint;

    String[] letters = {"A","B","C","D",
            "E","F","G","H","I",
            "J","K","L","M","N",
            "O","P","Q","R","S","T",
            "U","V","W","X","Y","Z","#"};

    OnTouchLetterListener listener;

    int lettercolor;

    public void setOnTouchLetterListener(OnTouchLetterListener listener) {
        this.listener = listener;
    }

    public MyLetterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 1) 读取该自定义 view 的自定义属性
        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.MyLetterView);

        lettercolor = t.getColor(R.styleable.MyLetterView_letter_color, Color.BLACK);

        t.recycle();
        // 2) 初始化重要的属性，画笔
        initPaint();
    }

    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        // 设置颜色
        paint.setColor(lettercolor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 如果 MyLetterView 的尺寸测量规则与 View 的默认测量规则不一致则有必要重写此方法
        // View 的默认测量规则：如果指定了具体的尺寸值，就按照具体值来设定尺寸
        // 如果使用 wrap_content 或 match_parent，则都按照 match_parent来设定
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        // 如果 specMode 的值为 MeasureSpec.EXACTLY，意味着宽度设置的是具体或 match_parent
        // 如果 specMode 的值为 MeasureSpec.AT_MOST，意味着宽度设置的是 wrap_content
        if(specMode == MeasureSpec.AT_MOST){
            // 自行设定规则，宽度 = 左边距 + 最宽的文字宽度 + 右内边距
            int leftPadding = getPaddingLeft();
            int rightPadding = getPaddingRight();
            int textwidth = 0;
            for(int i=0;i<letters.length;i++){
                Rect bounds = new Rect();
                paint.getTextBounds(letters[i],0,letters[i].length(),bounds );
                if(bounds.width()>textwidth){
                    textwidth = bounds.width();
                }
            }

            int widthSize = leftPadding+textwidth+rightPadding;

            setMeasuredDimension(widthSize, MeasureSpec.getSize(heightMeasureSpec));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight()/27.0f;

        for(int i=0;i<letters.length;i++){

            Rect bounds = new Rect();
            paint.getTextBounds(letters[i], 0, letters[i].length(), bounds);
            float x = width/2.0f - bounds.width()/2.0f;
            float y = height/2.0f + bounds.height()/2.0f + i*height;
            canvas.drawText(letters[i], x, y, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:

                setBackgroundColor(Color.GRAY);
                float y = event.getY();

                int idx = (int) (y*letters.length/getHeight());

                if(idx>=0 && idx<letters.length){
                    if(listener!=null){
                        listener.onTouchLetter(letters[idx]);
                    }
                }

                break;
            default:
                setBackgroundColor(Color.TRANSPARENT);
                if(listener!=null){
                    listener.onReleaseLetter();
                }
                break;
        }

        return true;
    }

    public interface OnTouchLetterListener{
        void onTouchLetter(String letter);
        void onReleaseLetter();
    }

}
