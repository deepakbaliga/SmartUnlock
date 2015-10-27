package com.deezdroid.smartunlock;

/**
 * Created by deezdroid on 28/4/15.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

import com.squareup.picasso.Transformation;

/**
 * Picasso Transformation for applying Blurring effect to an image
 * this code is supposed to work in conjuction with the V8 Renderscript

 */
public class BlurTransformation implements Transformation {

    /**
     * Max blur radius supported by the Renderscript library
     **/
    protected static final int MAX_RADIUS = 25;
    /**
     * Min blur radius supported by the Renderscript library
     **/
    protected static final int MIN_RADIUS = 1;
    /**
     * Application context to instantiate the Renderscript
     **/
    protected final Context context;
    /**
     * Selected radius
     **/
    protected final int radius;

    /**
     * Creates a new Blur transformation
     *
     * @param context Application context to instantiate the Renderscript
     **/
    public BlurTransformation(Context context, int radius) {
        this.context = context;
        this.radius = radius < MIN_RADIUS ? MIN_RADIUS :
                radius > MAX_RADIUS ? MAX_RADIUS : radius;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        long startTime = System.currentTimeMillis();
        // Load a clean bitmap and work from that.
        Bitmap originalBitmap = source;

        // Create another bitmap that will hold the results of the filter.
        Bitmap blurredBitmap;
        blurredBitmap = Bitmap.createBitmap(originalBitmap);

        // Create the Renderscript instance that will do the work.
        RenderScript rs = RenderScript.create(context);

        // Allocate memory for Renderscript to work with
        Allocation input = Allocation.createFromBitmap(rs, originalBitmap, Allocation.MipmapControl.MIPMAP_FULL, Allocation.USAGE_SCRIPT);
        Allocation output = Allocation.createTyped(rs, input.getType());

        // Load up an instance of the specific script that we want to use.
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setInput(input);
        // Set the blur radius
        script.setRadius(radius);

        // Start the ScriptIntrinisicBlur
        script.forEach(output);
        // Copy the output to the blurred bitmap
        output.copyTo(blurredBitmap);

        source.recycle();
        return blurredBitmap;
    }

    @Override
    public String key() {
        return "blurred";
    }
}
