package com.ober.opdf.surface.render;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * Created by ober on 2020/7/30.
 */
public class ViewGestureHelper {
    interface GestureHandler {
        void onSingleTapUp(MotionEvent event);
        void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);
        void onScale(ScaleGestureDetector detector);
        void onScaleBegin(ScaleGestureDetector detector);
        void onScaleEnd(ScaleGestureDetector detector);
        void onPointerUp(MotionEvent event);
    }

    public static void bindView(View view, final GestureHandler gestureHandler) {

        final ComposedListener composedListener = new ComposedListener(gestureHandler);

        final GestureDetector gestureDetector = new GestureDetector(view.getContext(), composedListener);
        final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(view.getContext(), composedListener);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);
                if(composedListener.isScaling) {
                    return true;
                }
                boolean r = gestureDetector.onTouchEvent(event);

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    gestureHandler.onPointerUp(event);
                }

                return r;
            }
        });
    }

    private static class ComposedListener implements
            GestureDetector.OnGestureListener,
            ScaleGestureDetector.OnScaleGestureListener {

        private final GestureHandler gestureHandler;

        private boolean isScaling;

        ComposedListener(GestureHandler handler) {
            this.gestureHandler = handler;
            isScaling = false;
        }


        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            gestureHandler.onSingleTapUp(e);
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            gestureHandler.onScroll(e1, e2, distanceX, distanceY);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            gestureHandler.onScale(detector);

            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            isScaling = true;
            gestureHandler.onScaleBegin(detector);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isScaling = false;
            gestureHandler.onScaleEnd(detector);
        }
    }

}
