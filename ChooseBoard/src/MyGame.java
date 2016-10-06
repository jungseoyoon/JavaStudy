import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Random;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.JFrame;
import javax.swing.WindowConstants;


public class MyGame extends JFrame implements KeyListener{

	
	//static GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
	
	final public int gGameWidth = 640;
	final public int gGameHeight = 480;
	
	GameScreen gamescreen;
	
	public static void main(String[] args){
		
		new MyGame();
	}
	
	MyGame(){
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int f_xpos = (int) (screen.getWidth() / 2 - gGameWidth / 2);//창 위치를 모니터 화면의 중앙에 위치시키기 위한 값
		int f_ypos = (int) (screen.getHeight() / 2 - gGameHeight / 2);
		
		//창 선언
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);//창 닫기 가능
		setUndecorated(true);//프레임 없음
		//set
		setBackground(new Color(0xffffff));//윈도우 기본 배경색 지정 (R=ff, G=ff, B=ff : 하얀색)
		setTitle("게임제작용 범용 프레임워크");//윈도우 이름 지정
		setLayout(null);//윈도우의 레이아웃을 프리로 설정
		setBounds(f_xpos,f_ypos,gGameWidth,gGameHeight);//윈도우의 시작 위치와 너비 높이 지정
		setResizable(false);//윈도우의 크기를 변경할 수 없음
		setVisible(true);//윈도우 표시

		addKeyListener(this);//키 리스너 등록
		
		gamescreen=new GameScreen(this);//화면 묘화를 위한 캔버스 객체
		gamescreen.setBounds(0,0,gGameWidth,gGameHeight);
		add(gamescreen);//Canvas 객체를 프레임에 올린다
	}

	//키 입력 처리
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		System.out.println("" + e);
		gamescreen.keyTyped(e);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		System.out.println("" + e);
		gamescreen.keyPressed(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		System.out.println("" + e);
		gamescreen.keyReleased(e);
	}


}

class GameScreen extends Canvas implements Runnable{
	
	MyGame frame;
	
	BufferedImage buff;//더블버퍼링용 백버퍼
	Graphics2D gc2;//더블버퍼링용 그래픽 컨텍스트
	
	BufferedImage ImgBuf;
	BufferedImage ImgBuf2;
	
	Random rnd = new Random();
	
	int dCnt;//화면 그리기용 cnt
	int pCnt;//process용 cnt
	int delay;//루프 딜레이. 1/1000초 단위.
	long pretime;//루프 간격을 조절하기 위한 시간 체크값
	
	int mode;//게임 제어를 위한 화면 전체 상태변수
	boolean isPause;//게임 일시정지
	
