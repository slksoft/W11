import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class W11 extends JFrame implements ActionListener {
	// ################################################################################
	// ################################################################################
	// ################################################################################

	public static void main(String[] args) {
		new W11();
		// 
		
		///
	}

	// ################################################################################
	// ################################################################################
	// ################################################################################

	private static final String APP_TITLE = "W11";
	private static final int APP_TOP = 100;
	private	static final int APP_LEFT = 100;
	private static final int APP_WIDTH = 900;
	private static final int APP_HEIGHT = 600;
	private static final String COMMAND_PLUS = "+";
	private static final String COMMAND_MINUS = "-";
	private static final double BOUNCE_FRICTION = 0.9;
	private static final double GRAVITY_DELTA = 0.1;
	private static final int TIMETICK_MS = 25;
	private static final int BALL_COUNT = 200;
	private static final int BALL_SIZE = 10;
	private static final Random RANDOMS = new Random();

	// ================================================================================

	private GamePanel gamePanel;

	// ================================================================================

	private int gameWidth = 800;
	private int gameHeight = 500;
	private double gravity = 0;
	private int timetickCount = 0;
	private ArrayList<GameBall> balls = new ArrayList<GameBall>();
	
	// ================================================================================
	// ================================================================================
	// ================================================================================
	
	public W11() {
		super(APP_TITLE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(APP_WIDTH, APP_HEIGHT));
		setLayout(new BorderLayout());
		setUI();
		setActors();
		pack();
		this.setLocation(new Point(APP_LEFT, APP_TOP));
		setVisible(true);		
	}

	// ================================================================================

	public void setUI(){

		JPanel commandPanel = new JPanel();
		add(BorderLayout.SOUTH, commandPanel);
		
		JButton minusButton = new JButton(COMMAND_MINUS);
		commandPanel.add(minusButton);
		minusButton.setActionCommand(COMMAND_MINUS);

		JButton plusButton = new JButton(COMMAND_PLUS);
		commandPanel.add(plusButton);
		plusButton.setActionCommand(COMMAND_PLUS);	

		minusButton.addActionListener(this);
		plusButton.addActionListener(this);

		gamePanel = new GamePanel(); 
		add(BorderLayout.CENTER, gamePanel);
	}

	// ================================================================================

	public void setActors() {

		// game objects
        for(int n = 0; n < BALL_COUNT / 2; n++) {
        	balls.add(new GameBall((n * 7 * BALL_SIZE) % gameWidth, (n * 17 * BALL_SIZE) % gameHeight));
        	balls.add(new GameBall((n * 11 * BALL_SIZE) % gameWidth, (n * 13 * BALL_SIZE) % gameHeight));
        }
		
		// time thread
		Runnable chronos = new Runnable() {		
			@Override
			public void run() {
				while(true) {
					timetickCount++;
			        for(GameBall ball : balls) {
			        	ball.tick();
			        }
					manageCollisions();
					gamePanel.update();
					try {
						Thread.sleep(TIMETICK_MS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		Thread chronoThread = new Thread(chronos);
        chronoThread.start();       
	}

	// ================================================================================

	public void manageCollisions() {
        for(GameBall ball : balls) ball.bounced = false;
        for(GameBall ball1 : balls) {
        	if (ball1.bounced) continue;
            for(GameBall ball2 : balls)
            {
            	if (ball2.bounced) continue;
            	if (ball1 == ball2) continue;
            	
            	int xDiff = (int) (ball1.x - ball2.x);
            	int yDiff = (int) (ball1.y - ball2.y);

            	if (xDiff > -BALL_SIZE && xDiff < BALL_SIZE && yDiff > -BALL_SIZE && yDiff < BALL_SIZE) {
            		ball1.xSpeed = - ball1.xSpeed;
            		ball2.xSpeed = - ball2.xSpeed;
            		ball1.bounced = true;
            		ball2.bounced = true;
            		break;
            	}
            }
        }
	}
	
	// ================================================================================
	// ================================================================================
	// ================================================================================

	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		if (command.equals(COMMAND_MINUS)) {
			gravity -= GRAVITY_DELTA;
			gamePanel.update();
			
		}
		else if (command.equals(COMMAND_PLUS)) {
			gravity += GRAVITY_DELTA;
			gamePanel.update();
		}
	}

	// ################################################################################
	// ################################################################################
	// ################################################################################

	public class GamePanel extends JPanel {
		// ================================================================================

		public void update() {
			setTitle(APP_TITLE + " G=" + String.format("%3.1f", gravity) + " T=" + timetickCount);
			repaint();		
		}

		// ================================================================================

		public void paintComponent(Graphics g) {
	        g.clearRect(0, 0, getWidth(), getHeight());
	        g.drawRect(0, 0, gameWidth + BALL_SIZE, gameHeight + BALL_SIZE);
	        for(GameBall ball : balls) {
	        	g.fillRect ((int) ball.x, (int) ball.y, BALL_SIZE, BALL_SIZE);
	        }
	    }		

		// ================================================================================
	}

	// ################################################################################
	// ################################################################################
	// ################################################################################

	public class GameBall {		
		// ================================================================================

		private boolean bounced;
		private double x;
		private double y;
		private double xSpeed = 2;
		private double ySpeed = 2;
		
		// ================================================================================

		public GameBall(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
		// ================================================================================

		public void tick() {
			ySpeed =  ySpeed + gravity;
			x += xSpeed;
			if (x > gameWidth) {
				xSpeed = - xSpeed;
				ySpeed = BOUNCE_FRICTION * ySpeed;
				x = gameWidth - (x - gameWidth);
			}
			if (x < 0) {
				xSpeed = -xSpeed;
				ySpeed = BOUNCE_FRICTION * ySpeed;
				x = -x;
			}
			y += ySpeed;
			if (y > gameHeight) {
				ySpeed = -BOUNCE_FRICTION * ySpeed;
				y = gameHeight - (y - gameHeight);
			}
			if (y < 0) {
				ySpeed = -BOUNCE_FRICTION * ySpeed;
				y = -y;
			}
		}
		
		// ================================================================================
	}

	// ################################################################################
	// ################################################################################
	// ################################################################################
}
