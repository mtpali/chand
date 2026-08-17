package com.chand.mobiletina

import android.app.Application

/**
 * Keep Application startup intentionally minimal.
 *
 * Widget broadcasts may start the process while the launcher is rendering the home screen.
 * Scheduling network work from Application.onCreate can make a launcher treat the provider
 * as failed if WorkManager initialization is delayed or throws on a vendor ROM. Scheduling
 * is therefore done only from the dollar receiver and when the activity is opened.
 */
class ChandApp : Application()
