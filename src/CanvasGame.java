import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class CanvasGame extends Canvas {
	public static CanvasGame instance = null;
	public static ElementManager gameElements = new ElementManager();
	public static CharBilly billy;
	public static TileMap map;
	
	public static BufferedImage charsetBilly;
	public static BufferedImage charsetDemon;
	public static BufferedImage tileset;
	public BufferedImage loadingScreen = GamePanel.loadImage("backgrounds/loading_background.png");
	
	public static String strMap01 = new String("maps/hell_01.map");
	public static String strTileset01 = new String("maps/hell_tileset.png");
	public static String strElements01 = new String("csv/hell_01.csv");
	
	Random rand = new Random();
	
	public static ArrayList<Character> enemiesList = new ArrayList<Character>();
	public static ArrayList<Projectile> projectilesList = new ArrayList<Projectile>();
	public static ArrayList<Checkpoint> checkpoints = new ArrayList<Checkpoint>();
	public static ArrayList<Effect> effectsList = new ArrayList<Effect>();
	
	public static boolean B_KEY_LEFT, B_KEY_RIGHT, B_KEY_JUMP, B_KEY_FIRE;
	public static boolean MOUSE_PRESSED;
	public static int MOUSE_X, MOUSE_Y;
	public static int MOUSE_CLICK_X, MOUSE_CLICK_Y;
	public static boolean loading;
	public int loadTime;
	public int mapPositionX;
	public int mapPositionY;
	public int respawnTime = 2000;
	public static int deathCounter = 0;
	public static int projectilesCounter = 0;
	
	public CanvasGame(int levelId) {
		instance = this;
		
		charsetBilly = GamePanel.loadImage("sprites/billy.png");
		charsetDemon = GamePanel.loadImage("sprites/demon.png");
		
		MOUSE_X = 0;
		MOUSE_Y = 0;
		MOUSE_CLICK_X = 0;
		MOUSE_CLICK_Y = 0;
		MOUSE_PRESSED = false;
		loading = true;
	}
	
	@Override
	public void selfSimulates(long diffTime) {
		if(!loading) {
			if(billy.isAlive) {
				billy.selfSimulates(diffTime);
				mapPositionX = (int)billy.x;
				mapPositionY = (int)billy.y;
				map.Positions(mapPositionX-GamePanel.PANEL_WIDTH/2, mapPositionY-GamePanel.PANEL_HEIGHT/2);
			} else {
				billy.respawnCountTime += diffTime;
				if(billy.respawnCountTime >= respawnTime) {
					billy.respawnCountTime = 0;
					billy.respawn();
				}
			}
		
			for(int i = 0; i < projectilesList.size(); i++){
				projectilesList.get(i).selfSimulates(diffTime);
				if(!projectilesList.get(i).active){
					projectilesList.remove(i);
					i--;
				}
			}
			
			for(int i = 0; i < enemiesList.size(); i++) {
				enemiesList.get(i).selfSimulates(diffTime);
				if(!enemiesList.get(i).isAlive){
					enemiesList.remove(i);
					i--;
				}
			}
			
			for(Checkpoint c : checkpoints) {
				c.selfSimulates(diffTime);
				if(c.respawnBilly && !c.hasBeenActived) {
					billy.respawnCountTime += diffTime;
					if(billy.respawnCountTime >= 1000) {
						billy.respawnCountTime = 0;
						billy.respawn();
						c.respawnBilly = false;
						c.hasBeenActived = true;
					}
				}
			}
			
			for(int i = 0; i < effectsList.size(); i++){
				effectsList.get(i).selfSimulates(diffTime);
				if(effectsList.get(i).active == false){
					effectsList.remove(i);
					i--;
				}
			}
		} else {
			loadTime += diffTime;
			if(loadTime >= 4000) {
				loadTime = 0;
				loading = false;
			}
		}
	}
	
	@Override
	public void selfDraws(Graphics2D dbg){
		map.selfDraws(dbg);
		
		for(int i = 0; i < projectilesList.size(); i++){
			projectilesList.get(i).selfDraws(dbg, map.MapX, map.MapY);
		}
		for(int i = 0; i < enemiesList.size(); i++) {
			enemiesList.get(i).selfDraws(dbg, map.MapX, map.MapY);
		}
		for(Checkpoint c : checkpoints) {
			c.selfDraws(dbg, map.MapX, map.MapY);
		}
		for(int i = 0; i < effectsList.size(); i++){
			effectsList.get(i).selfDraws(dbg, map.MapX, map.MapY);
		}
		
		billy.selfDraws(dbg, map.MapX, map.MapY);
		
		
		if(loading) {
			dbg.setColor(Color.BLACK);
			dbg.fillRect(0, 0, GamePanel.PANEL_WIDTH, GamePanel.PANEL_HEIGHT);
			dbg.drawImage(loadingScreen, 242, 274, 559, 326, 0, 0, loadingScreen.getWidth(), loadingScreen.getHeight(), null);
		}
	}

	@Override
	public void keyPressed(KeyEvent k){
		int keyCode = k.getKeyCode();
		if(keyCode == KeyEvent.VK_A)		{ B_KEY_LEFT = true; }
		if(keyCode == KeyEvent.VK_D)		{ B_KEY_RIGHT = true; }
		if(keyCode == KeyEvent.VK_W)		{ B_KEY_JUMP  = true; }
		if(keyCode == KeyEvent.VK_SPACE)	{ B_KEY_FIRE  = true; }
		if(keyCode == KeyEvent.VK_F)	{ GamePanel.showFps = !GamePanel.showFps; }
		if(keyCode == KeyEvent.VK_ESCAPE) {
			if(CanvasPause.instance == null) {
				CanvasPause pause = new CanvasPause();
			}
			GamePanel.canvasActive = CanvasPause.instance;
		}
	}

	@Override
	public void keyReleased(KeyEvent k){
		int keyCode = k.getKeyCode();
		if(keyCode == KeyEvent.VK_A)		{ B_KEY_LEFT  = false; }
		if(keyCode == KeyEvent.VK_D)		{ B_KEY_RIGHT = false; }
		if(keyCode == KeyEvent.VK_W)		{ B_KEY_JUMP  = false; }
		if(keyCode == KeyEvent.VK_SPACE)	{ B_KEY_FIRE  = false; }
	}

	@Override
	public void mouseMoved(MouseEvent m) {
		MOUSE_X = m.getX();
		MOUSE_Y = m.getY();
	}

	@Override
	public void mouseDragged(MouseEvent m) { }
	
	@Override
	public void mousePressed(MouseEvent m) {
		MOUSE_PRESSED = true;
		MOUSE_CLICK_X = m.getX();
		MOUSE_CLICK_Y = m.getY();
	}

	@Override
	public void mouseReleased(MouseEvent m) { 
		MOUSE_PRESSED = false;
	}
	
	public static void resetControls() {
		B_KEY_LEFT = false;
		B_KEY_RIGHT = false;
		B_KEY_JUMP = false; 
		B_KEY_FIRE = false;
	}
	
	public static void setGameLevel(int levelId) {
		if(levelId == 1) {
			enemiesList.clear();
			projectilesList.clear();
			checkpoints.clear();
			gameElements.elementsList.clear();
			tileset = GamePanel.loadImage(strTileset01);
			map = new TileMap(CanvasGame.tileset, (GamePanel.PANEL_WIDTH>>4)+(((GamePanel.PANEL_WIDTH&0x000f)>0)?1:0), (GamePanel.PANEL_HEIGHT>>4)+(((GamePanel.PANEL_HEIGHT%16)>0)?1:0));
			map.OpenMap(strMap01);
			gameElements = new ElementManager(strElements01);
			gameElements.decodeElements();
		}
	}
}