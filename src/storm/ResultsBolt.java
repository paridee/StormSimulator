package storm;


import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulation.SimulationMain;

public class ResultsBolt extends BaseBasicBolt{
	public static int counter	=	1;
	public final static Logger logger	=	LoggerFactory.getLogger(ResultsBolt.class);
	static double 	bestEpsilon		=	0;
	static double 	bestAlpha		=	0;
	static double 	bestYota		=	0;
	static double  	bestReward		=	Double.NEGATIVE_INFINITY;
	String report	=	"Reward,Epsilon,Alpha,Yota;\n";
	/**
	 * 
	 */
	private static final long serialVersionUID = -7783234200219060017L;

	@Override
	public void execute(Tuple arg0, BasicOutputCollector arg1) {
		// TODO Auto-generated method stub
		double 	epsilon		=	arg0.getDouble(0);
		double 	alpha		=	arg0.getDouble(2);;
		double 	yota		=	arg0.getDouble(1);
		double  reward		=	arg0.getDouble(3);
		if(reward>bestReward){
			bestEpsilon		=	epsilon;
			bestAlpha		=	alpha;
			bestYota		=	yota;
			bestReward		=	reward;
			String body		=	"New best configuration found, value "+reward+" epsilon "+epsilon+" alpha "+alpha+" yota "+yota;
			String address	=	"paride.casulli@gmail.com";
			String obj		=	"Better configuration found!";
			sendEmail(body+" \n"+report, obj, address);
		}
		report	=	report+reward+","+epsilon+","+alpha+","+yota+";\n";
		if(++counter%30==0){
			String obj		=	"Configurations report";
			String address	=	"paride.casulli@gmail.com";
			sendEmail(report, obj, address);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public static void sendEmail(String text,String obj,String address){
	    Properties props = new Properties();
	    props.put("mail.smtp.host", "secure.alien8.it");
	    props.put("mail.smtp.socketFactory.port", "465");
	    props.put("mail.smtp.socketFactory.class",
	            "javax.net.ssl.SSLSocketFactory");
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.port", "465"); 
	    Session session = Session.getDefaultInstance(props,
	        new javax.mail.Authenticator() {
	                            @Override
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication("noreply@eclshop.tv","193ofQf279");
	            }
	        });

	    try {

	        Message message = new MimeMessage(session);
	        message.setFrom(new InternetAddress("noreply@eclshop.tv"));
	        message.setRecipients(Message.RecipientType.TO,
	                InternetAddress.parse(address));
	        message.setSubject(obj);
	        message.setText(text);
	        
	        Transport.send(message);

	        System.out.println("Sending result, done.");

	    } catch (MessagingException e) {
	        throw new RuntimeException(e);
	    }
	}
	
	public static void main(String[] args){
		String testb	=	"pippo";
		String obj		=	"test";
		String mailB	=	"paride.casulli@gmail.com";
		sendEmail(testb,obj,mailB);
	}

}
