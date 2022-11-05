import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class ReceiveThread implements Runnable {
	
	private String name;
	private String path;
	public static final String EXCHANGE_NAME = "chat";
	
	public ReceiveThread(String name, String path) {
		this.name = name;
		this.path = path;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		 ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    Connection connection;
		try {
			connection = factory.newConnection();
			Channel channel = connection.createChannel();
			
			channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
		    String queueName = channel.queueDeclare().getQueue();
		    channel.queueBind(queueName, EXCHANGE_NAME, "");
		    
		    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
		        String message = new String(delivery.getBody(), "UTF-8");
		        String [] retMsg = parseMessage(message);
		        
		        if(!retMsg[0].equals(this.name)) {
		        	System.out.println(retMsg[0] + ": " + retMsg[1]);
		        }
		    };
		    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
			
		} catch (IOException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	
	public String[] parseMessage(String message) {
		String[] retMsg = new String[2];
		
		String[] arr = message.split("\r\n");
		if(arr[0].equals("IMAGE")) {
			retMsg[0] = arr[1];
			retMsg[1] = arr[2];
			
			if(!retMsg[0].equals(this.name))
				storeImage(arr[2], arr[3]);
		}
		else if(arr[0].equals("TEXT")){
			retMsg[0] = arr[1];
			retMsg[1] = arr[3];
		}
		else {
			retMsg[0] = this.name;
			System.out.println(arr[1]);
		}
		
		return retMsg;
	}
	
	public void storeImage(String imgName, String encodedImg) {
		
		byte[] data = Base64.getDecoder().decode(encodedImg);
		
        String imgPath = this.path + "\\" + imgName;
        
        File file = new File(imgPath);
        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(data);
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
	}
}
