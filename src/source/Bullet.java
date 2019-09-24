package source;

import vectors.Point2D;

public class Bullet {
Point2D vel;
Point2D pos;
	public Bullet(Point2D position,Point2D velocity) {
		pos = position;
		vel = velocity;
	}
	public void update(){
		pos = Point2D.add(pos,vel);
	}

}
