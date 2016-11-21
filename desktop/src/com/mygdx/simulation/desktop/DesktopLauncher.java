package com.mygdx.simulation.desktop;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.simulation.TransportSimulation;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Graphics.DisplayMode primaryMode = LwjglApplicationConfiguration.getDesktopDisplayMode();
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.setFromDisplayMode(primaryMode);
//		config.width = 1600;
//		config.height = 900;
//		config.fullscreen = true;
		// config.vSyncEnabled = true;
		new LwjglApplication(new TransportSimulation(), config);
	}
}
