import java.util.concurrent.locks.*;
import java.util.*;

public class MutexTest {
	private static Lock _mutex = new ReentrantLock(true);
	
	public static void main(String[] args) {
		Thread one = new Thread(new A());
		Thread two = new Thread(new B());
		
		one.start();
		two.start();

	}
	
	public static class B implements Runnable{
		public B(){}
		
		@Override
		public void run() {
			try{
				_mutex.lock();
					for(int i=0 ;i < 20 ; i++){
						Thread.sleep(200);
						System.out.println("B");
					}
				_mutex.unlock();
			}catch(Exception e){
				e.printStackTrace();
			}
			// TODO Auto-generated method stub
			
		}
	
	}//end B
	
	public static class A implements Runnable{
		public A(){}
		
		@Override
		public void run() {
			try{
				_mutex.lock();
					for(int i=0 ;i < 20 ; i++){
						Thread.sleep(300);
						System.out.println("A");
					}
				_mutex.unlock();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}//end A
}

