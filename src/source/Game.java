package source;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import vectors.Point2D;

public class Game extends JPanel implements Runnable, KeyListener, MouseMotionListener, MouseListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9020477412984598531L;
	final static float blurriness = 1f;
	JFrame frame;
	BufferedImage screen;
	BufferedImage comblayers;
	BufferedImage fscreen;
	Graphics2D graphics;
	Graphics2D copier;
	Graphics2D layerer;
	int screenwidth;
	int screenheight;
	int particlecount;
	int fps = 0;
	int threadnum = 0;
	int[] wait = new int[1000];
	int timemilis;
	boolean[] ispressed = new boolean[500];
	static BufferedImage[] images;
	Player mainplayer;
	boolean onground = false;
	boolean direction = true;
	Point2D Camera = new Point2D(0, 0);
	ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	ArrayList<Rectangle> walls = new ArrayList<Rectangle>();
	ArrayList<Enemy> enemies = new ArrayList<Enemy>();
	Point2D Mouse;
	int reloadreq = 1000;
	int firetime = 0;
	int reloadtime = reloadreq;
	int clipsize = reloadreq;
	boolean reloading = false;
	boolean[] isheld = new boolean[4];
	int points = 0;
	float[] sharpen = new float[] { 1/9f,1/9f,1/9f,1/9f,1/9f,1/9f,1/9f,1/9f,1/9f };
	Kernel k = new Kernel(3, 3, sharpen);
	ConvolveOp cop = new ConvolveOp(k);
	Point2D newbase;
	boolean dead = false;
	boolean won = false;
	boolean playing = true;
	int money=0;
	int speed = 1000;
	int pershot = 10;
	int clipcapacity = 1000;

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Game a = new Game(1080, 720);

	}

	public Game(int width, int height) {
		loadimages();
		screenwidth = width;
		screenheight = height;
		loadmap();
		mainplayer = new Player(new Point2D(screenwidth / 2, screenheight / 2), new Point2D(20, 40));
		frame = new JFrame();
		frame.setDefaultCloseOperation(3);
		frame.setResizable(false);
		frame.add(this);
		this.setPreferredSize(new Dimension(screenwidth, screenheight));
		addKeyListener(this);
		addMouseMotionListener(this);
		addMouseListener(this);
		frame.addKeyListener(this);
		frame.addMouseMotionListener(this);
		frame.addMouseListener(this);
		setSize(screenwidth, screenheight);
		frame.pack();
		frame.setLocationRelativeTo(null);
		screen = new BufferedImage(screenwidth, screenheight, BufferedImage.TYPE_INT_ARGB);
		fscreen = new BufferedImage(screenwidth, screenheight, BufferedImage.TYPE_INT_ARGB);
		comblayers = new BufferedImage(screenwidth, screenheight, BufferedImage.TYPE_INT_ARGB);
		graphics = screen.createGraphics();
		copier = fscreen.createGraphics();
		layerer = comblayers.createGraphics();
		frame.setVisible(true);
		new Thread(this).start();
		new Thread(this).start();
		new Thread(this).start();
	}

	private void loadimages() {
		URL txt = getClass().getClassLoader().getResource("images/meta.txt");
		int imagecount = 0;
		try {
			Scanner in = new Scanner(txt.openStream());
			imagecount = Integer.parseInt(in.nextLine());
			in.close();
		} catch (FileNotFoundException e1) {
		} catch (IOException e) {
		}
		images = new BufferedImage[imagecount];
		for (int i = 0; i < imagecount; i++) {
			URL image = getClass().getClassLoader().getResource("images/" + i + ".png");
			try {
				images[i] = ImageIO.read(image);
				System.out.println("images/" + i + ".png");
			} catch (IOException e) {
			}
		}

	}

	private void loadmap() {
		
	};

	public synchronized void paint(Graphics g) {
		g.drawImage(fscreen, 0, 0, null);
	}

	protected void graphicsupdate() throws ConcurrentModificationException {
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		float[] colorScales = { 1f, 1f, 1f, 0.75f };
		float[] offsets = { 0, 0, 0, 0 };
		RescaleOp rop = new RescaleOp(colorScales, offsets, null);
		BufferedImage filtered = rop.filter(images[0].getSubimage(0, 0, Math.min(screenwidth, images[0].getWidth()),
				Math.min(screenheight, images[0].getHeight())), null);
		graphics.drawImage(filtered, 0, 0, screenwidth, screenheight, null);
		graphics.setColor(Color.white);
		if (direction) {
			graphics.drawImage(images[2], (int) mainplayer.pos.x - (int) (mainplayer.dim.x * 0.5),
					screenheight - (int) mainplayer.pos.y - (int) (mainplayer.dim.y * 0.5), (int) mainplayer.dim.x,
					(int) mainplayer.dim.y, null);
		} else {
			graphics.drawImage(images[3], (int) mainplayer.pos.x - (int) (mainplayer.dim.x * 0.5),
					screenheight - (int) mainplayer.pos.y - (int) (mainplayer.dim.y * 0.5), (int) mainplayer.dim.x,
					(int) mainplayer.dim.y, null);

		}
		for (Bullet b : bullets) {
			graphics.drawImage(images[1],(int) b.pos.x -3, screenheight - (int) b.pos.y - 3, 6, 6,null);
		}
		for (Rectangle r : walls) {
			graphics.drawImage(r.tex, (int) (r.base.x), (int) (screenheight - r.base.y - r.dim.y), (int) (r.dim.x),
					(int) (r.dim.y), null);
		}
		for (Enemy e : enemies) {
			graphics.drawImage(e.hitbox.tex, (int) (e.hitbox.base.x),
					(int) (screenheight - e.hitbox.base.y - e.hitbox.dim.y), (int) (e.hitbox.dim.x),
					(int) (e.hitbox.dim.y), null);
		}
		if(dead){
			playing = false;
			graphics.setColor(new Color(0xffff0000));
			graphics.setFont(new Font("Futura", Font.PLAIN, 200));
			graphics.drawString("You Lost", 150, 360);
			graphics.setFont(new Font("Futura", Font.PLAIN, 50));
			graphics.drawString("You Ran Out of Time", 300, 410);
		}
		if(won){
			playing = false;
			graphics.setColor(new Color(0xff00ff00));
			graphics.setFont(new Font("Futura", Font.PLAIN, 200));
			graphics.drawString("You Won", 150, 360);
			graphics.setFont(new Font("Futura", Font.PLAIN, 50));
			graphics.drawString("Congratulations, you beat wave 30", 300, 410);
		}
		layerer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		layerer.drawImage(screen, 0, 0, null);
		layerer.setColor(new Color(0xffffffff));
		layerer.setFont(new Font("Futura", Font.PLAIN, 40));
		layerer.drawString("Points: " + String.valueOf(points), 0, 40);
		layerer.drawString("Ammo: " + String.valueOf(clipsize), 0, 80);
		layerer.drawString("Time Till Next Nave: " + String.valueOf(60-timemilis%60000/1000), 0, 120);
		layerer.drawString("Round: " + String.valueOf(timemilis/60000+1), 0, 160);
		layerer.drawString("Money: " + String.valueOf(money)+"$", 0, 200);
		copier.clearRect(0, 0, screenwidth, screenheight);
		copier.drawImage(comblayers, 0, 0, null);

	}
	protected void contentupdate() {
		playerupdate();
		bulletupdate();
		enemyupdate();
	}

	private void enemyupdate() {
		if (Math.random() < 0.01&&timemilis%60000<(1000*timemilis/60000+1000)) {
			enemies.add(new Jitterer(new Rectangle(
					new Point2D(Math.random() * screenwidth, Math.random() * screenheight), new Point2D(50, 50))));
		}
		dead|=timemilis%60000==50000&&enemies.size()>0;
		won|=timemilis/60000==30;
		//TODO
		for (Enemy e : enemies) {
			e.update();
			Point2D base = e.hitbox.base;
			boolean within = false;
			for (Rectangle r : walls) {
				within |= r.within(base);
			}
			if (base.x < 0 || base.x > screenwidth || base.y < 0 || base.y > screenheight || within) {
				e.hitbox.base = new Point2D(Math.random() * screenwidth, Math.random() * screenheight);
			}
		}
		for(int i = 0 ;i<enemies.size();i++){
			if(enemies.get(i).health<0){
				enemies.remove(i);
				i--;
				points+=100;
				money+=100;
			}
		}
	}

	private void playerupdate() {
		collisiondetectionupdate();
		wallcollisionupdate();
		keydetectionupdate();
		physicsupdate();
	}

	private void collisiondetectionupdate() {
		onground = false;
		if (mainplayer.pos.y <= mainplayer.dim.y * 0.5) {
			mainplayer.pos.y = mainplayer.dim.y * 0.5;
			mainplayer.vel.y = 0;
			onground = true;
		}
		if (mainplayer.pos.y >= screenheight - mainplayer.dim.y * 0.5) {
			mainplayer.pos.y = screenheight - mainplayer.dim.y * 0.5;
			mainplayer.vel.y *= -0.5;
		}
		if (mainplayer.pos.x < mainplayer.dim.x * 0.5) {
			mainplayer.pos.x = mainplayer.dim.x * 0.5;
			mainplayer.vel.x *= -0.5;
		}
		if (mainplayer.pos.x > screenwidth - mainplayer.dim.x * 0.5) {
			mainplayer.pos.x = screenwidth - mainplayer.dim.x * 0.5;
			mainplayer.vel.x *= -0.5;
		}
	}

	private void wallcollisionupdate() {
		for (Rectangle r : walls) {
			int side = r.side(mainplayer);
			if (side == 0) {
				if (r.istouching(mainplayer)) {
					onground = true;

					mainplayer.pos.y = r.base.y + r.dim.y + mainplayer.dim.y * 0.5;
					mainplayer.vel.y = 0;
				}
			} else if (side == 1 || side == 3) {
				if (r.istouching(mainplayer)) {
					mainplayer.pos.x = mainplayer.prevpos.x;
					mainplayer.vel.x *= -0.5;
				}
			} else {
				if (r.istouching(mainplayer)) {
					mainplayer.pos = mainplayer.prevpos;
					mainplayer.vel.y = 0;
				}
			}
			if (r.istouching(mainplayer) && r.side(mainplayer) != 0) {
			}

		}
	}

	private void physicsupdate() {
		mainplayer.prevpos = mainplayer.pos;
		mainplayer.pos = Point2D.add(mainplayer.pos, mainplayer.vel);
		mainplayer.vel.y *= 0.995;
		if (onground) {
			mainplayer.vel.x *= 0.985;
		} else {
			mainplayer.vel.x *= 0.999;
		}
		if (!onground) {
			mainplayer.vel.y -= 0.01;
		}
	}

	private void keydetectionupdate() {
		if (onground) {
			if (ispressed[KeyEvent.VK_A]) {
				mainplayer.vel.x -= 0.01;
				direction = true;
			}
			if (ispressed[KeyEvent.VK_D]) {
				mainplayer.vel.x += 0.01;
				direction = false;
			}
		} else {
			if (ispressed[KeyEvent.VK_A]) {
				mainplayer.vel.x -= 0.0005;
			}
			if (ispressed[KeyEvent.VK_D]) {
				mainplayer.vel.x += 0.0005;

			}
		}
		if (ispressed[KeyEvent.VK_SPACE] && onground) {
			mainplayer.vel.y += 2;
			onground = false;
		}
		if (ispressed[KeyEvent.VK_R]) {
			reloadtime = reloadreq;
			reloading = true;
		}
	}

	private void bulletupdate() {
		firetime--;
		reloadtime--;
		if (clipsize < 1 && !reloading) {
			reloadtime = reloadreq;
			reloading = true;
		}
		if (reloadtime == 0) {
			reloading = false;
			clipsize = reloadreq;
		}
		if (firetime < 0 && isheld[MouseEvent.BUTTON1] && reloadtime < 0) {
			Point2D dif = Point2D.add(Mouse, mainplayer.pos.scale(-1));
			direction = dif.x < 0;
			double angle = dif.angle();
			for (int i = 0; i < 10; i++) {
				double offsetangle = angle + (Math.random() - 0.5) * 0.1;
				bullets.add(new Bullet(mainplayer.pos,
						new Point2D(Math.sin(offsetangle), Math.cos(offsetangle)).scale(3 + Math.random())));
				mainplayer.vel = Point2D.add(mainplayer.vel,
						new Point2D(Math.sin(offsetangle), Math.cos(offsetangle)).scale(-0.01));
				clipsize--;
			}
			firetime = 200;
		}
		for (Bullet b : bullets) {
			b.update();
		}
		for (int i = 0; i < bullets.size(); i++) {
			Bullet b = bullets.get(i);
			boolean within = false;
			for (Rectangle r : walls) {
				within |= r.within(bullets.get(i).pos);
			}
			if (b.pos.x < 0 || b.pos.x > screenwidth || b.pos.y < 0 || b.pos.y > screenheight || within) {
				bullets.remove(i);
				i--;
			}
		}
		for (int i = 0; i < bullets.size(); i++) {
			Bullet b = bullets.get(i);
			boolean within = false;
			for (int j = 0; j < enemies.size(); j++) {
				Enemy e = enemies.get(j);
				within |= e.hitbox.within(bullets.get(i).pos);
				if (e.hitbox.within(bullets.get(i).pos)) {
					enemies.get(j).health-=1;
					points++;
				}
			}
			if (b.pos.x < 0 || b.pos.x > screenwidth || b.pos.y < 0 || b.pos.y > screenheight || within) {
				bullets.remove(i);
				i--;
			}
		}
	}

	public void run() {
		threadnum++;
		if (threadnum == 1) {
			while (playing) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
				try {
					graphicsupdate();
					repaint();
				} catch (ConcurrentModificationException e) {
				}

			}
		}
		if (threadnum == 2) {
			while (playing) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
				contentupdate();
				for (int i = 0; i < 1000; i++) {
					wait[i]++;
				}
			}
		}
		if (threadnum == 3) {
			while (true) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
				fps = wait[1];
				for (int i = 1; i < 1000; i++) {
					wait[i - 1] = wait[i];
				}
				wait[999] = 0;
				timemilis++;
			}

		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		ispressed[e.getKeyCode()] = true;
	}

	public void keyReleased(KeyEvent e) {
		ispressed[e.getKeyCode()] = false;
	}

	public void mouseDragged(MouseEvent e) {
		Mouse = new Point2D(e.getX(), screenheight - e.getY());
	}

	public void mouseMoved(MouseEvent e) {
		Mouse = new Point2D(e.getX(), screenheight - e.getY());

	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		isheld[e.getButton()] = true;
		if (e.getButton() == 3) {
			newbase = Mouse.copy();
		}
	}

	public void mouseReleased(MouseEvent e) {
		isheld[e.getButton()] = false;
		if (e.getButton() == 3) {
			walls.add(new Rectangle(newbase.copy(), Point2D.add(newbase.copy().scale(-1), Mouse.copy()), images[1]));
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
}