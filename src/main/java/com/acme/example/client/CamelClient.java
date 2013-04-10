/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.acme.example.client;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.ProducerTemplate;

/**
 * Client that uses the {@link ProducerTemplate} to easily exchange messages with the Server.
 * <p/>
 * Requires that the JMS broker is running, as well as CamelServer
 */
public final class CamelClient {
	
	
    private CamelClient() {
        // Helper class
    }

    public static void main(final String[] args) throws Exception {
        System.out.println("Notice this client requires that the CamelServer is already running!");
        Connection connection = null;
		Session session = null;
		
		MessageProducer producer = null;
		MessageProducer stop = null;
		MessageProducer start = null;

		String text = "<activemq>Test message X now=Y</activemq>";
		
		try {
			
			ActiveMQConnectionFactory amq = new ActiveMQConnectionFactory("admin","admin","tcp://localhost:61616");
			connection = amq.createConnection();
			
	        connection.setExceptionListener(new javax.jms.ExceptionListener() { 
	        	public void onException(javax.jms.JMSException e) {
	        		e.printStackTrace();
	        	} 
	        });
	
	        connection.start();
	        
	        // Create a Session
	        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	        
	        Destination inDestination = session.createQueue("in");
	        Destination startDestination = session.createTopic("start");
	        Destination stopDestination = session.createTopic("stop");
	        
			producer = session.createProducer(inDestination);
			start = session.createProducer(startDestination);
			stop = session.createProducer(stopDestination);
    		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
    		
    		TextMessage stopMessage = session.createTextMessage("stop");
    		TextMessage startMessage = session.createTextMessage("stop");
    		
			
	        long sendStartTime = System.currentTimeMillis();
	        int sent=0;
	        boolean stopped = false;
            int size = 10000;
	        
	        // Create and send message
	        for (int index=0; index<size; index++) {
	       	    
	       	    String t1 = text.replace("X", String.valueOf(index));
	       	    String t2 = t1.replace("Y", String.valueOf(System.currentTimeMillis()));
	            TextMessage message = session.createTextMessage(t2);
	            producer.send(message);
	        	sent++;
				
	        	try {
	        		Thread.sleep(5);
	        	} catch (InterruptedException e) {
	        		// NOOP
	        	}
	    
	        	
	        	if(index > 0 && (index % 100) == 0) {    
	        		System.out.println("Sent " + index + " msgs");
	        	}


                if (index > 1 && index < size && index % 2000 == 0) {
                    if(!stopped) {
                        System.out.println("Sending stop to camel route");
                        stop.send(stopMessage);
                        stopped = true;
                    } else {
                        System.out.println("Sending start to camel route");
                        start.send(startMessage);
                        stopped = false;
                    }
                }
	        }
	        
	        long sendEndTime = System.currentTimeMillis();
	        
	        long totalTime = (sendEndTime - sendStartTime);
	        double msgPerSec = 10000 / (totalTime / (double)1000);
	       
	        System.out.println("Completed (" + msgPerSec + " msgs/s)");
		} catch (Exception ex) {
			System.out.println("exiting due to exception:" + ex.getMessage());
			return;
		}
		finally {
			
			try {
				producer.close();
				start.close();
				stop.close();
				
			    if (session != null)
		        	session.close();
		        if (connection != null)
		        	connection.close();
			} catch (Exception e) {
				System.out.println("Problem closing down JMS objects: " + e);
			}
		}
        
        
        	
        	
        System.exit(0);
    }

}
