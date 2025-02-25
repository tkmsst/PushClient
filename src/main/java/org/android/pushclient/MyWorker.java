/**
 * Worker to restore SCREEN_OFF_TIMEOUT.
 */

package org.android.pushclient;

import android.content.Context;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MyWorker extends Worker {

    private final Context context;

    public MyWorker(Context ct, WorkerParameters params) {
        super(ct, params);
        context = ct;
    }

    @NonNull
    @Override
    public Result doWork() {
        final int screenTimeout =
                getInputData().getInt("TIMEOUT", 0);
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, screenTimeout);
        return Result.success();
    }
}
