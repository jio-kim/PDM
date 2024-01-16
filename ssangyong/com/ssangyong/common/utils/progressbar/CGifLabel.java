package com.ssangyong.common.utils.progressbar;

import java.io.InputStream;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
/**
 * Gif Label Å¬·¡½º
 * @author Administrator
 *
 */
public class CGifLabel extends Canvas
{
    private Image image = null;
    private GifThread thread = null;

    public CGifLabel(Composite parent, int style)
    {
        super(parent, style);

        addPaintListener(new PaintListener()
        {
            public void paintControl(PaintEvent e)
            {
                if (image == null)
                    return;
                e.gc.drawImage(image, 0, 0);
            }
        });

    }

    public void setGifImage(InputStream inputStream)
    {

        ImageLoader imageLoader = new ImageLoader();
        ImageData[] imageDatas = imageLoader.load(inputStream);

        if (imageLoader.data[0] != null)
            this.image = new Image(this.getDisplay(), imageDatas[0].width, imageDatas[0].height);

        GC gc = new GC(image);
        thread = new GifThread(gc, imageDatas);
        thread.start();
    }

    public void setImage(Image image)
    {
        if (thread != null)
        {
            thread.stopRunning();
            try
            {
                thread.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        if (image != this.image)
        {
            this.image = image;

            CGifLabel.this.getDisplay().asyncExec(new Runnable()
            {
                public void run()
                {
                    redraw();
                }
            });

        }
    }

    private class GifThread extends Thread
    {

        int frameIndex = 0;
        private GC gc = null;
        private boolean run = true;
        private ImageData[] imageDatas;

        public GifThread(GC gc, ImageData[] imageDatas)
        {
            this.gc = gc;
            this.imageDatas = imageDatas;
        }

        public void run()
        {
            while (run)
            {

                if (!CGifLabel.this.isDisposed())
                {
                    frameIndex %= imageDatas.length;
                    final ImageData frameData = imageDatas[frameIndex];

                    CGifLabel.this.getDisplay().asyncExec(new Runnable()
                    {
                        public void run()
                        {
                            if (!run)
                            {
                                return;
                            }
                            if (!CGifLabel.this.isDisposed())
                            {
                                Image frame = new Image(CGifLabel.this.getDisplay(), frameData);
                                if (frame == null || frame.isDisposed())
                                    return;
                                gc.drawImage(frame, frameData.x, frameData.y);
                                frame.dispose();
                                redraw();
                            }
                            else
                                stopRunning();
                        }
                    });
                    try
                    {
                        Thread.sleep(imageDatas[frameIndex].delayTime * 10);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    frameIndex += 1;
                }
                else
                    stopRunning();

            }
        }

        public void stopRunning()
        {
            run = false;
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();
        if (thread != null)
            thread.stopRunning();
    }

}
