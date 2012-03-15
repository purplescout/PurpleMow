package se.purplescout.purplemow.simulator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SimulatorView extends SurfaceView implements SurfaceHolder.Callback {
    class SimulatorThread extends Thread {
        private boolean running;
		private SurfaceHolder surfaceHolder;
		private SimulatorModel model;
		private Paint cutterPaint;
		private Paint mowerPaint;
		private Paint linePaint;
		private Bitmap bitmap;
		private Canvas lawnCanvas;
		private Paint bitmapPaint;

		public SimulatorThread(SurfaceHolder holder) {
			this.surfaceHolder = holder;
			
			cutterPaint = new Paint();
            cutterPaint.setARGB(0xFF, 0, 0x90, 0);

			mowerPaint = new Paint();
            mowerPaint.setARGB(0xFF, 0xFF, 0, 0xFF);
            
			linePaint = new Paint();
            linePaint.setARGB(0xFF, 0, 0, 0);

            bitmapPaint = new Paint(Paint.DITHER_FLAG);
		}

		public void setModel(SimulatorModel model) {
			synchronized(this) {
				this.model = model;
				notify();
			}
		}

		@Override
        public void run() {
			Log.d(this.getClass().getName(), "Starting view thread");

            bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            lawnCanvas = new Canvas(bitmap);
			
			if (model == null) {
        		synchronized(this) {
        			try {
        				wait();
        			} catch (InterruptedException e) {}
        		}
        	}

			while (running) {
                Canvas c = null;
                try {
                    c = surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder) {
                    	model.update();
                        drawCanvas(c);
                        Thread.sleep(20);
                    }
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
                    if (c != null) {
                        surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

		private void drawCanvas(Canvas c) {
			final float radius = 20;

			c.drawColor(0xFF00A000);
			c.drawBitmap(bitmap, 0, 0, bitmapPaint);
			float x = getWidth() * model.getMowerX();
			float y = getHeight() * model.getMowerY();
			float d = model.getDirection();
			float dx = (float) (1.5*radius*Math.cos(d));
			float dy = (float) (1.5*radius*Math.sin(d));
			
			lawnCanvas.drawCircle(x, y, radius-2, cutterPaint);
			c.drawCircle(x, y, radius, mowerPaint);
			c.drawLine(x, y, x + dx, y + dy, linePaint);
		}

    }

	private SimulatorThread thread;

	public SimulatorView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
		thread = new SimulatorThread(surfaceHolder);
	}

	@Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

	@Override
    public void surfaceCreated(SurfaceHolder holder) {
		thread.running = true;
		thread.start();
    }

	@Override
    public void surfaceDestroyed(SurfaceHolder holder) {
		thread.running = false;
        while (true) {
            try {
                thread.join();
                break;
            } catch (InterruptedException e) {}
        }
    }

	public void setModel(SimulatorModel model) {
        thread.setModel(model);
	}

}
