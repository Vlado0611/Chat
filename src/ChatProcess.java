import java.io.File;
import java.util.Random;

public class ChatProcess {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Random r = new Random();
		String name = "Guest"+r.nextInt(100);
		String path = "C:\\Users\\vlaki\\eclipse-drs\\ChatRabbitMQ\\src\\" + name;
		
		File pathAsFile = new File(path);
		if(!pathAsFile.exists()) {
			pathAsFile.mkdir();
		}
		
		ReceiveThread receive = new ReceiveThread(name, path);
		SendThread send = new SendThread(name);
		
		Thread t1 = new Thread(receive);
		Thread t2 = new Thread(send);
		
		t1.start();
		t2.start();
		
		try {
			t1.join();
			t2.join();
		}
		catch(Exception ex) {
			
		}
		
	}

}
