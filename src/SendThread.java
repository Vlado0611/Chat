import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class SendThread implements Runnable{

	private String name;

	public static final String EXCHANGE_NAME = "chat";
	
	public SendThread(String name) {
		this.name = name;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));;
		
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    
	    try (Connection connection = factory.newConnection();
		        Channel channel = connection.createChannel()) {
		        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
		        
		        String beginMessage = "BEGIN\r\n";
		        beginMessage += name + " has entered the chat \r\n";
		        channel.basicPublish(EXCHANGE_NAME, "", null, beginMessage.getBytes("UTF-8"));
		        
		        while(true) {
			        String message = userIn.readLine();
			        
			        String rabbitMessage = rabbitMessage(message);
			        channel.basicPublish(EXCHANGE_NAME, "", null, rabbitMessage.getBytes("UTF-8"));
		        }
		    } catch (IOException | TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public String rabbitMessage(String message) {
		
		String rabbitMessage = "";

		if(message.endsWith(".jpg") || message.endsWith(".png")) {
			rabbitMessage = "IMAGE\r\n";
			rabbitMessage += this.name +"\r\n";
			rabbitMessage += message.substring(message.lastIndexOf("\\")+1, message.length()) +"\r\n";
			
			File image = new File(message);
			String imgInString = toBase64(image);
			
			rabbitMessage += imgInString;
		}
		else {
			rabbitMessage = "TEXT\r\n";
			rabbitMessage += this.name + "\r\n";
			rabbitMessage += "\r\n";
			rabbitMessage += message;
		}
		
		return rabbitMessage;
	
	}
	
	public String toBase64(File f) {
		
		FileInputStream fileInputStreamReader;
		
		try {
			fileInputStreamReader = new FileInputStream(f);
			byte[] bytes = new byte[(int)f.length()];
	        fileInputStreamReader.read(bytes);
	        String encodedFile = Base64.getEncoder().encodeToString(bytes);
	        fileInputStreamReader.close();
	        return encodedFile;
	        
		}
		catch(Exception ex) {
			return null;
		}
		
	}
	
}