	GameScreen(MyGame frame){
		
		this.frame = frame;
		setFocusable(false);
		
		dCnt = 0;
		pCnt = 0;
		mode = 0;
		delay = 15;
		isPause = false;
		
		ImgBuf = makeBufferedImage("C:\\JavaProject\\ChooseBoard\\bin\\img_0256") ;
		ImgBuf2 = makeBufferedImage("C:\\JavaProject\\ChooseBoard\\bin\\img_3") ;
		
		Thread thread = new Thread(this);
		thread.start();
		
		System.out.println("(0,0) to (100,0)'s degree = " + getAngle(0, 0, 100, 0));
		System.out.println("(0,0) to (100,100)'s degree = " + getAngle(0, 0, 100, 100));
		System.out.println("(0,0) to (0,100)'s degree = " + getAngle(0, 0, 0, 100));
		System.out.println("(100,100) to (200,0)'s degree = " + getAngle(100, 100, 200, 0));
	}

	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub
		if(gc2==null){
			
			buff = new BufferedImage(frame.gGameWidth, frame.gGameHeight, BufferedImage.TYPE_INT_ARGB);//오프스크린 버퍼
			if(buff!=null)
				gc2 = buff.createGraphics();
			return;
		}
		update(g);
	}

	@Override
	public void update(Graphics g) {
		// TODO Auto-generated method stub
		if(gc2==null) return;
		
		dblpaint();//오프스크린 버퍼에 그리기
		g.drawImage((Image)buff, 0,0, frame.gGameWidth, frame.gGameHeight, this);//오프스크린 버퍼를 메인화면에 그린다.
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			try{
				pretime=System.currentTimeMillis();
				process();
				repaint();

				if(System.currentTimeMillis()-pretime<delay) Thread.sleep(delay-System.currentTimeMillis()+pretime);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	void process(){
		
		if(isPause){
			//엄밀히 말하면 pause도 일종의 mode이지만..
			process_PAUSE();//일시정지중에 처리할 예외적인 내용
			return;//일시정지중에는 모든 process를 중지한다
		}

		switch(mode){
		case 0:
			process_TITLE();
			break;
		case 1:
			process_INTRO();
			break;
		case 2:
			process_PLAY();
			break;
		case 3:
			process_GAMEOVER();
			break;
		default:
			break;
		}

		pCnt++;
		
	}

	void dblpaint(){
		
		//화면 갱신은 isPause의 영향을 받지 않는다
		//단, 그러기 위해서 draw_에서는 어떤 그림의 상태변화도 있어서는 안된다.
		//process용과 draw용 cnt를 따로 돌리는게 좋다..

		gc2.setColor(Color.WHITE);
		gc2.fillRect(0,  0, frame.gGameWidth, frame.gGameHeight);
		
		switch(mode){
		
		case 0:
			draw_TITLE();
			break;
		case 1:
			draw_INTRO();
			break;
		case 2:
			draw_PLAY();
			break;
		case 3:
			draw_GAMEOVER();
			break;
		default:
			break;
		}
		
		dCnt++;
		
		//test
		drawBImage(gc2, getRotateImage(getScaledImage(ImgBuf2, 240+Math.abs(dCnt%100-50),240+Math.abs(dCnt%100-50)), 360-pCnt%360), 320, 240, this);
		drawBImage(gc2, getRotateImage(getScaledImage(ImgBuf, 360,240), pCnt%360), 0, 0, this);
		drawBImage(gc2, getRotateImage(getScaledImage(ImgBuf, 240,360), 360-pCnt%360), 640, 0, this);
		drawBImage(gc2, getRotateImage(getScaledImage(ImgBuf, 480,480), 360-pCnt%360), 0, 480, this);
		drawBImage(gc2, getRotateImage(getScaledImage(ImgBuf, 360,360), pCnt%360), 640, 480, this);
	}
	
	
	//이 부분을 적당히(?) 채워넣는다
	void process_TITLE(){
		
	}
	void process_INTRO(){
		
	}
	void process_PLAY(){
		
	}
	void process_GAMEOVER(){
		
	}
	void process_PAUSE(){
		
	}
	
	//※draw~ 에서는 그림의 상태나 위치를 직접 변경하지 않도록 하는게 좋다
	void draw_TITLE(){
		
	}
	void draw_INTRO(){
		
	}
	void draw_PLAY(){
		
	}
	void draw_GAMEOVER(){
		
	}
	
	//키 입력 관련은 이쪽에서..
	public void keyTyped(KeyEvent e){
		
	}
	public void keyPressed(KeyEvent e){
		
	}
	public void keyReleased(KeyEvent e){
		
		//일시정지 테스트
		if(e.getKeyCode()==KeyEvent.VK_0)
			isPause = !isPause;
	}
	
	/*
	 * 자바 게임 프로그래밍에서 범용으로 사용할 수 있는 루틴 모음 
	 * 
	 * 이미지 크기 조절(확대/축소)
	 * 이미지 회전
	 * 배경음악 연주
	 * 효과음 발생
	 * 일정 범위의 난수 구하기
	 * 한 점에서 다른 점을 향한 각도(degree) 구하기
	 * 두 점 사이의 거리 구하기
	 * 상자충돌 여부
	 * 
	 */

	//중심점 앵커가 적용된 그리기
	void drawBImage(Graphics2D g, BufferedImage image, int x, int y, ImageObserver obs){
		//center anchor draw
		int w = image.getWidth();
		int h = image.getHeight();
		
		g.drawImage(image, x-w/2, y-h/2, obs);
	}
	
	//확대 축소된 BufferedImage 얻기
	BufferedImage getScaledImage(BufferedImage image, int width, int height){
		
		GraphicsConfiguration gc = frame.getGraphicsConfiguration();
		BufferedImage result = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
		Graphics2D g = result.createGraphics();
		
		double w = image.getWidth();
		double h = image.getHeight();
		g.scale((double)width/w, (double)height/h);
		g.drawRenderedImage(image, null);
		g.dispose();
		
		return result;
	}

	//회전시킨 BufferedImage 얻기
	BufferedImage getRotateImage(BufferedImage image, double angle){//angle : degree
		
		double _angle = Math.toRadians(angle);
		double sin = Math.abs(Math.sin(_angle));
		double cos = Math.abs(Math.cos(_angle));
		double w = image.getWidth();
		double h = image.getHeight();
		int newW = (int)Math.floor(w*cos + h*sin);
		int newH = (int)Math.floor(w*sin + h*cos);
		
		GraphicsConfiguration gc = frame.getGraphicsConfiguration();
		BufferedImage result = gc.createCompatibleImage(newW, newH, Transparency.TRANSLUCENT);
		Graphics2D g = result.createGraphics();
		
		g.translate((newW-w)/2, (newH-h)/2);
		g.rotate(_angle, w/2, h/2);
		g.drawRenderedImage(image, null);
		g.dispose();
		
		return result;
	}

	//Image 리소스 읽어들이기
	public Image makeImage(String furl){
		Image img;
		Toolkit tk=Toolkit.getDefaultToolkit();
		img=tk.getImage(furl);
		try {
			//여기부터
			MediaTracker mt = new MediaTracker(this);
			mt.addImage(img, 0);
			mt.waitForID(0);
			//여기까지, getImage로 읽어들인 이미지가 로딩이 완료됐는지 확인하는 부분
		} catch (Exception ee) {
			ee.printStackTrace();
			return null;
		}	
		return img;
	}
	
	//BufferedImage 리소스 읽어들이기 - Image를 BufferedImage로 바꾸는 방식 
	public BufferedImage makeBufferedImage(String furl){
		
		Image img = null;
		Toolkit tk=Toolkit.getDefaultToolkit();
		img=tk.getImage(furl);
		try {
			//여기부터
			MediaTracker mt = new MediaTracker(this);
			mt.addImage(img, 0);
			mt.waitForID(0);
			//여기까지, getImage로 읽어들인 이미지가 로딩이 완료됐는지 확인하는 부분
		} catch (Exception ee) {
			ee.printStackTrace();
			return null;
		}	

		BufferedImage bImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D bGr = bImg.createGraphics();
		bGr.drawImage(img, 0,0, null);
		bGr.dispose();

		return bImg;
	}

	//배경음악 연주 - 루프 여부 지정
	Clip bgmclip;
	public void playBgm(File file, boolean loop){
		try{
			//BGM은 임의의 시점에서 정지시킬 수 있어야 하므로 전역으로 전용 Clip을 사용한다
			bgmclip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));
			bgmclip.open(AudioSystem.getAudioInputStream(file));
			bgmclip.addLineListener(new LineListener() {
				@Override
				public void update(LineEvent event) {
					// TODO Auto-generated method stub
					System.out.println("" + event.getType());
					if(event.getType()==LineEvent.Type.STOP){
						bgmclip.close();
					}
				}
			});
			bgmclip.start();
			if(loop)
				bgmclip.loop(bgmclip.LOOP_CONTINUOUSLY);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//배경음악 정지
	public void stopBgm(){
		
		if(bgmclip!=null && bgmclip.isRunning()){
			bgmclip.stop();
			bgmclip.close();
		}
	}
	//효과음 발생 - 횟수 지정
	public void playSound(File file, int repeat){
		try{
			final Clip clip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));
			clip.open(AudioSystem.getAudioInputStream(file));
			clip.addLineListener(new LineListener() {
				@Override
				public void update(LineEvent event) {
					// TODO Auto-generated method stub
					System.out.println("" + event.getType());
					if(event.getType()==LineEvent.Type.STOP){
						//이 부분이 없으면 효과음이 메모리에 점점 쌓여서 언젠가 크래시된다
						clip.close();
					}
				}
			});
			clip.loop(repeat);
			clip.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//일정 범위의 난수 구하기
	public int RAND(int startnum, int endnum) //랜덤범위(startnum부터 ramdom까지), 랜덤값이 적용될 변수.
	{
		int a, b;
		if(startnum<endnum)
			b = endnum - startnum; //b는 실제 난수 발생 폭
		else
			b = startnum - endnum;
		a = Math.abs(rnd.nextInt()%(b+1));
		return (a+startnum);
	}
	//한 점에서 다른 점을 향한 각도 구하기
	int getAngle(int sx, int sy, int dx, int dy){
		int vx=dx-sx;
		int vy=dy-sy;
		double rad=Math.atan2(vy,vx);
		int degree=(int)(rad*57.3f)+90;
		if(degree<0)
			degree+=360;
		return degree;
	}
	//두 점 사이의 거리 구하기
	public int getDistance(int x1,int y1,int x2,int y2){
		return Math.abs((y2-y1)*(y2-y1)+(x2-x1)*(x2-x1));
		//실제 환산된 거리가 아니라는 점에 주의
	}
	//상자충돌 검출하기
	public boolean getCollision(int x1, int y1, Rectangle rect1, int x2, int y2, Rectangle rect2){
		
		boolean ret = false;

		if(rect1==null)
			return false;
		
		if(rect2==null)
			return false;

		Rectangle _rect1 = new Rectangle(x1+rect1.x, y1+rect1.y, rect1.width, rect1.height);
		Rectangle _rect2 = new Rectangle(x2+rect2.x, y2+rect2.y, rect2.width, rect2.height);
		
		if(
		_rect1.x < (_rect2.x+_rect2.width) &&
		_rect2.x < (_rect1.x+_rect1.width) &&
		_rect1.y < (_rect2.y+_rect2.height) &&
		_rect2.y < (_rect1.y+_rect1.height)
				)
			ret = true;
		
		return ret;
	}
	//상자충돌 검출하기 (절대좌표)
	public boolean getCollisionAb(Rectangle rect1, Rectangle rect2){
		
		boolean ret = false;

		if(rect1==null)
			return false;
		
		if(rect2==null)
			return false;

		if(
		rect1.x < (rect2.x+rect2.width) &&
		rect2.x < (rect1.x+rect1.width) &&
		rect1.y < (rect2.y+rect2.height) &&
		rect2.y < (rect1.y+rect1.height)
				)
			ret = true;
		
		return ret;
	}
	//이미지로부터 상자의 절대좌표범위 얻어내기
	public Rectangle getRectfromImage(BufferedImage image, int x, int y, float scale){
		
		Rectangle rect = new Rectangle(0,0, -1,-1);
		
		float width = ((float)image.getWidth() * scale);
		float height = ((float)image.getHeight() * scale);
		
		x = x - (int)width*50;
		y = y - (int)height*50;
		
		rect.x = x/100;
		rect.y = y/100;
		rect.width = (int)width;
		rect.height = (int)height;
		
		return rect;
	}
	
	//점 a에서 각도, 거리에 있는 점 b 구하기
	public Point getDegreePosition(int fx, int fy, int degree, int dist){
		
		Point pos = new Point(-1,-1);
		
		double rad = (degree-90)%360*Math.PI / 180;
		pos.x = (int)(fx + dist*Math.cos(rad));
		pos.y = (int)(fy + dist*Math.sin(rad));
		
		return pos;
	}
	
	//숫자 문자열에 , 넣기
	public String numComma(String numStr){
		
		DecimalFormat df = new DecimalFormat("#,###");
		numStr = df.format(Integer.parseInt(numStr));

		return numStr;
	}
	//그림숫자 그리기
	public void drawNumber(Graphics2D g, BufferedImage image, String numStr, int x, int y, ImageObserver obs){
		
		int width = image.getWidth(this)/11;
		int height = image.getHeight(this);
		
		int _xx = x;
		for(int i=0;i<numStr.length();i++)
			if(numStr.charAt(i)==',')
				drawImageClip(g, image, _xx+i*width, y, 10*width, 0, width,height, 0);
			else
				drawImageClip(g, image, _xx+i*width, y, (numStr.charAt(i)-'0')*width, 0, width,height, 0);
	}
	//이미지 클리핑
	public void drawImageClip(Graphics2D g, BufferedImage img, int x, int y, int sx,int sy, int wd,int ht, int anc){//sx,sy부터 wd,ht만큼 클리핑해서 그린다.
		if(x<0) {
			wd+=x;
			sx-=x;
			x=0;
		}
		if(y<0) {
			ht+=y;
			sy-=y;
			y=0;
		}
		if(wd<0||ht<0) return;
		x=x-(anc%3)*(wd/2);
		y=y-(anc/3)*(ht/2);
		g.setClip(x, y, wd, ht);
		g.drawImage(img, x-sx, y-sy, this);
		g.setClip(0,0, frame.gGameWidth,frame.gGameHeight);
	}
	//이미지의 일정 영역을 프레임화하여 중앙정렬로 출력
	public void drawFrame(Graphics2D g, BufferedImage img, int x, int y, int widthFrame, int heightFrame, int frameNum, float scale){
	
		int width = img.getWidth() / widthFrame;
		int height = img.getHeight() / heightFrame;
		int sx = (frameNum % widthFrame) * width;
		int sy = (frameNum / heightFrame) * height;

		if(scale==1.0f)
			drawImageClip(g, img, x, y, sx, sy, width, height, 4);
		else
			drawImageClip(g, getScaledImage(img, (int)(img.getWidth()*scale), (int)(img.getHeight()*scale)), x, y, (int)(sx*scale), (int)(sy*scale), (int)(width*scale), (int)(height*scale), 4);
	}
}
