package ChatRoom_add_4_3;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JList;

public class FriendsListListener implements MouseListener{
	ChatRoom_add_4_3 chat = null;
	JList list = null;
	String id = null;

	
	FriendsListListener(ChatRoom_add_4_3 chat,JList list){
		this.chat = chat;
		this.list = list;
	}
	
	public String getTargetID(){
		return id;
	}
	
	public void setTargetID(){
		this.id = null;
	}
	
	public void mouseClicked(MouseEvent e){
		if(e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()==2){
			id = (String)list.getSelectedValue();
			if(id==null || id.equals(chat.listen.getID()))
				return ;
			chat.flag = true;
			chat.idLabelPersonal.setText(id);
			chat.startPersonalChat();
		} else if(e.getButton()==MouseEvent.BUTTON3 && e.getClickCount()==1){//添加菜单，可选私聊或删除好友操作
			System.out.println("this is right button..");
		}
	}

    public void mousePressed(MouseEvent e){}

    public void mouseReleased(MouseEvent e){}

    public void mouseEntered(MouseEvent e){}

    public void mouseExited(MouseEvent e){}
}
