package net;

import java.io.File;

import net.ZSocket.Answer;

public class A {

	public static int a = 12;
	public static int b=a;
	public A() {
		System.out.println(b);
	}
	public static void main(String[] args) {
		A a=new A();
//		ZSocket zSocket=ZSocket.getInstance().asServer().setPort(5556).addAnswer("HELLOSERVER", new Answer() {
//			
//			@Override
//			public String getAnswer(String param) {
//				return "HELLOCLIENT";
//			}
//		});
	}

}
