package source;

import java.awt.image.BufferedImage;

import vectors.Point2D;

public class Rectangle {
Point2D base;
Point2D dim;
BufferedImage tex;
	public Rectangle(Point2D b, Point2D a, BufferedImage texture) {
		base = b;
		dim = a;
		tex = texture;
	}
	public Rectangle(Point2D b, Point2D a) {
		base = b;
		dim = a;
		tex = new BufferedImage(1,1,1);
	}
	public boolean within(Point2D p){
		return(p.x>=base.x&&p.y>=base.y&&p.x<=base.x+dim.x&&p.y<=base.y+dim.y);
	}
	public boolean istouching(Player comp){
		Rectangle pointcomp = new Rectangle(Point2D.add(comp.dim.scale(-0.5), base),Point2D.add(dim, comp.dim),tex);
		Point2D comp2 = comp.pos;
		return(comp2.x>=pointcomp.base.x&&comp2.y>=pointcomp.base.y&&comp2.x<=pointcomp.base.x+pointcomp.dim.x&&comp2.y<=pointcomp.base.y+pointcomp.dim.y);
	}
	public int side(Player comp){
		Rectangle pointcomp = new Rectangle(Point2D.add(comp.dim.scale(-0.5), base),Point2D.add(dim, comp.dim),tex);
		Point2D comp2 = comp.pos;
		Point2D dif = Point2D.add(comp2,pointcomp.base.scale(-1));
		dif = Point2D.add(dif, pointcomp.dim.scale(-0.5));
		boolean isy = Math.abs(dif.x/pointcomp.dim.x)<Math.abs(dif.y/pointcomp.dim.y);
		if(isy){
			if(dif.y>0){
				return 0;
			}else{
				return 2;
			}
		}else{
			if(dif.x>0){
				return 1;
			}else{
				return 3;
			}
			
		}
	}

}
