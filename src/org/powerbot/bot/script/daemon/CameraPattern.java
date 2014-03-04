package org.powerbot.bot.script.daemon;

import org.powerbot.bot.script.Antipattern;
import org.powerbot.script.rs3.tools.MethodContext;
import org.powerbot.script.util.Random;

public class CameraPattern extends Antipattern {
	public CameraPattern(final MethodContext factory) {
		super(factory);
	}

	@Override
	public void run() {
		final boolean a = isAggressive();
		final int t = ctx.camera.getYaw(), c = Random.nextInt(1, 3) * (a ? 2 : 1);

		for (int i = 0; i < c; i++) {
			final String k = Random.nextBoolean() ? "LEFT" : "RIGHT";
			ctx.keyboard.send("{VK_" + k + " down}");
			sleep(100, a ? Random.nextInt(200, 300) : 800);
			ctx.keyboard.send("{VK_" + k + " up}");
		}

		if (isStateful()) {
			final int d = 10;
			ctx.camera.setAngle(t + Random.nextInt(-d, d));
		}
	}
}