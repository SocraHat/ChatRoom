package ChatRoom_add_4_3;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class ChatListener implements ActionListener{
	ChatRoom_add_4_3 chat = null;
	Socket socket = null;
	JLabel idLabel = null;
	RMessage r = null;
	OutputStream os = null;
	DataOutputStream dos = null;
	InputStream is = null;
	DataInputStream dis = null;
	private String id = "";
	private char[] password = null;
	static String targetID = null;
	static final String REG = "(retsiger)";
	
	ChatListener(ChatRoom_add_4_3 chat,JLabel idLabel){
		this.chat = chat;
		this.idLabel = idLabel;
	}
	
	public String getID(){
		return id;
	}
	
	public void actionPerformed(ActionEvent e){//需要提前手动开启服务器
		if(e.getActionCommand().equals("注册")){
			chat.warn.setText("");
			chat.launchFrame.setVisible(false);
			chat.registerFrame();
		} else if(e.getActionCommand().equals("确定")){
			id = chat.regText.getText();
			password = chat.regPassword.getPassword();
			startSocket();
			try{
				os = socket.getOutputStream();
				dos = new DataOutputStream(os);
				dos.writeInt(id.length()+password.length+REG.length()+1);
				os.write((id+"*"+new String(password)+REG).getBytes());
				os.flush();
			} catch(IOException error){
				System.out.println("error in register...");
			}
			id = "";
			password = null;
			chat.loginText.setText("");
			chat.loginPassword.setText("");
			chat.regFrame.setVisible(false);
			chat.launchFrame.setVisible(true);

		} else if(e.getActionCommand().equals("登陆")){//登陆之后，id和密码都有保存
			id = chat.loginText.getText();//获取登陆的id
			password = chat.loginPassword.getPassword();//获取登陆的id
			if(id.length()==0){
				chat.warn.setText("账号不能为空");
				return ;
			}
			if(password.length==0){
				chat.warn.setText("密码不能为空");
				return ;
			}
			//无论是否可以连上服务器，都需要先进行连接
			startSocket();
			try {
				os = socket.getOutputStream();
				dos = new DataOutputStream(os);
				dos.writeInt(id.length()+1+password.length);
				os.write(new String(id+"*"+new String(password)).getBytes());//把id和密码 转换为byte数组传过去
				os.flush();
				is = socket.getInputStream();
				dis = new DataInputStream(is);
				int mark = dis.readInt();
				if(mark==1){//允许登陆之后的操作					
					chat.idLabel.setText("ID : " + id);
					chat.launchFrame.setVisible(false);
					chat.loginFrame();
					showMessage();
				} 
				else if(mark==2){
					chat.warn.setText("账号已登陆");
				} else if(mark==0){
					chat.warn.setText("账号密码不存在");
				}
				
			} catch (IOException e1) {
				System.out.println("error in login...");
			}
			
		} else if(e.getActionCommand().equals("发送群聊消息")){//点击发送就开始往流中写入数据
			if(targetID==null)
				targetID = "TNEILCLLA";
			try {
				os = socket.getOutputStream();
				dos = new DataOutputStream(os);
				String out = new String(id+"*"+chat.sendGroupMessageArea.getText()+"*"+targetID);
				if(out.length()!=0){
					dos.writeInt(out.getBytes().length);
					os.write(out.getBytes());
					os.flush();
				}
			} catch (IOException e1) {
				System.out.println("error in send group Message...");
			}
			chat.sendGroupMessageArea.setText(null);
			targetID = null;
		} else if(e.getActionCommand().equals("发送私聊消息")){
			targetID = chat.friendListListen.getTargetID();
			try {
				os = socket.getOutputStream();
				dos = new DataOutputStream(os);
				String out = new String(id+"*"+chat.sendPersonalMessageArea.getText()+"*"+targetID);
				if(out.length()!=0){
					dos.writeInt(out.getBytes().length);
					os.write(out.getBytes());
					os.flush();
				}
			} catch (IOException e1) {
				System.out.println("error in send personal Message...");
			}
			chat.sendPersonalMessageArea.setText(null);
			targetID = null;
		} else if(e.getActionCommand().equals("刷新ID列表")){
			chat.onLineIDArea.setText("");
			try {
				os = socket.getOutputStream();
				dos = new DataOutputStream(os);
				//String out = new String(id+"*"+"DIENILNOLLAC"+"*"+"DIENILNOLLAC");
				String out = new String(id+"*"+"DISDNEIRFHSULF"+"*"+"DISDNEIRFHSULF");
				if(out.length()!=0){
					dos.writeInt(out.length());
					os.write(out.getBytes());
					os.flush();
				}
			} catch (IOException e1) {
				System.out.println("error in get onine id...");
			}
		} else if(e.getActionCommand().equals("群聊")){
			chat.startGroupChat();
		} else if(e.getActionCommand().equals("添加好友")){
			chat.addFriend();
		} else if(e.getActionCommand().equals("添加")){
			String addTargetID = (String) chat.onlineList.getSelectedValue();//获取选中的id
			if(addTargetID.equals(id))
				return ;
			//添加弹窗，提示请求已发送
			JOptionPane.showMessageDialog(null, "请求已发送");
			chat.addFriendFrame.setVisible(false);
			try {
				os = socket.getOutputStream();
				dos = new DataOutputStream(os);
				String out = new String(id+"*"+addTargetID+"*"+"DNEIRFDDALLAC");
				if(out.length()!=0){
					dos.writeInt(out.length());
					os.write(out.getBytes());
					os.flush();
				}
			} catch (IOException e1) {
				System.out.println("error in add friend...");
			}
		} else if(e.getActionCommand().equals("确认添加")){
			chat.applyFriendFrame.setVisible(false);
			try {
				os = socket.getOutputStream();
				dos = new DataOutputStream(os);
				String out = new String(id+"*"+"SEY|"+chat.applyID+"*"+"DNEIRFDDALLACYLPER");
				if(out.length()!=0){
					dos.writeInt(out.length());
					os.write(out.getBytes());
					os.flush();
				}
			} catch (IOException e1) {
				System.out.println("error in reply apply YES...");
			}
		} else if(e.getActionCommand().equals("拒绝添加")){
			chat.applyFriendFrame.setVisible(false);
			try {
				os = socket.getOutputStream();
				dos = new DataOutputStream(os);
				String out = new String(id+"*"+"TSUJER|"+chat.applyID+"*"+"DNEIRFDDALLACYLPER");
				if(out.length()!=0){
					dos.writeInt(out.length());
					os.write(out.getBytes());
					os.flush();
				}
			} catch (IOException e1) {
				System.out.println("error in reply apply NO...");
			}
		}
	}
	
	private void startSocket(){
		try {
			socket = new Socket("127.0.0.1",7777);
		} catch (IOException e) {
			System.out.println("error in socket...");
		}
	}
	
	/**
	 * 启动接收消息的线程
	 */
	private void showMessage(){
		r = new RMessage(chat,socket,chat.onLineIDArea,id);
		Thread t = new Thread(r);
		t.start();
	}
	
}
