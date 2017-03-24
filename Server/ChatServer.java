/*	版本4.3
 *	根据客户端发送的请求，来判断是否是好友，写回特殊的命令响应
 *	不是好友可以申请加好友并存储，写回响应结果
 *	好友申请可以通过或是不通过，写回响应结果
 *	
 *	指定通信协议：
 *	DIENILNOLLAC  申请获取所有id
 *	TNEILCLLAC	把消息发送给所有人
 *	NOITACILPPALLAC		申请好友
 *	DIENILNOHSULF	刷新在线id
 *	DNESREGNARTS	陌生人发消息
 *	收到消息可以自动弹出？？？客户端可以设置
 */

package ChatServer_add_4_3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ChatServer extends JFrame{
	ArrayList<MySocket> mySockets = new ArrayList<MySocket>();//存放MySocket
	HashMap<String,String> record = new HashMap<String,String>();//根据id查找socket
	static JTextArea showMessageArea = new JTextArea();
	static JTextArea idArea = new JTextArea();
	static JTextArea clientCountArea = new JTextArea();
	static JTextArea idAndPasswordArea = new JTextArea();
	static String id = "";
	static String password = "";
	static final String REG = "(retsiger)";
	
	
	public static void main(String[] args){
		ChatServer server = new ChatServer();
		server.serverUI();
		server.readRecord(server);
		server.launchServer();
	}

	
	/**
	 * 单独的拎出来实现启动
	 */
	private void launchServer(){
		showMessageArea.setText("server is going ..." + "\r\n" + "-------------------");
		clientCountArea.setText("now number of client:   0");
		ServerSocket ss = null;
		InputStream is = null;
		DataInputStream newDis = null;
		OutputStream os = null;
		DataOutputStream dos = null;
		ChatServer server = new ChatServer();
		
		try {
			ss = new ServerSocket(7777);//建立socket端口监听
		} catch (IOException e) {
			System.out.println("error in link to client...");
		}
		while(true){			
			clientCountArea.setText("now number of client:   " + server.mySockets.size());
			
			try {
				//阻塞式接受socket，然后新开启一个线程
				Socket s = ss.accept();
				is = s.getInputStream();
				newDis = new DataInputStream(is);//获取客户端的流
				int length = newDis.readInt();
				byte[] bytes = new byte[length];
				is.read(bytes);
				if(new String(bytes).indexOf(REG)!=-1){
					register(bytes,s);
				} else{
					getIDAndPassword(bytes);
					os = s.getOutputStream();//服务器回写标志，1可以登陆，0不允许登陆
					dos = new DataOutputStream(os);
					if(judgeLogin(s)){
						if(judgeLoginAgain(server.mySockets,s,id)){
							MySocket myS = new MySocket(id,s);
							server.mySockets.add(myS);
							dos.writeInt(1);//允许登陆后，服务器回写标识
							dos.flush();
							updateGlobalID(server.mySockets, myS);//向所有客户端发送id列表消息
							//还应该向所有客户端都发送自己的好友列表id,   后面实现
							updateFriendID( myS);
							new Thread(new ChatServerReader(myS,server)).start();
							clientCountArea.setText("now number of client:   " + server.mySockets.size());
							idArea.append("client id :"+id+"   is linking....\r\n");
							id = "";
							password = "";
						} else{
							System.out.println(id + " 重复登陆...");
							dos.writeInt(2);
							dos.flush();
							s.close();
						}
					} else{
						dos.writeInt(0);
						dos.flush();
						s.close();
					}
				}
			} catch (IOException e) {
				System.out.println("error in server socket...");
			}
		}
	}
	
	private void readRecord(ChatServer server) {
		byte[] fileRecord = null;
		File file = null;
		FileInputStream fis = null;
		try {
			file = new File("src\\record.txt");
			fis = new FileInputStream(file);
			fileRecord = new byte[(int) file.length()];
			fis.read(fileRecord);
		} catch (IOException e) {
			System.out.println("error in load record ...");
		}
		String[] recordStr = new String(fileRecord).split("\\*\\|\\*");
		for(int i=0; i<recordStr.length; i++){
			String[] iap = recordStr[i].split("-");
			record.put(iap[0],iap[1]);
			server.idAndPasswordArea.append("\r\n" + iap[0] + " ---------- " + iap[1]);
		}
		try {
			fis.close();
		} catch (IOException e) {
			System.out.println("error in close readfile ...");
		}
	}
	
	private void writeRecord(String id,String password){
		try {
			File file = new File("src\\record.txt");
			FileOutputStream fos = new FileOutputStream(file,true);//true代表向源文件中追加
			String appendRecord = "*|*"+id+"-"+password;
			fos.write(appendRecord.getBytes());
			fos.flush();
			fos.close();
		} catch (IOException e) {
			System.out.println("error in write record ...");
		}
	}
	
	/*
	 * 刷新所有客户端的online id 列表
	 */
	private void updateGlobalID(ArrayList<MySocket> mySockets, MySocket myS) throws IOException{
		String temporary = getONLINEID(mySockets, myS);
		OutputStream os = null;
		DataOutputStream dos = null;
		for(int i=0 ; i<mySockets.size(); i++){
			MySocket myNewS = mySockets.get(i);
			os = myNewS.getSocket().getOutputStream();//因为是多线程，所以，并不能就直接定义一个可以统一使用的变量吧？？？
			dos = new DataOutputStream(os);
			String out = myS.getId()+"*"+temporary+"*"+"DIENILNOHSULF";
			dos.writeInt(out.getBytes().length);
			os.write(out.getBytes());
			os.flush();
		}
	}
	
	/*
	 * 应该是只针对调用方法的socket发送该客户端的所有好友id
	 */
	private void updateFriendID(MySocket myS) throws IOException{
		String temporary = "";
		OutputStream os = null;
		DataOutputStream dos = null;
		temporary = getFriendsID(myS);//获取所有socket自己专属的friendsID列表
		System.out.println("socket id:"+myS.getId()+" friends id: "+temporary);
		os = myS.getSocket().getOutputStream();
		dos = new DataOutputStream(os);
		String out = myS.getId()+"*"+temporary+"*"+"DISDNEIRFHSULF";
		dos.writeInt(out.getBytes().length);
		os.write(out.getBytes());
		os.flush();
	}
	
	private String getFriendsID(MySocket myS){
		String friends = myS.getId();
		for(int j=0;j<myS.getFriends().size();j++){
			friends += "|"+myS.getFriends().get(j);//每一个socket中包含的只有好友id，没有自己的id
		}
		return friends;
	}
	
	private void getIDAndPassword(byte[] bytes){
		for(int i=0; i<bytes.length; i++){
			if(bytes[i]=='*'){
				id = new String(bytes).substring(0, i);
				password = new String(bytes).substring(i+1, bytes.length);
				break;
			}
		}
	}
	
	private boolean judgeLogin(Socket s) throws IOException{
		if(record.get(id)!=null)
			return true; 
		return false;
	}
	
	private boolean judgeLoginAgain(ArrayList<MySocket> mySockets,Socket s,String id) throws IOException{
		for(int i=0; i<mySockets.size(); i++){
			if(id.equals(mySockets.get(i).getId()))
				return false;
		}
		return true;
	}
	
	private void register(byte[] bytes,Socket s) throws IOException{
		for(int i=0; i<bytes.length-REG.length(); i++){//(rersiger)
			if(bytes[i]=='*'){
				id = new String(bytes).substring(0, i);
				password = new String(bytes).substring(i+1, bytes.length-REG.length());
				record.put(id, password);
				idAndPasswordArea.append("\r\n" + id + " ---------- " + password);
				writeRecord(id,password);
				break;
			}
		}
		s.close();
	}
	
	/**
	 * 为什么实现了接口后，重写的抽象方法必须定义成public？
	 * 把与客户端连接后的操作写入run方法中 
	 * @author Suagr
	 *
	 */
	static class ChatServerReader implements Runnable{
		
		InputStream is = null;
		OutputStream os = null;
		DataInputStream dis;
		DataOutputStream dos;
		ChatServer server = null;
		MySocket myS = null;
		String sendID = null;
		String message = null;
		String targetID = null;
		
		/**
		 * 获取当前连接端口的输入输出流
		 * @param s
		 */
		ChatServerReader(MySocket myS , ChatServer server){
			try {
				this.myS = myS;
				is = myS.getSocket().getInputStream();
				 dis = new DataInputStream(is);
				 this.server = server;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void run(){
			while(true){
				try {
					int length = dis.readInt();
					byte[] bytes = new byte[length];
					is.read(bytes);
					String out = new String(bytes);
					int flag = 0;
					for(int i=0; i<out.length(); i++){
						if(out.charAt(i)=='*' && flag==0){
							flag = i;
							sendID = out.substring(0, i);
						} else if(out.charAt(i)=='*' && flag!=0){
							message = out.substring(flag+1,i);
							targetID = out.substring(i+1,out.length());
						}
					}
					//System.out.println(sendID+" "+message+" "+targetID);
					
					for(int i=0; i<server.mySockets.size(); i++){		
						MySocket myNewS = server.mySockets.get(i);
						if(myNewS == this.myS){
							showMessageArea.append("\r\n"+"CLIENT ID:   " +server.mySockets.get(i).getId() );
							showMessageArea.append("\r\nmessage:   " +message);
						}
						
						if(targetID.equals("TNEILCLLA") ){//请求群发
							os = myNewS.getSocket().getOutputStream();
							dos = new DataOutputStream(os);
							dos.writeInt(out.getBytes().length);
							os.write(out.getBytes());
							os.flush();
						} else if(targetID.equals("DIENILNOLLAC")){//请求刷新在线id
							if(myNewS == this.myS){
								String ONLINEID = getONLINEID(server.mySockets,this.myS);
								os = myNewS.getSocket().getOutputStream();
								dos = new DataOutputStream(os);
								String newMessage = sendID+"*"+ONLINEID+"*"+targetID;
								dos.writeInt(newMessage.getBytes().length);//传都传字节数组的长度
								os.write(newMessage.getBytes());
								os.flush();		
							}
						} else if(targetID.equals("DNEIRFDDALLAC")){//请求添加好友，目标ID在message里面
							String addTargetID = message;
							if(myNewS.getId().equals(addTargetID)){
								os = myNewS.getSocket().getOutputStream();
								dos = new DataOutputStream(os);
								String newMessage = sendID+"*"+"DNEIRFDDALLAC"+"*"+"DNEIRFDDALLAC";
								dos.writeInt(newMessage.getBytes().length);//传都传字节数组的长度
								os.write(newMessage.getBytes());
								os.flush();
							}
						} else if(targetID.equals("DNEIRFDDALLACYLPER")){
							String[] reply = message.split("\\|");
							System.out.println(message+" get add friend application ");
							if(myNewS.getId().equals(reply[1])){
								if(reply[0].equals("SEY")){//互相加上好友
									myS.changeFriends(reply[1], true);
									myNewS.changeFriends(myS.getId(), true);
									//如果同意添加好友，则应该刷新双方的好友列表  
									System.out.println("add succeed ");
									server.updateFriendID(myNewS);
									server.updateFriendID(myS);
								}
								os = myNewS.getSocket().getOutputStream();
								dos = new DataOutputStream(os);
								String newMessage = sendID+"*"+reply[0]+"*"+"DNEIRFDDALLACYLPER";
								dos.writeInt(newMessage.getBytes().length);//传都传字节数组的长度
								os.write(newMessage.getBytes());
								os.flush();
							}
						} else if(targetID.equals("DISDNEIRFHSULF")){ //主动申请刷新好友id列表
							server.updateFriendID(myS);
						} else{//请求私聊
							//else if(!targetID.equals("TNEILCLLA") && !targetID.equals("DIENILNOLLAC") && !targetID.equals("DNEIRFDDALLAC"))
							if(myNewS.getId().equals(targetID) || myNewS.getId().equals(sendID)){
								os = myNewS.getSocket().getOutputStream();
								dos = new DataOutputStream(os);
								dos.writeInt(out.getBytes().length);
								os.write(out.getBytes());
								os.flush();
							}
						}
					}
					
				} catch (IOException e) {
					String idList = getONLINEID(server.mySockets, myS);
					updateIDThen(server.mySockets, myS, os, dos, idList.substring(idList.indexOf("|")+1, idList.length()));
					server.mySockets.remove(myS);
					idArea.append("client id :"+myS.getId()+"   is leaving....\r\n ");
					clientCountArea.setText("now number of client:   " + server.mySockets.size());
					break;
				}
			}
		}
	}
	
	private static String getONLINEID(ArrayList<MySocket> mySockets,MySocket myS){
		String ONLINEID = myS.getId();
		for(int j=0;j<mySockets.size();j++){
			if(!mySockets.get(j).getId().equals(myS.getId())){
				ONLINEID += "|"+mySockets.get(j).getId();
			}
		}
		return ONLINEID;
	}
	
	private static void updateIDThen(ArrayList<MySocket> mySockets, MySocket myS, OutputStream os,DataOutputStream dos,String ONLINEID){
		//把新的id信息写回到其余客户端中
		try {
			for(int i=0; i<mySockets.size(); i++){
				MySocket myNewS = mySockets.get(i);
				if(myNewS!=myS){
					os = myNewS.getSocket().getOutputStream();
					dos = new DataOutputStream(os);
					String newMessage = "DIENILNOHSULF" + "*" + ONLINEID + "*" + "DIENILNOHSULF";
					dos.writeInt(newMessage.getBytes().length);
					os.write(newMessage.getBytes());
					os.flush();
				}
			}
		}  catch (IOException e1) {
			System.out.println("error is return onlineID ...");
		}
	}
	
	private void serverUI(){
		this.setTitle("Server");
		this.setSize(400,700);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setDefaultCloseOperation(3);
		this.setLayout(null);//把默认的流式布局取消
		//设置不可编辑
		clientCountArea.setEditable(false);
		clientCountArea.setBounds(5,5,380,35);
		idArea.setEnabled(false);
		JScrollPane idPane = new JScrollPane(idArea);
		idPane.setBounds(5, 50, 380, 90);
		showMessageArea.setEnabled(false);
		showMessageArea.setVerifyInputWhenFocusTarget(true);
		JScrollPane showMessagePane = new JScrollPane(showMessageArea);
		showMessagePane.setBounds(5,150,380,400);
		idAndPasswordArea.setEnabled(false);
		JScrollPane idAndPassPane = new JScrollPane(idAndPasswordArea);
		idAndPassPane.setBounds(5,560,380,100);
		idAndPasswordArea.setText(" id ----------  password");//10个-
		
		this.add(clientCountArea);
		this.add(idPane);
		this.add(showMessagePane);
		this.add(idAndPassPane);
		this.setVisible(true);
	}
}
