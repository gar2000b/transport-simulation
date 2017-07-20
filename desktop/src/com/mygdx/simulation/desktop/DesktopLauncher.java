package com.mygdx.simulation.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.simulation.TransportSimulation;

public class DesktopLauncher {
	public static void main (String[] arg) {
		// Graphics.DisplayMode primaryMode = LwjglApplicationConfiguration.getDesktopDisplayMode();
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		// config.setFromDisplayMode(primaryMode);
//		System.out.println("width: " + primaryMode.width);
//		System.out.println("height: " + primaryMode.height);
//		System.out.println(primaryMode.toString());
//		config.width = 1024;
//		config.height = 768;
		config.width = LwjglApplicationConfiguration.getDesktopDisplayMode().width;
		config.height = LwjglApplicationConfiguration.getDesktopDisplayMode().height;
		// config.fullscreen = true;
		// config.vSyncEnabled = true;
		new LwjglApplication(new TransportSimulation(), config);
	}
}
