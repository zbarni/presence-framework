/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.csipsimple.service;

import android.R.bool;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;

import com.csipsimple.utils.Log;

public class Component extends Service {

	private static final String THIS_FILE = "COMP SRV";
	
	//components 	
	private static Boolean HEADSET_CONNECTED = false;
	private static Boolean 
	private static Boolean 
	private static Boolean 
	private static Boolean 
	private static Boolean 

	public IBinder onBind (Intent intent) {

		return null;
	}

	public void onCreate () {
		super.onCreate();
		// TODO Auto-generated method stub
		Log.d(THIS_FILE,"Component Service Created");
//		this.stopSelf();
	}

	public int onStartCommand (Intent intent, int flags, int startId) {
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		Log.d(THIS_FILE,"Component Service Started!");
		scanComponents();
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(THIS_FILE,"Component Service Destroyed");
		super.onDestroy();
	}

	private void scanComponents() {
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(300);
	}
	
}
