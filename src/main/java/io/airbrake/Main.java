package io.airbrake;

import java.util.Date;
import java.util.Timer;
import io.airbrake.javabrake.Config;
import io.airbrake.javabrake.Metrics;
import io.airbrake.javabrake.Notifier;
import io.airbrake.javabrake.QueueMetric;
import io.airbrake.javabrake.RouteMetric;

public class Main {
	static Config config;
	static Notifier notifier;
	static Timer rTimer; 

	public static void main(String[] args) {
		config = new Config();
		config.projectId = 412680;
		config.projectKey = "77eb9b2856aa85fda5ad754a2e8ff9f3";
		config.performanceStats  = true;
	
		notifier = new Notifier(config);

		// try {
		// 	int a = 10/0;
		// } catch (Exception e) {
		// 	notifier.report(e);
		// }

	   
		//	routeTest();
			queryTest();
		//	queueTest();
		
	}

	public static void queryTest(){
		Date startTime = new Date();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		try {
			notifier.queries.notify( "GET",
			"/test",
			"select * from employee",
			startTime,new Date(),null,null,0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void queueTest(){


		QueueMetric metrics4 = new QueueMetric( "sj");
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		metrics4.startSpan("DB", new Date());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		metrics4.endSpan("DB", new Date());

		metrics4.startSpan("view", new Date());
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		metrics4.endSpan("view", new Date());		
		metrics4.end();
		try {
			notifier.queues.notify(metrics4);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public static void routeTest()
	{
		//Route
		RouteMetric metrics = new RouteMetric("POST",
		"/test1/:1");
		try {
		Thread.sleep(1500);
		} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
		
		metrics.end();
		metrics.statusCode = 400;
		metrics.contentType = "application/json";
		notifier.routes.notify(metrics);

		RouteMetric metrics1 = new RouteMetric("POST",
		"/test2/:1");
		try {
		Thread.sleep(3500);
		} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
		
		metrics1.end();
		metrics1.statusCode = 500;
		metrics.contentType = "application/json";
		notifier.routes.notify(metrics1);

		RouteMetric metrics2 = new RouteMetric("POST",
		"/test3/:1");
		try {
		Thread.sleep(10000);
		} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
		
		metrics2.end();
		metrics2.statusCode = 300;
		metrics.contentType = "application/json";
		notifier.routes.notify(metrics2);

		// config.environment = "test";
		// notifier = Notifier.getInstance(config);

	
// // RouteBreakDown
		RouteMetric metrics3 = new RouteMetric("POST", "/testrb/:1");
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		metrics3.startSpan("DB", new Date());
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		metrics3.endSpan("DB", new Date());

		metrics3.startSpan("view", new Date());
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		metrics3.endSpan("view", new Date());		
		metrics3.end();
		metrics3.statusCode = 300;
		metrics.contentType = "application/json";
		notifier.routes.notify(metrics3);


	}
}
