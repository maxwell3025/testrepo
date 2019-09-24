package source;

import vectors.Point2D;

public class Player {
	Point2D vel = Point2D.Origin();
	Point2D pos;
	Point2D dim;
	Point2D prevpos = Point2D.Origin();
	public Player(Point2D startingPos, Point2D size) {
		pos = startingPos;
		dim=size;
	}

}
