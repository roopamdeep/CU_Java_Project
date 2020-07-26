import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class exchange {

	static ArrayList<Call> callArr = new ArrayList<Call>();
	static HashMap<String, Caller> caller = new HashMap<String, Caller>();
	static BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

	public static void parse_data() throws Exception {
		URL path = exchange.class.getResource("C:\\Users\\josan\\Downloads\\Java.zip\\Java");
		File file = new File(path.getFile());

		BufferedReader br = new BufferedReader(new FileReader(file));
		String temp;
		while ((temp = br.readLine()) != null) {
			String result = temp.substring(temp.indexOf("{") + 1, temp.indexOf("}."));
			String[] temparr = result.split(",", 2);

			String tempcalls = temp.substring(temp.indexOf("[") + 1, temp.indexOf("]"));
			String[] calls = tempcalls.split(",");
			callArr.add(new Call(temparr[0], calls));
		}
		br.close();
	}

	public static void main(String[] args) throws Exception {
		parse_data();
		int numcalls = 0;
		System.out.println("\n* * Calls to be made * *");
		for (Call obj : callArr) 
		{
			System.out.println(obj.getName() + ": [" + String.join(",", obj.getCalls()) + "]");
			numcalls = numcalls + obj.getNumCalls();
			caller.put(obj.getName(), new Caller(obj.getName(), queue, obj.getCalls()));
		}

		for(Caller call: caller.values()) {
			call.start();
		}
		
		System.out.print("\n");
		for(int i=0; i<2*numcalls; i++) {
			String data = queue.take().toString();
			String[] parts = data.split(":");
			if(parts[0].equals("intro")) {
				System.out.println(parts[2] + " received intro message from " + parts[1] + " [" + parts[3] + "]" );
			}
			if(parts[0].equals("reply")) {
				System.out.println(parts[2] + " received reply message from " + parts[1] + " [" + parts[3] + "]" );
			}	
		}
		
		try {
			Thread.sleep(10000);
			System.out.println("\nMaster has received no replies for 10 seconds, ending...");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static class Call {
		private String[] Calls;
		private String Name;

		public Call(String Name, String[] Calls) {
			this.Calls = Calls;
			this.Name = Name;
		}
		//
		public String[] getCalls() {
			return Calls;
		}

		public String getName() {
			return Name;
		}

		public Integer getNumCalls() {
			return Calls.length;
		}
	}

	static class Caller extends Thread {

		BlockingQueue<String> queue;
		String[] calls = null;
		String name;

		public Caller(String name, BlockingQueue<String> queue, String[] calls) {
			this.queue = queue;
			this.calls = calls;
			this.name = name;
		}

		public void run() {
			Thread.currentThread().setName(name);
			for (String call : calls)  {
				try {
					Thread.sleep((long)(Math.random() * 100));
					Long time = System.currentTimeMillis();
					queue.put("intro:"+Thread.currentThread().getName()+":"+call+":"+time);
					caller.get(call).reply(Thread.currentThread().getName(), call, time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(5000);
				System.out.println("\nProcess " + Thread.currentThread().getName() + " has received no calls for 5 seconds, ending...");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void reply(String from, String to, Long time) throws InterruptedException {
			Thread.sleep((long)(Math.random() * 100));
			queue.put("reply:"+to+":"+from+":"+time);
		}
	}
}