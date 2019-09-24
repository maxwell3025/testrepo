package source;

import vectors.Point2D;

public class Jitterer extends Enemy{
double direction;
	public Jitterer(Rectangle R) {
		hitbox=R;
		hitbox.tex=Game.images[4];
		direction = Math.random()*Math.PI*2;
		health = 20.0;
	}
	@Override
	void update() {
		direction+=(Math.random()-0.5)*0.1;
		hitbox.base=Point2D.add(hitbox.base, new Point2D(Math.sin(direction),Math.cos(direction)).scale(0.01));
	}

}
